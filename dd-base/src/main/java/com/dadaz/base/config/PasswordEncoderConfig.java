package com.dadaz.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置
 * <p>
 * 只注册一个 {@link PasswordEncoder} Bean，供业务层注入使用。
 * 刻意不引入 {@code spring-boot-starter-security}：只需要 BCrypt 这一个工具，
 * 引入整个 starter 会激活 Spring Security 的过滤器链，与 Sa-Token 鉴权体系冲突。
 * <p>
 * <b>BCrypt 的两个必须知道的特性</b>：
 * <ul>
 *   <li><b>每次加密结果都不同。</b>BCrypt 会把随机盐写进哈希串本身，
 *       所以同一个明文两次 {@code encode} 得到的密文不一样，<b>不能用密文相等来校验密码</b>，
 *       必须用 {@code encoder.matches(明文, 库里的密文)}；</li>
 *   <li><b>不可逆。</b>忘记密码只能重置，不能"查回原密码"。</li>
 * </ul>
 * <p>
 * 默认强度 10，输出固定 60 字符，与 {@code sys_user.password varchar(100)} 的长度匹配。
 * 强度每 +1 计算耗时翻倍，10 是安全性与登录延迟的常用平衡点；调整前先压测登录接口。
 *
 * @author dadaz
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * BCrypt 密码编码器
     *
     * @return 强度为 10（BCrypt 默认值）的编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
