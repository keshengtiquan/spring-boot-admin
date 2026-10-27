package com.dadaz.base.model;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dadaz.base.util.PageSortUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分页入参基类
 * <p>
 * 分页参数的两类约束统一在本类收口，避免各业务 Service 各写一套默认值和上限：
 * <ul>
 *   <li><b>默认值</b>：{@code pageNum} / {@code pageSize} 用字段初始值给出 1 / 10。
 *       页面不传时 Spring 不会覆盖字段，拿到的就是初始值，Service 里不必再判 null 兜底；</li>
 *   <li><b>非法值</b>：{@code @Min} / {@code @Max} 直接拒绝并返回 400（如页码 0、每页 1001），
 *       不做静默纠正 —— 前端传错就报错，比悄悄改成第 1 页更容易定位问题。</li>
 * </ul>
 * <p>
 * {@code @NotNull} 针对的是「显式传空值」：{@code ?pageNum=} 会把 Long 绑成 null，
 * 此时字段初始值已被覆盖，而 {@code @Min} 不校验 null；没有 {@code @NotNull}，
 * 后续 {@code Page.of(pageNum, pageSize)} 拆箱时就会 NPE 变成 500。
 *
 * @author dadaz
 */
@Data
public class PageParam {

    /**
     * 默认页码：页面不传 pageNum 时使用
     */
    public static final long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页条数：页面不传 pageSize 时使用
     */
    public static final long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 每页条数上限，同时作为 {@link #pageSize} 上 {@code @Max} 的取值，
     * 保证"校验用的上限"和文档里写的上限永远是同一个数
     */
    public static final long MAX_PAGE_SIZE = 1000L;

    @Schema(description = "页码，不传默认 1", example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码最小为 1")
    private Long pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页数量，不传默认 10，最大 1000", example = "10")
    @NotNull(message = "每页数量不能为空")
    @Min(value = 1, message = "每页数量最小为 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页最大为 {value}")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 排序列，多个字段用英文逗号分隔，例如 {@code userName,createTime}
     * <p>
     * 字段名取实体属性名（如 {@code createTime}），也兼容数据库列名（如 {@code create_time}）；
     * 参数名取 {@code orderByColumn}，前端表格组件可直接透传。
     * <p>
     * <b>不要直接把这里的值拼进 SQL</b>：交给 {@code com.dadaz.base.util.PageSortUtils}
     * 做格式校验与实体字段白名单转换，最终进入 ORDER BY 的只能是实体元数据里的列名。
     */
    @Schema(description = "排序列，多列用英文逗号分隔；取实体属性名，如 userName,createTime", example = "createTime")
    private String orderByColumn;

    /**
     * 排序方向：{@code asc} / {@code desc}，多个方向用英文逗号分隔，与 {@link #orderByColumn} 的列一一对应；
     * 只传一个方向时作用于所有排序列。兼容 {@code ascending} / {@code descending} 两种常见写法，
     * 也兼容 {@code isAsc} 这一参数名。不传时默认 {@code asc}。
     */
    @Schema(description = "排序方向：asc/desc（兼容 ascending/descending），多列用英文逗号分隔，只有一个方向时作用于所有排序列", example = "desc")
    private String isAsc;

    /**
     * 构建 MyBatis-Plus 分页对象（分页参数 + 排序一次搞定），业务查询直接拿去用
     * <p>
     * 把这一步收在参数对象里的原因：页码、每页条数、排序本来就是"同一次分页请求"的三部分，
     * 让 {@code PageParam} 自己产出 {@link Page} 后，业务 Service 只写一行：
     * <pre>
     * Page&lt;UserEntity&gt; page = query.build(UserEntity.class);
     * page(entityPage, wrapper);
     * </pre>
     * 不用每个列表接口都重复 {@code new Page(...)} + {@code addOrder(...)}。
     * <p>
     * 排序字段仍然交给 {@link PageSortUtils} 做标识符校验、实体字段白名单和属性转列名：
     * 字段写错或方向非法会抛业务异常（响应体 code=400）；不传排序时回退到实体主键倒序。
     * <p>
     * 调用前提：{@code pageNum} / {@code pageSize} 非空且合法。走 Web 层时由
     * {@code @NotNull} / {@code @Min} / {@code @Max} 保证；代码里直接构造时字段初始值兜底。
     *
     * @param entityClass 实体类型，用于取字段白名单与主键列
     * @param <T>         实体类型
     * @return 带分页参数和排序的分页对象
     */
    public <T> Page<T> build(Class<T> entityClass) {
        Page<T> page = Page.of(pageNum, pageSize);
        page.addOrder(PageSortUtils.resolveOrDefault(entityClass, orderByColumn, isAsc));
        return page;
    }
}
