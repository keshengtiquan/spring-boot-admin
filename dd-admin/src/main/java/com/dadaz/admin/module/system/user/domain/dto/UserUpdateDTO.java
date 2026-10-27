package com.dadaz.admin.module.system.user.domain.dto;

import com.dadaz.base.enums.StatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "UserCreateDTO", description = "创建用户入参")
public class UserUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 用户昵称，页面展示用
     */
    @Schema(description = "用户昵称", example = "张三")
    @Size(max = 30, message = "用户昵称长度不能超过 30 个字符")
    private String nickName;

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
     * 账号状态。不传则默认正常
     */
    @Schema(description = "账号状态：0 正常、1 停用，不传默认 0", example = "0")
    @Pattern(regexp = StatusEnum.PATTERN, message = "账号状态取值只能是 0 或 1")
    private String status;
}
