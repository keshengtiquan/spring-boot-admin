package com.dadaz.base.module.captcha;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 图形验证码配置，对应 yaml 中的 {@code captcha.*}。
 * <p>
 * 只控制「登录时是否强制校验图形验证码」；验证码生成接口本身不受影响，
 * 关闭后登录入参里的 {@code code} / {@code uuid} 会被直接忽略，
 * 方便本地联调或不需要验证码的部署环境。
 * <p>
 * 用法示例（application.yaml）：
 * <pre>
 * captcha:
 *   enabled: true
 * </pre>
 *
 * @author dadaz
 */
@Data
@Component
@ConfigurationProperties(prefix = "captcha")
public class CaptchaProperties {

    /**
     * 是否开启登录验证码。
     * <p>
     * true：登录必须携带并校验验证码；false：跳过验证码校验。
     * 默认 true（安全优先），需要关闭时在配置文件中显式设为 false。
     */
    private boolean enabled = true;
}
