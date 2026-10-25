package com.dadaz.base.module.redis;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 通用操作服务
 * <p>
 * 参考 1024lab 的写法：值统一按字符串存取，对象先用 fastjson2 转成 JSON 字符串再写入，
 * 因此<b>不需要额外的 RedisConfig</b>，只依赖 Spring Boot 自动配置的 {@link StringRedisTemplate}。
 * <p>
 * 约定：
 * <ul>
 *   <li>{@link #set(String, String)} 存原始字符串，用 {@link #get(String)} 读；</li>
 *   <li>{@link #set(String, Object)} 存 JSON，用 {@link #getObject(String, Class)} 读；</li>
 *   <li>Hash / List / Set 的元素统一按 JSON 存储，读取时传入元素类型（字符串元素传 {@code String.class}）；</li>
 *   <li>本类不生成环境前缀 key，前缀由调用方自己拼；分布式锁严格场景请优先使用项目已引入的 Redisson。</li>
 * </ul>
 *
 * @author dadaz
 */
@Service
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    private final ValueOperations<String, String> valueOperations;

    private final HashOperations<String, String, String> hashOperations;

    private final ListOperations<String, String> listOperations;

    private final SetOperations<String, String> setOperations;

    /**
     * 构造器注入：Spring Boot 自动配置的 StringRedisTemplate 已经是 String 序列化，
     * 不需要我们再声明 RedisTemplate<String, Object> 或任何 @Bean
     *
     * @param stringRedisTemplate 字符串模板
     */
    public RedisService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.valueOperations = stringRedisTemplate.opsForValue();
        this.hashOperations = stringRedisTemplate.opsForHash();
        this.listOperations = stringRedisTemplate.opsForList();
        this.setOperations = stringRedisTemplate.opsForSet();
    }

    // ==================== 通用 key 操作 ====================

    /**
     * 判断 key 是否存在
     *
     * @param key Redis key
     * @return true 表示存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 指定缓存失效时间
     *
     * @param key   Redis key
     * @param second 过期时间（秒）
     * @return true 表示设置成功（key 不存在时返回 false）
     */
    public boolean expire(String key, long second) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, second, TimeUnit.SECONDS));
    }

    /**
     * 获取 key 的剩余过期时间（秒）
     *
     * @param key Redis key
     * @return 剩余秒数；-1 表示永久有效，-2 表示 key 不存在
     */
    public long getExpire(String key) {
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire == null ? -2L : expire;
    }

    /**
     * 删除缓存，可以传一个或多个 key
     *
     * @param keys Redis key
     */
    public void delete(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        stringRedisTemplate.delete(List.of(keys));
    }

    /**
     * 批量删除缓存
     *
     * @param keys Redis key 集合
     */
    public void delete(Collection<String> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        stringRedisTemplate.delete(keys);
    }

    /**
     * 获取当天剩余的秒数，适合做「当天有效」的缓存或计数
     * <p>
     * 例如：{@code set(key, value, currentDaySecond())}，零点自动过期。
     *
     * @return 距离今天 23:59:59 的秒数
     */
    public static long currentDaySecond() {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.of(LocalDate.now(), LocalTime.MAX));
    }

    // ==================== 简单锁 ====================

    /**
     * 尝试获取锁（SET NX EX）
     * <p>
     * 只是最基础的互斥标记，<b>unLock 不校验持有者</b>：如果业务执行时间超过 expire，
     * 锁过期后被别的线程拿到，当前线程再 unlock 会误删别人的锁。
     * 对可靠性有要求的场景（续期、可重入、等待）请直接使用 RedissonClient。
     *
     * @param key         锁 key
     * @param expireMilli 锁过期时间（毫秒），防止宕机后死锁
     * @return true 表示加锁成功
     */
    public boolean getLock(String key, long expireMilli) {
        Boolean success = valueOperations.setIfAbsent(key, String.valueOf(System.currentTimeMillis()),
                expireMilli, TimeUnit.MILLISECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放锁
     * <p>
     * 直接删除 key，不校验持有者；简单场景可用，严格场景请用 Redisson。
     *
     * @param key 锁 key
     */
    public void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    // ==================== String / Object ====================

    /**
     * 获取原始字符串缓存
     *
     * @param key Redis key
     * @return 字符串值；key 不存在时返回 null
     */
    public String get(String key) {
        return key == null ? null : valueOperations.get(key);
    }

    /**
     * 获取 JSON 缓存并反序列化成对象
     *
     * @param key   Redis key
     * @param clazz 目标类型
     * @param <T>   目标类型
     * @return 反序列化后的对象；key 不存在时返回 null
     */
    public <T> T getObject(String key, Class<T> clazz) {
        String json = get(key);
        return json == null ? null : JSON.parseObject(json, clazz);
    }

    /**
     * 缓存原始字符串
     *
     * @param key   Redis key
     * @param value 字符串内容
     */
    public void set(String key, String value) {
        valueOperations.set(key, value);
    }

    /**
     * 缓存原始字符串并设置过期时间
     *
     * @param key    Redis key
     * @param value  字符串内容
     * @param second 过期时间（秒），小于等于 0 表示永久有效
     */
    public void set(String key, String value, long second) {
        if (second > 0) {
            valueOperations.set(key, value, second, TimeUnit.SECONDS);
        } else {
            set(key, value);
        }
    }

    /**
     * 缓存对象（JSON 序列化，永久有效）
     *
     * @param key   Redis key
     * @param value 对象
     */
    public void set(String key, Object value) {
        valueOperations.set(key, JSON.toJSONString(value));
    }

    /**
     * 缓存对象（JSON 序列化）并设置过期时间
     *
     * @param key    Redis key
     * @param value  对象
     * @param second 过期时间（秒），小于等于 0 表示永久有效
     */
    public void set(String key, Object value, long second) {
        String json = JSON.toJSONString(value);
        if (second > 0) {
            valueOperations.set(key, json, second, TimeUnit.SECONDS);
        } else {
            valueOperations.set(key, json);
        }
    }

    // ==================== 计数器 ====================

    /**
     * 原子自增（INCRBY）
     * <p>
     * 只适用于纯数字的 key；key 不存在时从 0 开始。
     *
     * @param key   Redis key
     * @param delta 增量
     * @return 自增后的值
     */
    public long increment(String key, long delta) {
        Long value = valueOperations.increment(key, delta);
        return value == null ? 0L : value;
    }

    /**
     * 原子自减（DECRBY）
     *
     * @param key   Redis key
     * @param delta 减量
     * @return 自减后的值
     */
    public long decrement(String key, long delta) {
        Long value = valueOperations.decrement(key, delta);
        return value == null ? 0L : value;
    }

    // ==================== Hash ====================

    /**
     * 写入 Hash 的一个字段（值按 JSON 存储）
     *
     * @param key     Redis key
     * @param hashKey Hash 字段名
     * @param value   字段值
     */
    public void mset(String key, String hashKey, Object value) {
        hashOperations.put(key, hashKey, toJson(value));
    }

    /**
     * 批量写入 Hash 字段（值按 JSON 存储）
     *
     * @param key    Redis key
     * @param values 字段名与字段值
     */
    public void msetAll(String key, Map<String, ?> values) {
        if (CollectionUtils.isEmpty(values)) {
            return;
        }
        Map<String, String> jsonMap = new HashMap<>(values.size());
        values.forEach((hashKey, value) -> jsonMap.put(hashKey, toJson(value)));
        hashOperations.putAll(key, jsonMap);
    }

    /**
     * 读取 Hash 字段的原始 JSON 字符串
     *
     * @param key     Redis key
     * @param hashKey Hash 字段名
     * @return JSON 字符串；不存在时返回 null
     */
    public String mget(String key, String hashKey) {
        return hashOperations.get(key, hashKey);
    }

    /**
     * 读取 Hash 字段并反序列化成对象
     *
     * @param key     Redis key
     * @param hashKey Hash 字段名
     * @param clazz   目标类型
     * @param <T>     目标类型
     * @return 反序列化后的值；不存在时返回 null
     */
    public <T> T mget(String key, String hashKey, Class<T> clazz) {
        return parse(mget(key, hashKey), clazz);
    }

    /**
     * 读取整个 Hash 的原始 JSON 字符串
     *
     * @param key Redis key
     * @return 字段名与 JSON 字符串的映射；key 不存在时返回空 Map
     */
    public Map<String, String> mgetAll(String key) {
        Map<String, String> entries = hashOperations.entries(key);
        return entries == null ? Collections.emptyMap() : entries;
    }

    /**
     * 读取整个 Hash 并反序列化成对象
     *
     * @param key   Redis key
     * @param clazz 值的目标类型
     * @param <T>   值的目标类型
     * @return 字段名与对象的映射；key 不存在时返回空 Map
     */
    public <T> Map<String, T> mgetAll(String key, Class<T> clazz) {
        Map<String, String> entries = mgetAll(key);
        if (entries.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, T> result = new HashMap<>(entries.size());
        entries.forEach((hashKey, json) -> result.put(hashKey, parse(json, clazz)));
        return result;
    }

    /**
     * 判断 Hash 中是否存在某个字段
     *
     * @param key     Redis key
     * @param hashKey Hash 字段名
     * @return true 表示存在
     */
    public boolean mhasKey(String key, String hashKey) {
        return Boolean.TRUE.equals(hashOperations.hasKey(key, hashKey));
    }

    /**
     * 删除一个或多个 Hash 字段
     *
     * @param key      Redis key
     * @param hashKeys Hash 字段名
     * @return 实际删除的字段数量
     */
    public long mdelete(String key, String... hashKeys) {
        if (hashKeys == null || hashKeys.length == 0) {
            return 0L;
        }
        Long count = hashOperations.delete(key, (Object[]) hashKeys);
        return count == null ? 0L : count;
    }

    /**
     * 获取 Hash 的所有字段名
     *
     * @param key Redis key
     * @return 字段名集合；key 不存在时返回空集合
     */
    public Set<String> mkeys(String key) {
        Set<String> keys = hashOperations.keys(key);
        return keys == null ? Collections.emptySet() : keys;
    }

    /**
     * 获取 Hash 的字段数量
     *
     * @param key Redis key
     * @return 字段数量
     */
    public long msize(String key) {
        Long size = hashOperations.size(key);
        return size == null ? 0L : size;
    }

    // ==================== List ====================

    /**
     * 从列表左侧批量插入元素（元素按 JSON 存储）
     *
     * @param key    Redis key
     * @param values 元素
     * @return 插入后的列表长度
     */
    public long lpush(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        Long size = listOperations.leftPushAll(key, toJsonArray(values));
        return size == null ? 0L : size;
    }

    /**
     * 从列表右侧批量插入元素（元素按 JSON 存储）
     *
     * @param key    Redis key
     * @param values 元素
     * @return 插入后的列表长度
     */
    public long rpush(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        Long size = listOperations.rightPushAll(key, toJsonArray(values));
        return size == null ? 0L : size;
    }

    /**
     * 从列表左侧弹出元素，返回原始 JSON 字符串
     *
     * @param key Redis key
     * @return JSON 字符串；列表为空时返回 null
     */
    public String lpop(String key) {
        return listOperations.leftPop(key);
    }

    /**
     * 从列表左侧弹出元素并反序列化成对象
     *
     * @param key   Redis key
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 弹出的元素；列表为空时返回 null
     */
    public <T> T lpop(String key, Class<T> clazz) {
        return parse(lpop(key), clazz);
    }

    /**
     * 从列表右侧弹出元素，返回原始 JSON 字符串
     *
     * @param key Redis key
     * @return JSON 字符串；列表为空时返回 null
     */
    public String rpop(String key) {
        return listOperations.rightPop(key);
    }

    /**
     * 从列表右侧弹出元素并反序列化成对象
     *
     * @param key   Redis key
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 弹出的元素；列表为空时返回 null
     */
    public <T> T rpop(String key, Class<T> clazz) {
        return parse(rpop(key), clazz);
    }

    /**
     * 按下标区间读取列表，返回原始 JSON 字符串
     *
     * @param key   Redis key
     * @param start 起始下标（包含），0 表示第一个元素
     * @param end   结束下标（包含），-1 表示最后一个元素
     * @return JSON 字符串列表；区间为空时返回空列表
     */
    public List<String> lrange(String key, long start, long end) {
        List<String> values = listOperations.range(key, start, end);
        return values == null ? Collections.emptyList() : values;
    }

    /**
     * 按下标区间读取列表并反序列化成对象
     *
     * @param key   Redis key
     * @param start 起始下标（包含）
     * @param end   结束下标（包含），-1 表示最后一个元素
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 对象列表；区间为空时返回空列表
     */
    public <T> List<T> lrange(String key, long start, long end, Class<T> clazz) {
        List<String> values = lrange(key, start, end);
        if (values.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>(values.size());
        for (String json : values) {
            result.add(parse(json, clazz));
        }
        return result;
    }

    /**
     * 获取列表长度
     *
     * @param key Redis key
     * @return 列表长度
     */
    public long llen(String key) {
        Long size = listOperations.size(key);
        return size == null ? 0L : size;
    }

    /**
     * 截取列表，只保留 start 到 end 区间的元素
     *
     * @param key   Redis key
     * @param start 起始下标
     * @param end   结束下标，-1 表示最后一个元素
     */
    public void ltrim(String key, long start, long end) {
        listOperations.trim(key, start, end);
    }

    /**
     * 从列表中删除指定元素
     *
     * @param key   Redis key
     * @param count 删除数量：大于 0 从头往尾删，小于 0 从尾往头删，等于 0 删除所有匹配项
     * @param value 要删除的元素（按 JSON 序列化后比较）
     * @return 实际删除的数量
     */
    public long lrem(String key, long count, Object value) {
        Long removed = listOperations.remove(key, count, toJson(value));
        return removed == null ? 0L : removed;
    }

    // ==================== Set ====================

    /**
     * 向集合添加元素（元素按 JSON 存储）
     *
     * @param key    Redis key
     * @param values 元素
     * @return 实际新增的元素数量
     */
    public long sadd(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        Long count = setOperations.add(key, toJsonArray(values));
        return count == null ? 0L : count;
    }

    /**
     * 从集合删除元素
     *
     * @param key    Redis key
     * @param values 元素（按 JSON 序列化后比较）
     * @return 实际删除的元素数量
     */
    public long srem(String key, Object... values) {
        if (values == null || values.length == 0) {
            return 0L;
        }
        Long count = setOperations.remove(key, (Object[]) toJsonArray(values));
        return count == null ? 0L : count;
    }

    /**
     * 获取集合全部元素，返回原始 JSON 字符串
     *
     * @param key Redis key
     * @return JSON 字符串集合；key 不存在时返回空集合
     */
    public Set<String> smembers(String key) {
        Set<String> members = setOperations.members(key);
        return members == null ? Collections.emptySet() : members;
    }

    /**
     * 获取集合全部元素并反序列化成对象
     *
     * @param key   Redis key
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 对象集合；key 不存在时返回空集合
     */
    public <T> Set<T> smembers(String key, Class<T> clazz) {
        Set<String> members = smembers(key);
        if (members.isEmpty()) {
            return Collections.emptySet();
        }
        Set<T> result = new LinkedHashSet<>(members.size());
        for (String json : members) {
            result.add(parse(json, clazz));
        }
        return result;
    }

    /**
     * 判断元素是否在集合中
     *
     * @param key   Redis key
     * @param value 元素（按 JSON 序列化后比较）
     * @return true 表示存在
     */
    public boolean sismember(String key, Object value) {
        return Boolean.TRUE.equals(setOperations.isMember(key, toJson(value)));
    }

    /**
     * 获取集合元素数量
     *
     * @param key Redis key
     * @return 元素数量
     */
    public long scard(String key) {
        Long size = setOperations.size(key);
        return size == null ? 0L : size;
    }

    /**
     * 随机弹出一个集合元素，返回原始 JSON 字符串
     *
     * @param key Redis key
     * @return JSON 字符串；集合为空时返回 null
     */
    public String spop(String key) {
        return setOperations.pop(key);
    }

    /**
     * 随机弹出一个集合元素并反序列化成对象
     *
     * @param key   Redis key
     * @param clazz 元素类型
     * @param <T>   元素类型
     * @return 弹出的元素；集合为空时返回 null
     */
    public <T> T spop(String key, Class<T> clazz) {
        return parse(spop(key), clazz);
    }

    // ==================== JSON 工具 ====================

    private static String toJson(Object value) {
        return JSON.toJSONString(value);
    }

    private static String[] toJsonArray(Object... values) {
        String[] jsonArray = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            jsonArray[i] = toJson(values[i]);
        }
        return jsonArray;
    }

    private static <T> T parse(String json, Class<T> clazz) {
        return json == null ? null : JSON.parseObject(json, clazz);
    }
}
