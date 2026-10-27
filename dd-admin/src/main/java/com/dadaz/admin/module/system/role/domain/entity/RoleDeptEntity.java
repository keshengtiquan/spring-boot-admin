package com.dadaz.admin.module.system.role.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色与部门关联实体，对应表 {@code sys_role_dept}
 * <p>
 * 这张表只在角色的 {@code data_scope = '2'}（自定义数据权限）时才有数据，
 * 表示"该角色能看到哪些部门的数据"。其他数据范围（全部/本部门/本部门及以下/仅本人）
 * 分别由角色自身、用户所属部门、部门 ancestors 或个人身份推导，不读本表。
 * <p>
 * 纯关联表：没有自增 ID、没有审计字段，{@code (role_id, dept_id)} 两列合起来就是主键。
 * <ul>
 *   <li>不继承 {@link com.dadaz.base.model.BaseEntity}：关联行没有"谁创建的"语义；</li>
 *   <li>没有 {@code @TableId}：{@code selectById / deleteById} 这类单主键方法不适用，
 *       按角色或部门维度的操作请用条件构造器；</li>
 *   <li>{@code @Data} 的 equals/hashCode 正好按两列比较，等价于联合主键语义。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@TableName("sys_role_dept")
@Schema(name = "RoleDeptEntity", description = "角色与部门关联（自定义数据权限）")
public class RoleDeptEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID", example = "2")
    @TableField("role_id")
    private Long roleId;

    /**
     * 部门 ID
     */
    @Schema(description = "部门 ID", example = "100")
    @TableField("dept_id")
    private Long deptId;
}
