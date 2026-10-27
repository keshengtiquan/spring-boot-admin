package com.dadaz.admin.module.system.role.domain.dto;

import com.dadaz.base.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建角色入参
 * <p>
 * 只承载"客户端可以决定"的字段。以下字段刻意不在本 DTO 暴露：
 * <ul>
 *   <li>{@code roleId}：数据库自增，客户端无权指定；</li>
 *   <li>{@code createBy} / {@code createTime} / {@code updateBy} / {@code updateTime}：
 *       由 {@code MybatisMetaObjectHandler} 自动填充，客户端传入也会被覆盖。</li>
 * </ul>
 * <p>
 * 两个约定写在字段校验上，而不是靠 Service 里再写一遍：
 * <ul>
 *   <li>{@code roleKey} 只允许小写字母开头 + 小写字母/数字/冒号/下划线。它是鉴权判断依据，
 *       大小写混用会让 {@code hasRole("admin")} 与库里 {@code Admin} 对不上，
 *       而 MySQL 唯一索引本身不区分大小写，所以在入口处统一为小写语义；</li>
 *   <li>可选字段（排序、数据范围、状态）不传时为 null，由 Service 按业务默认值补齐；
 *       校验注解对 null 一律放行，避免"没传"被当成"传了非法值"。</li>
 * </ul>
 * <p>
 * {@code dataScope} 与 {@code deptIds} 的跨字段一致性由 Service 校验：
 * {@code dataScope = 2}（自定义数据权限）时 {@code deptIds} 必填且非空，
 * 其他取值范围不允许传 {@code deptIds}。
 *
 * @author dadaz
 */
@Data
@Schema(name = "RoleCreateDTO", description = "创建角色入参")
public class RoleCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色名称，页面展示用。允许重名，唯一性由 {@code roleKey} 保证
     */
    @Schema(description = "角色名称", example = "普通角色", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称长度不能超过 30 个字符")
    private String roleName;

    /**
     * 角色权限标识，全表唯一，鉴权判断依据
     */
    @Schema(description = "角色权限标识，全表唯一", example = "common", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "角色权限标识不能为空")
    @Size(max = 100, message = "角色权限标识长度不能超过 100 个字符")
    @Pattern(regexp = "^[a-z][a-z0-9_:]*$",
            message = "角色权限标识只能包含小写字母、数字、冒号、下划线，且必须以小写字母开头")
    private String roleKey;

    /**
     * 显示顺序。不传时 Service 按 0 处理
     */
    @Schema(description = "显示顺序，不传默认 0", example = "2")
    @Min(value = 0, message = "显示顺序不能为负数")
    @Max(value = 9999, message = "显示顺序不能超过 9999")
    private Integer roleSort;

    /**
     * 数据范围。不传时 Service 按 1（全部数据权限）处理
     */
    @Schema(description = "数据范围：1 全部、2 自定义、3 本部门、4 本部门及以下、5 仅本人，不传默认 1", example = "1")
    @Pattern(regexp = "^[1-5]$", message = "数据范围取值只能是 1、2、3、4、5")
    private String dataScope;

    /**
     * 角色状态。不传时 Service 按 0（正常）处理
     */
    @Schema(description = "角色状态：0 正常、1 停用，不传默认 0", example = "0")
    @Pattern(regexp = StatusEnum.PATTERN, message = "角色状态取值只能是 0 或 1")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "普通角色")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 自定义数据权限的部门 ID 列表。仅 dataScope = 2 时允许且必须非空
     */
    @Schema(description = "自定义数据权限的部门 ID 列表，仅 dataScope=2 时使用且必须非空", example = "[100, 101]")
    @Size(max = 200, message = "一次最多关联 200 个部门")
    private List<@Positive(message = "部门 ID 必须为正整数") Long> deptIds;
}
