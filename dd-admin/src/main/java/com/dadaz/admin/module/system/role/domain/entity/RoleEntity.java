package com.dadaz.admin.module.system.role.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dadaz.base.model.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;

/**
 * 系统角色实体，对应表 {@code sys_role}
 * <p>
 * 字段来源：{@code sys_role} 通用结构（与 {@code sys_user} 同源），按当前项目约定做了四点调整：
 * <ul>
 *   <li>审计字段（create_by / create_time / update_by / update_time）由 {@link BaseEntity} 承载，
 *       由 {@code MybatisMetaObjectHandler} 自动填充，本类不再重复声明；</li>
 *   <li><b>没有 {@code del_flag}</b>：很多后台系统的角色/用户表靠 {@code del_flag} 做逻辑删除，
 *       但本项目的 {@code sys_user} 已经去掉了这一列，统一采用物理删除。
 *       这里的取舍是「少一个必须处处记得过滤的字段」——逻辑删除一旦漏加
 *       {@code del_flag = '0'}，被删除的角色仍会出现在分配列表里，属于权限隐患。
 *       若后续确实需要逻辑删除，请连同 {@code sys_user} 一起改，并给本类加
 *       {@code @TableLogic}，不要只给单表加；</li>
 *   <li><b>没有 {@code menu_check_strictly} / {@code dept_check_strictly}</b>：这两个字段只服务于
 *       角色分配页"菜单/部门树父子联动勾选"的配置，本项目不需要这一交互，故不建字段、不建列；
 *       将来若要做，再按 {@code Boolean} + {@code tinyint(1) NOT NULL DEFAULT 1} 成对补上；</li>
 *   <li>列名一律显式写在 {@code @TableField(value = ...)} 上，不依赖驼峰自动映射。</li>
 * </ul>
 * <p>
 * 注解说明：类上用 {@code @Data} 简写，省掉逐个声明 getter/setter 的样板代码；
 * 但 {@code @Data} 自带的 {@code @EqualsAndHashCode} / {@code @ToString} 在这里不合适，
 * 因此两者都被显式覆盖：
 * <ul>
 *   <li>{@code @EqualsAndHashCode(callSuper = false, of = "roleId")}：实体应当「同一行数据即同一对象」，
 *       只按主键判等，插入后 hashCode 不再变化。注意<b>不能</b>写成 {@code callSuper = true}——
 *       {@link BaseEntity} 并没有重写 equals/hashCode，Lombok 只会生成 {@code super.equals(o)} /
 *       {@code super.hashCode()}，实际落到 {@code Object} 的引用比较，
 *       结果是两条"同一行"的实体永远不相等，放进 {@code Set}/{@code Map} 也无法去重；</li>
 *   <li>{@code @ToString(callSuper = true)}：{@code @Data} 的 toString 默认不打印父类字段，
 *       显式打开后日志里才能看到 createBy/updateTime 等审计字段。
 *       本类没有密码一类敏感字段，因此不需要 {@code @ToString.Exclude}。</li>
 * </ul>
 * <p>
 * 取值约定（沿用 {@code sys_role} 结构的既有取值；这些值是字典回显、数据范围过滤的依据，
 * 改动前必须同步所有读取方）：
 * <table border="1">
 *   <caption>枚举型字段取值</caption>
 *   <tr><th>字段</th><th>取值</th></tr>
 *   <tr><td>status</td><td>0 正常、1 停用（复用 {@code StatusEnum}）</td></tr>
 *   <tr><td>data_scope</td>
 *       <td>1 全部数据权限、2 自定义数据权限、3 本部门数据权限、
 *           4 本部门及以下数据权限、5 仅本人数据权限</td></tr>
 * </table>
 * <p>
 * 数据库侧职责（本类无法表达，需在 DDL 中保证）：
 * <ul>
 *   <li>{@code role_key} 唯一索引：它是鉴权判断依据，重复会让"谁能访问"变得不可预期；</li>
 *   <li>{@code role_sort}、{@code data_scope}、{@code status} 的默认值由 DDL 的 DEFAULT 兜底，
 *       插入时未赋值也不会触发非空约束；</li>
 *   <li>角色与用户、角色与菜单是多对多关系，落在 {@code sys_user_role} / {@code sys_role_menu}
 *       关联表里，不在本表加冗余列。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "roleId")
@TableName("sys_role")
@Schema(name = "RoleEntity", description = "系统角色")
public class RoleEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID，主键。数据库自增，插入时不需要赋值
     * <p>
     * 约定保留两个固定 ID：{@code 1} 超级管理员、{@code 2} 普通角色。
     * 很多实现会把 {@code role_id = 1} 当作"绕过权限校验"的硬编码判断，
     * 请勿把这部分 ID 挪作他用（详见 {@code sql/02_sys_role.sql} 的说明）
     */
    @Schema(description = "角色 ID", example = "2")
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色名称，页面展示用。允许重名（两个"运营"是正常业务），
     * 真正需要唯一的是下面的 {@code roleKey}
     */
    @Schema(description = "角色名称", example = "普通角色")
    @TableField("role_name")
    private String roleName;

    /**
     * 角色权限字符串，鉴权判断依据（如 Sa-Token 的 {@code hasRole("admin")}）。
     * <p>
     * 约定只用小写字母、数字和冒号/下划线，长度不超过 100；
     * 它是"角色标识"而非"权限点"，权限点由菜单/按钮的 {@code perms} 表达。
     * 表上建了唯一索引，重复插入会抛 {@code DuplicateKeyException}，调用方需捕获并翻译成业务提示
     */
    @Schema(description = "角色权限字符串，全表唯一", example = "common")
    @TableField("role_key")
    private String roleKey;

    /**
     * 显示顺序，升序排列（数值越小越靠前）。
     * <p>
     * 用包装类型 {@code Integer} 而不是 {@code int}：未传时保持 null，
     * MyBatis-Plus 默认忽略 null 字段，由 DDL 的 {@code DEFAULT 0} 兜底，
     * 不会把"没传"变成"显式 0"覆盖数据库默认值
     */
    @Schema(description = "显示顺序，越小越靠前", example = "2")
    @TableField("role_sort")
    private Integer roleSort;

    /**
     * 数据范围，决定该角色在列表类接口里能看到哪些数据（数据权限过滤的通用语义）：
     * 1 全部、2 自定义、3 本部门、4 本部门及以下、5 仅本人。
     * <p>
     * 取值 2（自定义）时，具体能看哪些部门由 {@code sys_role_dept} 关联表决定
     */
    @Schema(description = "数据范围：1 全部、2 自定义、3 本部门、4 本部门及以下、5 仅本人", example = "1")
    @TableField("data_scope")
    private String dataScope;

    /**
     * 角色状态。0 正常、1 停用，取值复用 {@code StatusEnum}。
     * 停用角色不应再参与任何鉴权判断，登录后组装权限时需过滤
     */
    @Schema(description = "角色状态：0 正常、1 停用", example = "0")
    @TableField("status")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "普通角色")
    @TableField("remark")
    private String remark;
}
