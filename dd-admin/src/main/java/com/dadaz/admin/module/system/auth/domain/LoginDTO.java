package com.dadaz.admin.module.system.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * 账号密码登录入参
 * <p>
 * 密码只在本次请求中传输，与库里的 BCrypt 密文比对后立即丢弃；
 * {@code @ToString.Exclude} 防止日志或异常堆栈里打印明文密码。
 *
 * @author dadaz
 */
@Data
@Schema(name = "LoginDTO", description = "账号密码登录入参")
public class LoginDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录账号", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "登录账号不能为空")
    @Length(max = 30, message = "登录账号最多30字符")
    private String userName;

    @Schema(description = "登录密码", example = "Abc@123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    @ToString.Exclude
    private String password;

    @Schema(description = "验证码（开启 captcha.enabled 时必填，值为 /auth/captcha 返回图片中的字符）")
    private String code;

    @Schema(description = "验证码uuid标识（开启 captcha.enabled 时必填，取自 /auth/captcha 返回值）")
    private String uuid;
}
