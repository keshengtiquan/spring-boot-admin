package com.dadaz.admin.module.system.user.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dadaz.base.model.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统用户实体，对应表 {@code sys_user}
 * <p>
 * 字段来源：若依 {@code sys_user} 标准结构，按当前项目约定做了两点调整：
 * <ul>
 *   <li>审计字段（create_by / create_time / update_by / update_time）由 {@link BaseEntity} 承载，
 *       由 {@code MybatisMetaObjectHandler} 自动填充，本类不再重复声明；</li>
 *   <li>列名一律显式写在 {@code @TableField(value = ...)} 上，不依赖驼峰自动映射。</li>
 * </ul>
 * <p>
 * 为什么不用 {@code @Data}（{@code @Data} = Getter + Setter + RequiredArgsConstructor + ToString + EqualsAndHashCode）：
 * <ol>
 *   <li><b>判等语义错了。</b>{@code @Data} 附带的 {@code @EqualsAndHashCode} 默认 {@code callSuper = false}，
 *       父类 {@link BaseEntity} 的审计字段<b>完全不参与</b>比较（编译时会告警
 *       "Generating equals/hashCode implementation but without a call to superclass"）。
 *       它还会遍历全部业务字段（含 password、avatar、remark），等于把实体当成值对象。
 *       实体应该"同一行数据即同一对象"，因此这里显式按主键比较：{@code of = "userId"}</li>
 *   <li><b>hashCode 不稳定。</b>全字段 hashCode 依赖可变字段，实体一旦被修改，
 *       已放进 {@code HashSet} 或作为 {@code HashMap} key 的对象会"查不到自己"。
 *       只按 userId 算，插入后就不会再变</li>
 *   <li><b>toString 会把密码写进日志。</b>{@code @Data} 的 {@code @ToString} 默认 {@code callSuper = false}，
 *       既看不到审计字段，也无法选择性脱敏。这里用 {@code @ToString(callSuper = true)}
 *       并在 password 上加 {@code @ToString.Exclude}</li>
 *   <li><b>构造器隐患。</b>{@code @Data} 会生成 {@code @RequiredArgsConstructor}。当前没有
 *       final / {@code @NonNull} 字段，生成的是无参构造，暂时无害；但将来一旦加了 final 字段，
 *       隐式无参构造会消失，MyBatis 反射实例化和 Jackson 反序列化会直接失败</li>
 * </ol>
 * 一句话：{@code @Data} 是"四件事打包"的快捷方式，适合纯 DTO / VO；
 * 带父类、带敏感字段、需要明确判等规则的持久化实体应当逐个声明。
 * <p>
 * 取值约定（与前端 RuoYi-Element 保持一致，改为别的值会导致字典回显错乱）：
 * <table border="1">
 *   <caption>枚举型字段取值</caption>
 *   <tr><th>字段</th><th>取值</th></tr>
 *   <tr><td>user_type</td><td>00 系统用户、01 其他注册用户</td></tr>
 *   <tr><td>sex</td><td>0 男、1 女、2 未知</td></tr>
 *   <tr><td>status</td><td>0 正常、1 停用</td></tr>
 * </table>
 * <p>
 * 数据库侧职责（本类无法表达，需在 DDL 中保证）：
 * <ul>
 *   <li>{@code user_name} 唯一索引，防止并发注册出现同名账号；</li>
 *   <li>业务字段的默认值（user_type='00'、sex='0'、status='0'）
 *       由 DDL 的 DEFAULT 兜底，插入时显式指定默认值也可；</li>
 *   <li>{@code dept_id} 建普通索引，按部门检索用户时会用到。</li>
 * </ul>
 *
 * @author dadaz
 */
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, of = "userId")
@TableName("sys_user")
@Schema(name = "UserEntity", description = "系统用户")
public class UserEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID，主键。数据库自增，插入时不需要赋值
     */
    @Schema(description = "用户 ID", example = "1")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 所属部门 ID。允许为空：超管等不归属具体部门的账号 dept_id 为 null；
     * 更新时 {@code FieldStrategy.ALWAYS}，否则 MyBatis-Plus 默认忽略 null，
     * 前端把部门清空的操作会变成静默失败
     */
    @Schema(description = "所属部门 ID", example = "100")
    @TableField(value = "dept_id", updateStrategy = FieldStrategy.ALWAYS)
    private Long deptId;

    /**
     * 登录账号，登录凭证，全局唯一。存库前需 trim，避免 "admin" 与 "admin " 被当成两个账号
     */
    @Schema(description = "登录账号", example = "admin")
    @TableField("user_name")
    private String userName;

    /**
     * 用户昵称，页面展示用。允许与登录账号不同，便于展示中文姓名
     */
    @Schema(description = "用户昵称", example = "张三")
    @TableField("nick_name")
    private String nickName;

    /**
     * 用户类型。00 系统用户、01 其他注册用户
     */
    @Schema(description = "用户类型：00 系统用户、01 其他注册用户", example = "00")
    @TableField("user_type")
    private String userType;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "admin@dadaz.com")
    @TableField("email")
    private String email;

    /**
     * 手机号。注意表列名是 {@code phonenumber}（无下划线，若依原始结构如此），
     * 而 Java 属性按驼峰命名为 phoneNumber，因此必须显式指定列名
     */
    @Schema(description = "手机号", example = "13800138000")
    @TableField("phonenumber")
    private String phoneNumber;

    /**
     * 性别。0 男、1 女、2 未知
     */
    @Schema(description = "性别：0 男、1 女、2 未知", example = "0")
    @TableField("sex")
    private String sex;

    /**
     * 头像地址。存相对路径或完整 URL，长度上限 100
     */
    @Schema(description = "头像地址", example = "/profile/avatar/2026/10/20/xxx.png")
    @TableField("avatar")
    private String avatar;

    /**
     * 密码密文（BCrypt / 盐值哈希后的结果），禁止明文入库。
     * <p>
     * {@code @JsonProperty(access = WRITE_ONLY)} 表示只接受入参、永不参与 JSON 序列化，
     * 从根上避免查询接口把密码哈希吐给前端；MyBatis 读写不受该注解影响。
     * <p>
     * {@code @ToString.Exclude} 让 {@code toString()} 跳过本字段，
     * 避免把密码哈希写进日志或异常堆栈。注意它只影响 toString，
     * 不影响 JSON 序列化（那由上面的 {@code @JsonProperty} 负责），两者必须同时标注。
     */
    @Schema(description = "密码密文，仅入参，接口不会返回", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    @TableField("password")
    private String password;

    /**
     * 账号状态。0 正常、1 停用。停用账号登录时应在业务层直接拒绝
     */
    @Schema(description = "账号状态：0 正常、1 停用", example = "0")
    @TableField("status")
    private String status;

    /**
     * 最后登录 IP。登录成功后由业务层回写
     */
    @Schema(description = "最后登录 IP", example = "192.168.31.100")
    @TableField("login_ip")
    private String loginIp;

    /**
     * 最后登录时间
     */
    @Schema(description = "最后登录时间", example = "2026-10-20 13:50:50")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("login_date")
    private LocalDateTime loginDate;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "语文教研组")
    @TableField("remark")
    private String remark;
}
