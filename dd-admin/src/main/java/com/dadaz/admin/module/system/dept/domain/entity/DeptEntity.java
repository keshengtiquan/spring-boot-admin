package com.dadaz.admin.module.system.dept.domain.entity;

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
 * 系统部门实体，对应表 {@code sys_dept}
 * <p>
 * 字段来源：{@code sys_dept} 通用结构（与 {@code sys_user} / {@code sys_role} 同源），
 * 按当前项目约定做了四点调整：
 * <ul>
 *   <li>审计字段（create_by / create_time / update_by / update_time）由 {@link BaseEntity} 承载，
 *       由 {@code MybatisMetaObjectHandler} 自动填充，本类不再重复声明；</li>
 *   <li><b>没有 {@code del_flag}</b>：与 {@code sys_user} / {@code sys_role} 一致，统一物理删除；</li>
 *   <li><b>{@code ancestors} 由 Service 维护</b>：新增部门时按父部门拼出，不开放给客户端。
 *       移动部门时要级联更新整棵子树，属于服务端职责，不能交给前端传值；</li>
 *   <li>列名一律显式写在 {@code @TableField(value = ...)} 上，不依赖驼峰自动映射。</li>
 * </ul>
 * <p>
 * 树结构约定（全表统一，不要各写一套）：
 * <ul>
 *   <li>顶级部门的 {@code parent_id = 0}，{@code ancestors = "0"}；</li>
 *   <li>子部门的 {@code ancestors} = 父部门 ancestors + {@code ","} + 父部门 ID，
 *       例如 100 → 101 → 1011，三代部门的 ancestors 分别是 {@code "0"}、{@code "0,100"}、
 *       {@code "0,100,101"}；</li>
 *   <li>按"本部门及以下"过滤时用 {@code FIND_IN_SET(dept_id, ancestors)} 或
 *       {@code ancestors LIKE '0,100,%'}，不要把 ancestors 当普通字符串截取。</li>
 * </ul>
 * <p>
 * 注解说明（与 {@link com.dadaz.admin.module.system.role.domain.entity.RoleEntity} 一致）：
 * {@code @Data} 负责 getter/setter，{@code @EqualsAndHashCode(of = "deptId")} 只按主键判等，
 * {@code @ToString(callSuper = true)} 让日志带上审计字段。注意不要把 equals 改成
 * {@code callSuper = true}——{@link BaseEntity} 没有重写 equals/hashCode，
 * 那会退化成引用比较，同一行记录永远不相等。
 * <p>
 * 数据库侧职责（本类无法表达，需在 DDL 中保证）：
 * <ul>
 *   <li>{@code (parent_id, dept_name)} 唯一索引：同一父部门下不允许重名，并发创建由数据库兜底；</li>
 *   <li>{@code status} 普通索引：列表页高频过滤条件；</li>
 *   <li>{@code parent_id} 的查询由上面的联合唯一索引前缀覆盖，不再单独建索引。</li>
 * </ul>
 *
 * @author dadaz
 */
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "deptId")
@TableName("sys_dept")
@Schema(name = "DeptEntity", description = "系统部门")
public class DeptEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门 ID，主键。数据库自增，插入时不需要赋值
     */
    @Schema(description = "部门 ID", example = "100")
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;

    /**
     * 父部门 ID。0 表示顶级部门；Service 负责"父部门必须存在"的校验
     */
    @Schema(description = "父部门 ID，0 表示顶级部门", example = "0")
    @TableField("parent_id")
    private Long parentId;

    /**
     * 祖级列表，英文逗号分隔，不含自身。由 Service 在新增/移动时维护，客户端不可指定
     */
    @Schema(description = "祖级列表，逗号分隔，如 0,100,101", example = "0,100")
    @TableField("ancestors")
    private String ancestors;

    /**
     * 部门名称。同一父部门下唯一（DB 联合唯一索引兜底），页面展示用
     */
    @Schema(description = "部门名称", example = "研发部")
    @TableField("dept_name")
    private String deptName;

    /**
     * 显示顺序，升序排列（数值越小越靠前）。未传时由 Service 补 0
     */
    @Schema(description = "显示顺序，越小越靠前", example = "1")
    @TableField("order_num")
    private Integer orderNum;

    /**
     * 负责人
     */
    @Schema(description = "负责人", example = "张三")
    @TableField("leader")
    private String leader;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "13800138000")
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "rd@dadaz.com")
    @TableField("email")
    private String email;

    /**
     * 部门状态。0 正常、1 停用，取值复用 {@code StatusEnum}。
     * 停用部门不应再被新用户/子部门引用，业务层需自行过滤
     */
    @Schema(description = "部门状态：0 正常、1 停用", example = "0")
    @TableField("status")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "研发部")
    @TableField("remark")
    private String remark;
}
