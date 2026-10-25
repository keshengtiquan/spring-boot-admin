package com.dadaz.base.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器配置
 * <p>
 * 只加 {@code sa-token-spring-boot3-starter} 依赖、写了 yaml 配置，都<b>不会</b>对任何请求做鉴权：
 * starter 只通过自动配置注册了上下文（SaTokenContext）与配置 Bean，拦截器必须由业务代码显式注册。
 * <p>
 * 当前采用<b>注解鉴权模式</b>：{@code new SaInterceptor()} 的 auth 是空实现，
 * 只处理 {@code @SaCheckLogin} / {@code @SaCheckPermission} / {@code @SaCheckRole} 等注解，
 * 未标注解的路由照常放行，对现有接口零影响。
 * <p>
 * <b>为什么现在不能用全局校验</b>：登录接口与用户体系尚未落地，
 * 若此时开启 {@code StpUtil.checkLogin()} 全路径拦截，所有业务接口、
 * 接口文档 {@code /doc.html}、{@code /v3/api-docs}、{@code /error} 都会返回未登录。
 * <p>
 * 等登录接口就绪后，再把下面的注解模式替换为全局登录校验，并<b>必须</b>带上白名单：
 * <pre>
 * registry.addInterceptor(new cn.dev33.satoken.interceptor.SaInterceptor(handle -&gt; cn.dev33.satoken.stp.StpUtil.checkLogin()))
 *         .addPathPatterns("/**")
 *         .excludePathPatterns("/user/login", "/captcha/**",
 *                              "/doc.html", "/webjars/**", "/v3/api-docs/**", "/favicon.ico", "/error");
 * </pre>
 *
 * @author dadaz
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/login", "/doc.html",
                        "/webjars/**",
                        "/favicon.ico",
                        "/v3/api-docs/**",
                        "/auth/captcha");
    }
}
