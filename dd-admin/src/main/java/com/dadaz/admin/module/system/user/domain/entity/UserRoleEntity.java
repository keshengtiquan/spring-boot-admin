package com.dadaz.admin.module.system.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户与角色关联实体，对应表 {@code sys_user_role}
 * <p>
 * 纯关联表：没有自增 ID、没有审计字段，{@code (user_id, role_id)} 两列合起来就是主键。
 * <ul>
 *   <li>不继承 {@link com.dadaz.base.model.BaseEntity}：关联行没有"谁创建的"语义，
 *       授权审计属于操作日志，不属于本行；</li>
 *   <li>没有 {@code @TableId}：MyBatis-Plus 的单主键 API（selectById / deleteById）
 *       在这里不适用，按用户或角色维度的操作请用条件构造器；
 *       例如删除某用户的全部角色：{@code delete(new LambdaQueryWrapper<UserRoleEntity>()
 *       .eq(UserRoleEntity::getUserId, userId))}；</li>
 *   <li>这里用 {@code @Data} 是合适的：equals/hashCode 正好按两列比较，
 *       等价于联合主键语义，放进 Set 去重也正是业务想要的效果。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@TableName("sys_user_role")
@Schema(name = "UserRoleEntity", description = "用户与角色关联")
public class UserRoleEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    @TableField("user_id")
    private Long userId;

    /**
     * 角色 ID
     */
    @Schema(description = "角色 ID")
    @TableField("role_id")
    private Long roleId;
}
