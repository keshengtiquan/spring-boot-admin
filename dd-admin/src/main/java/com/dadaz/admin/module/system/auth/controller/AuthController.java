package com.dadaz.admin.module.system.auth.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.dadaz.admin.module.system.auth.domain.LoginDTO;
import com.dadaz.admin.module.system.auth.domain.LoginResultVO;
import com.dadaz.admin.module.system.auth.domain.CurrentUserInfoVO;
import com.dadaz.admin.module.system.auth.service.LoginService;
import com.dadaz.base.model.R;
import com.dadaz.base.module.captcha.CaptchaService;
import com.dadaz.base.module.captcha.domain.CaptchaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 登录认证接口
 * <p>
 * 当前只实现账号密码登录，登录成功后返回 Sa-Token token（请求头名默认 {@code satoken}）。
 * <p>
 * <b>关于 {@link SaIgnore}</b>：现在 Sa-Token 还是注解鉴权模式（{@code new SaInterceptor()} 不带全局校验），
 * 这个注解暂时不生效；等以后开启 {@code /**} 全局 {@code checkLogin()} 时，
 * 登录接口靠它豁免，不用再往拦截器白名单里加路径。
 *
 * @author dadaz
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "权限管理", description = "用户的登录权限")
public class AuthController {

    @Resource
    private LoginService loginService;

    @Resource
    private CaptchaService captchaService;

    /**
     * 账号密码登录
     * @param loginDTO 登录入参：账号 + 密码
     * @param request  用于获取客户端 IP，记录到 {@code sys_user.login_ip}
     * @return token 与登录用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "账号密码登录",
            description = "登录成功后返回 Sa-Token token，后续请求通过 satoken 请求头携带")
    public R<LoginResultVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String clientIp = JakartaServletUtil.getClientIP(request);
        return R.success("登录成功", loginService.login(loginDTO, clientIp));
    }

    @GetMapping("/getUserInfo")
    @Operation(summary = "获取登录用户的信息", description = "获取当前登录用户的信息")
    public R<CurrentUserInfoVO> getUserInfo() {
        return R.success(loginService.getUserInfo());
    }

    @GetMapping("/logout")
    @Operation(summary = "退出登录", description = "注销当前 token，同一账号在其他设备上的登录不受影响")
    public R<String> logout() {
        return R.success(loginService.logout());
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码")
    public R<CaptchaVO> generateCaptcha(){
        return R.success(captchaService.generateCaptcha());
    }

}
