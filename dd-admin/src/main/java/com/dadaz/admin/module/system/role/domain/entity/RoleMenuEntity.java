package com.dadaz.admin.module.system.role.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 角色与菜单关联实体，对应表 {@code sys_role_menu}
 * <p>
 * 多对多关系：一个角色拥有多个菜单（目录/菜单/按钮），一个菜单可被多个角色拥有。
 * <ul>
 *   <li>不继承 {@link com.dadaz.base.model.BaseEntity}：关联行没有"谁创建的"语义，
 *       授权审计属于操作日志；</li>
 *   <li>没有 {@code @TableId}：联合主键 {@code (role_id, menu_id)}，
 *       {@code selectById / deleteById} 这类单主键方法不适用，
 *       按角色维度的操作请用条件构造器；</li>
 *   <li>{@code @Data} 的 equals/hashCode 正好按两列比较，等价于联合主键语义。</li>
 * </ul>
 * 本表与数据权限表 {@code sys_role_dept} 职责不同：本表控制"能看到哪些菜单/按钮"，
 * 后者控制"能看到哪些部门的数据"。
 *
 * @author dadaz
 */
@Data
@TableName("sys_role_menu")
@Schema(name = "RoleMenuEntity", description = "角色与菜单关联")
public class RoleMenuEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID", example = "2")
    @TableField("role_id")
    private Long roleId;

    /**
     * 菜单 ID（目录/菜单/按钮都算）
     */
    @Schema(description = "菜单 ID", example = "2001")
    @TableField("menu_id")
    private Long menuId;
}
