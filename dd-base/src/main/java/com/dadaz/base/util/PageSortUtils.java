package com.dadaz.base.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dadaz.base.enums.ResultCode;
import com.dadaz.base.exception.BusinessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 分页排序工具：把前端传的 {@code orderByColumn} / {@code isAsc} 转成 MyBatis-Plus 的 {@link OrderItem}
 * <p>
 * 参数名和格式按前端表格组件的常见约定设计，便于直接透传：
 * <pre>
 * ?orderByColumn=userName,createTime&amp;isAsc=asc,desc   → ORDER BY user_name ASC, create_time DESC
 * ?orderByColumn=userName&amp;isAsc=descending             → ORDER BY user_name DESC
 * ?orderByColumn=userId,createTime&amp;isAsc=desc           → ORDER BY user_id DESC, create_time DESC（方向广播）
 * </pre>
 * 之所以还要这一层，是因为 ORDER BY 的列名是<b>拼进 SQL 的字符串</b>，不能直接用前端输入。
 * 这里多做一步实体字段白名单，处理顺序是：
 * <ol>
 *   <li><b>标识符校验</b>：每个字段名必须匹配 {@code ^[A-Za-z][A-Za-z0-9_]*$}，
 *       分号、空格、引号、括号、点号等注入常见字符在第一步就被拒掉；</li>
 *   <li><b>方向校验</b>：只接受 asc/desc（兼容 ascending/descending，忽略大小写），
 *       不接受 {@code asc;--} 这类变体；多列时方向数可以是 1（广播）或与列数一致；</li>
 *   <li><b>实体字段白名单</b>：字段名必须是该实体真实存在的属性或列（从 MyBatis-Plus 的
 *       {@link TableInfo} 元数据里取），字段写错直接 400，而不是把错误 SQL 丢给数据库；</li>
 *   <li><b>属性转列名</b>：前端传 {@code createTime}，实际排序用 {@code create_time}；
 *       最终交给 ORDER BY 的是元数据里的列名，<b>前端字符串永远不会原样进入 SQL</b>。</li>
 * </ol>
 * <p>
 * 一般不需要直接调用：{@code com.dadaz.base.model.PageParam#build(Class)} 已经把它封装成
 * "一行构造带排序的 Page"。只有写不经过 PageParam 的自定义查询时才需要直接使用：
 * <pre>
 * Page&lt;UserEntity&gt; page = Page.of(1, 10);
 * page.addOrder(PageSortUtils.resolveOrDefault(UserEntity.class, "createTime", "desc"));
 * </pre>
 * 排序放在 {@link com.baomidou.mybatisplus.core.metadata.IPage#orders()} 上而不是 QueryWrapper 上：
 * 分页插件会把 page 的 orders 拼到最终 SQL 的 ORDER BY，这样单表查询和后续的自定义联表 SQL
 * 都能复用同一套排序规则，也不用为了排序把 LambdaQueryWrapper 换成字符串拼接的 QueryWrapper。
 *
 * @author dadaz
 */
public final class PageSortUtils {

    /**
     * 合法字段名（标识符）。要求字母开头，只允许字母、数字、下划线
     */
    private static final Pattern FIELD_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    /**
     * 升序关键字
     */
    private static final String ASC = "asc";

    /**
     * 降序关键字
     */
    private static final String DESC = "desc";

    private PageSortUtils() {
    }

    /**
     * 解析排序参数，非法输入直接抛业务异常（HTTP 200 + 响应体 code=400）
     *
     * @param entityClass    实体类型，用于取字段白名单
     * @param orderByColumn  排序列，多列用英文逗号分隔，可为 null / 空白
     * @param isAsc          排序方向，多列用英文逗号分隔，可为 null / 空白（视为 asc）
     * @return 排序项列表；没传排序列时返回空列表
     * @throws BusinessException 字段非法、方向非法或字段不在白名单时抛出
     */
    public static List<OrderItem> resolve(Class<?> entityClass, String orderByColumn, String isAsc) {
        List<String> fields = split(orderByColumn);
        if (fields.isEmpty()) {
            return List.of();
        }

        List<String> directions = toDirections(isAsc, fields.size());
        Map<String, String> columnMap = columnMap(entityClass);

        List<OrderItem> orders = new ArrayList<>(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            // 只有一个方向时作用于所有列，多个方向时与列一一对应
            String direction = directions.size() == 1 ? directions.get(0) : directions.get(i);

            if (!FIELD_PATTERN.matcher(field).matches()) {
                throw invalid("排序字段包含非法字符：" + field);
            }
            String column = columnMap.get(field);
            if (column == null) {
                throw invalid("不支持的排序字段：" + field);
            }

            orders.add(DESC.equals(direction) ? OrderItem.desc(column) : OrderItem.asc(column));
        }
        return orders;
    }

    /**
     * 解析排序参数；未传排序时回退到实体主键倒序
     * <p>
     * 主键倒序是大多数列表页需要的默认顺序（新数据在前），且走索引，比按创建时间排序更稳定
     * （创建时间相同的记录顺序不确定，翻页时可能重复或漏数据）。
     *
     * @param entityClass   实体类型
     * @param orderByColumn 排序列，可为 null / 空白
     * @param isAsc         排序方向，可为 null / 空白
     * @return 排序项列表，一定非空
     */
    public static List<OrderItem> resolveOrDefault(Class<?> entityClass, String orderByColumn, String isAsc) {
        List<OrderItem> orders = resolve(entityClass, orderByColumn, isAsc);
        return orders.isEmpty() ? List.of(defaultOrder(entityClass)) : orders;
    }

    /**
     * 默认排序：实体主键倒序
     *
     * @param entityClass 实体类型
     * @return 主键倒序的排序项
     */
    public static OrderItem defaultOrder(Class<?> entityClass) {
        return OrderItem.desc(tableInfo(entityClass).getKeyColumn());
    }

    /**
     * 解析方向列表：空视为 asc；数量只能是 1（广播）或与列数一致；ascending/descending 归一化为 asc/desc
     */
    private static List<String> toDirections(String isAsc, int fieldCount) {
        List<String> rawDirections = split(isAsc);
        if (rawDirections.isEmpty()) {
            return Collections.nCopies(fieldCount, ASC);
        }
        List<String> directions = new ArrayList<>(rawDirections.size());
        for (String raw : rawDirections) {
            String direction = normalizeDirection(raw);
            if (direction == null) {
                throw invalid("排序方向只能是 asc 或 desc，实际收到：" + raw);
            }
            directions.add(direction);
        }
        if (directions.size() != 1 && directions.size() != fieldCount) {
            throw invalid("排序字段与排序方向数量不一致：字段 " + fieldCount + " 个，方向 "
                    + directions.size() + " 个");
        }
        return directions;
    }

    /**
     * 方向归一化：asc / ascending → asc，desc / descending → desc，其余返回 null
     */
    private static String normalizeDirection(String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "asc", "ascending" -> ASC;
            case "desc", "descending" -> DESC;
            default -> null;
        };
    }

    /**
     * 按英文逗号拆分并去空白，空串直接返回空列表
     */
    private static List<String> split(String value) {
        if (StringUtils.isBlank(value)) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        for (String token : value.split(",")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    /**
     * 取实体元数据；拿不到说明该类不是 MyBatis-Plus 实体（没有 @TableName）或 Mapper 尚未初始化
     */
    private static TableInfo tableInfo(Class<?> entityClass) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(entityClass);
        if (tableInfo == null) {
            throw new BusinessException(ResultCode.FAIL, "未找到实体映射信息：" + entityClass.getName());
        }
        return tableInfo;
    }

    /**
     * 构建「属性名 → 列名」映射，同时把列名本身也作为 key，方便前端直接传下划线列名
     */
    private static Map<String, String> columnMap(Class<?> entityClass) {
        TableInfo tableInfo = tableInfo(entityClass);
        Map<String, String> map = new HashMap<>((tableInfo.getFieldList().size() + 1) * 2);
        map.put(tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
        map.put(tableInfo.getKeyColumn(), tableInfo.getKeyColumn());
        for (TableFieldInfo field : tableInfo.getFieldList()) {
            map.put(field.getProperty(), field.getColumn());
            map.put(field.getColumn(), field.getColumn());
        }
        return map;
    }

    /**
     * 构造参数错误异常（响应体 code=400）
     */
    private static BusinessException invalid(String message) {
        return new BusinessException(ResultCode.PARAM_ERROR, message);
    }
}
