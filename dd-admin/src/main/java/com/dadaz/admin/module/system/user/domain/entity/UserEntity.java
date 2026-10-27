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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统用户实体，对应表 {@code sys_user}
 * <p>
 * 字段来源：{@code sys_user} 通用结构，按当前项目约定做了两点调整：
 * <ul>
 *   <li>审计字段（create_by / create_time / update_by / update_time）由 {@link BaseEntity} 承载，
 *       由 {@code MybatisMetaObjectHandler} 自动填充，本类不再重复声明；</li>
 *   <li>列名一律显式写在 {@code @TableField(value = ...)} 上，不依赖驼峰自动映射。</li>
 * </ul>
 * <p>
 * 注解说明：类上用 {@code @Data} 简写 getter/setter，但 {@code @Data} 自带的生成物
 * 对持久化实体并不合适，因此逐项显式覆盖：
 * <ul>
 *   <li><b>判等与 hashCode</b>：{@code @EqualsAndHashCode(callSuper = false, of = "userId")}，
 *       实体应当"同一行数据即同一对象"，只按主键判等，插入后 hashCode 不再变化。
 *       不能写成 {@code callSuper = true}——{@link BaseEntity} 没有重写 equals/hashCode，
 *       Lombok 只会生成 {@code super.equals(o)} / {@code super.hashCode()}，实际落到
 *       {@code Object} 的引用比较，两条"同一行"的实体将永远不相等；
 *       也不宜用默认的全字段判等（含 password、avatar、remark），改个昵称就不相等，
 *       等于把实体当成值对象；</li>
 *   <li><b>toString</b>：{@code @ToString(callSuper = true)} 让日志里能看到审计字段，
 *       并在 {@code password} 上标 {@code @ToString.Exclude}，防止密码哈希写进日志或异常堆栈。
 *       注意它只影响 toString，JSON 脱敏由字段上的
 *       {@code @JsonProperty(access = WRITE_ONLY)} 负责，两者必须同时标注；</li>
 *   <li><b>构造器</b>：显式 {@code @NoArgsConstructor} 保留 public 无参构造，供 MyBatis 反射实例化
 *       与 Jackson 反序列化使用；类上存在构造器注解时 {@code @Data} 不再生成
 *       {@code @RequiredArgsConstructor}，避免将来加了 final 字段后无参构造被悄悄替换。</li>
 * </ul>
 * <p>
 * 取值约定（这些值是字典回显、状态过滤的依据，改动前必须同步所有读取方）：
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
@Data
@NoArgsConstructor
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
     * 手机号。注意表列名是 {@code phonenumber}（无下划线，沿用原始表结构），
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
