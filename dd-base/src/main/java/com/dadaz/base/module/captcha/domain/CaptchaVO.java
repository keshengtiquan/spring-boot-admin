package com.dadaz.base.module.captcha.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CaptchaVO {

    @Schema(description = "验证码唯一标识")
    private String uuid;

    @Schema(description = "验证码Base64图片")
    private String image;

    @Schema(description = "过期时间（秒）")
    private Long expireSeconds;
}
