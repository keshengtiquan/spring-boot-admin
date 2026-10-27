package com.dadaz.admin.module.system.user.domain.dto;

import com.dadaz.base.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 创建用户入参
 * <p>
 * 只承载"客户端可以决定"的字段。以下字段刻意不在本 DTO 中暴露：
 * <ul>
 *   <li>{@code userId}：数据库自增，客户端无权指定；</li>
 *   <li>{@code userType}：服务端固定写 '01'（其他用户）。若开放给前端，
 *       等于允许调用方自行提升账号类型，属于越权隐患；</li>
 *   <li>{@code loginIp} / {@code loginDate}：登录态字段，由服务端维护；</li>
 *   <li>{@code createBy} / {@code createTime} / {@code updateBy} / {@code updateTime}：
 *       由 {@code MybatisMetaObjectHandler} 自动填充，客户端传入也会被覆盖。</li>
 * </ul>
 * <p>
 * 这里用 {@code @Data} 是合适的：DTO 是纯值对象，没有父类，
 * 判等语义就是"所有字段相同即相同"，也没有持久化实体的 hashCode 稳定性问题。
 * 唯一的例外是 password 必须排除在 toString 之外，否则日志、异常堆栈里会留下明文密码。
 *
 * @author dadaz
 */
@Data
@Schema(name = "UserCreateDTO", description = "创建用户入参")
public class UserCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录账号，全局唯一
     */
    @Schema(description = "登录账号，全局唯一", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    @Size(min = 2, max = 30, message = "登录账号长度需在 2 到 30 个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "登录账号只能包含字母、数字和下划线")
    private String userName;

    /**
     * 用户昵称，页面展示用
     */
    @Schema(description = "用户昵称", example = "张三", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户昵称不能为空")
    @Size(max = 30, message = "用户昵称长度不能超过 30 个字符")
    private String nickName;

    /**
     * 初始密码明文。只在本次请求中传输，落库前立即 BCrypt 加密，不会以任何形式保存明文
     */
    @Schema(description = "初始密码明文，落库前加密", example = "Abc@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在 6 到 20 个字符之间")
    @ToString.Exclude
    private String password;

    /**
     * 所属部门 ID，可为空
     */
    @Schema(description = "所属部门 ID，可为空", example = "100")
    private Long deptId;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "zhangsan@dadaz.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过 50 个字符")
    private String email;

    /**
     * 手机号。允许为空串（前端表单清空后常提交 ""），非空时必须是 11 位手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;

    /**
     * 性别。不传则视为未知
     */
    @Schema(description = "性别：0 男、1 女、2 未知，不传默认 2", example = "0")
    @Pattern(regexp = "^[012]$", message = "性别取值只能是 0、1、2")
    private String sex;

    /**
     * 头像地址
     */
    @Schema(description = "头像地址", example = "/profile/avatar/default.png")
    @Size(max = 100, message = "头像地址长度不能超过 100 个字符")
    private String avatar;

    /**
     * 账号状态。不传则默认正常
     */
    @Schema(description = "账号状态：0 正常、1 停用，不传默认 0", example = "0")
    @Pattern(regexp = StatusEnum.PATTERN, message = "账号状态取值只能是 0 或 1")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "语文教研组")
    @Size(max = 500, message = "备注长度不能超过 500 个字符")
    private String remark;

    /**
     * 角色 ID 列表，可空。重复 ID 由 Service 去重；有任何一个角色不存在则整体拒绝
     */
    @Schema(description = "角色 ID 列表，可空；重复 ID 去重，不存在的角色会拒绝创建", example = "[2]")
    @Size(max = 50, message = "一次最多关联 50 个角色")
    private List<@Positive(message = "角色 ID 必须为正整数") Long> roleIds;
}
