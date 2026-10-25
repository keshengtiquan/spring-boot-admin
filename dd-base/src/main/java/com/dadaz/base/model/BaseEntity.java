package com.dadaz.base.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类：统一承载审计字段（创建人、创建时间、更新人、更新时间）
 * <p>
 * 用法：业务实体继承本类，主键与业务字段各自声明。
 * <pre>
 * public class User extends BaseEntity {
 *     &#64;TableId(type = IdType.AUTO)
 *     private Long id;
 *     private String username;
 *     // ...
 * }
 * </pre>
 * <p>
 * 字段值由 {@code MybatisMetaObjectHandler} 在插入/更新时自动填充，
 * 业务代码不需要、也不应该手工赋值。
 * <p>
 * 说明：
 * <ul>
 *   <li>列名显式写在 {@code @TableField(value = ...)} 上，不依赖 map-underscore-to-camel-case 配置；</li>
 *   <li>时间使用 {@link LocalDateTime}，与 MySQL {@code datetime} 对应，避免 Date 的时区歧义；</li>
 *   <li>{@code @JsonFormat} 让接口出参与入参统一为 {@code yyyy-MM-dd HH:mm:ss}，
 *       否则 Jackson 默认会输出 ISO 格式的 {@code 2026-10-20T13:15:47}；</li>
 *   <li>访问器与 {@code toString} 由 Lombok 的 {@link Getter} / {@link Setter} / {@link ToString} 生成。
 *       这里刻意<b>不使用 {@code @Data}</b>：它会顺带生成 {@code equals/hashCode}，
 *       使「审计字段相同」的两条不同记录被判为相等，破坏实体语义
 *       （MyBatis 缓存、{@code Set}/{@code Map} 去重、比较逻辑都可能受影响）。
 *       确需按主键比较时，由子类自行声明 {@code @EqualsAndHashCode(of = "id")}。</li>
 * </ul>
 *
 * @author dadaz
 */
@Getter
@Setter
@ToString
@Schema(description = "实体基类：审计字段")
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建人。存登录账号标识，未登录上下文（定时任务、初始化数据）填充 {@code system}
     */
    @Schema(description = "创建人", example = "admin")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-10-20 13:15:47")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人。首次插入时与创建人一致
     */
    @Schema(description = "更新人", example = "admin")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * 更新时间。首次插入时与创建时间一致
     */
    @Schema(description = "更新时间", example = "2026-10-20 13:15:47")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
