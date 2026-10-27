package com.dadaz.admin.module.system.role.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 给角色分配菜单入参
 * <p>
 * 语义是<b>全量覆盖</b>：本次传入的菜单列表就是该角色最终拥有的全部菜单。
 * <ul>
 *   <li>传 {@code []}：清空该角色的所有菜单权限；</li>
 *   <li>不传（null）：视为非法入参，用 {@code []} 表达"清空"，避免 null 与空数组两种语义混淆；</li>
 *   <li>列表里包含目录（M）、菜单（C）、按钮（F）的 ID，前端菜单树的勾选结果整体传入。</li>
 * </ul>
 * 重复 ID 由 Service 去重；不存在的菜单 ID 会整体拒绝，不会产生半成品授权。
 *
 * @author dadaz
 */
@Data
@Schema(name = "RoleMenuAssignDTO", description = "给角色分配菜单入参")
public class RoleMenuAssignDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID 列表，空数组表示清空该角色的菜单权限
     */
    @Schema(description = "菜单 ID 列表；空数组表示清空该角色的菜单权限",
            example = "[2000, 2001, 2002, 2003, 2004]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单 ID 列表不能为空；清空权限请传空数组")
    @Size(max = 1000, message = "一次最多分配 1000 个菜单")
    private List<@Positive(message = "菜单 ID 必须为正整数") Long> menuIds;
}
