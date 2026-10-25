package com.dadaz.admin.module.system.auth.service;

import com.dadaz.admin.module.system.auth.domain.LoginDTO;
import com.dadaz.admin.module.system.auth.domain.LoginResultVO;
import com.dadaz.admin.module.system.user.domain.vo.UserVO;

/**
 * 登录认证服务
 *
 * @author dadaz
 */
public interface LoginService {

    /**
     * 账号密码登录
     * <p>
     * 处理顺序：验证码校验（{@code captcha.enabled=true} 时）→ 查账号 → 校验状态 → BCrypt 校验密码
     * → 签发 token → 回写最近登录信息。
     *
     * @param loginDTO 登录入参
     * @param clientIp 客户端 IP，记录到 {@code sys_user.login_ip}
     * @return token 与登录用户信息
     * @throws com.dadaz.base.exception.BusinessException 验证码错误、账号不存在、密码错误或账号被停用时抛出
     */
    LoginResultVO login(LoginDTO loginDTO, String clientIp);

    /**
     * 获取当前登录用户信息
     * <p>
     * 返回的是 {@link UserVO}，不含密码等敏感字段。
     *
     * @return 当前登录用户信息
     * @throws com.dadaz.base.exception.BusinessException 登录账号已不存在时抛出（会同时清除登录态）
     */
    UserVO getUserInfo();

    /**
     * 退出登录
     * <p>
     * 注销当前请求携带的 token：退出后该 token 再请求接口会返回 401。
     * 同一账号在其他设备上的登录不受影响；如果需要"退出所有端"，
     * 另行调用 {@code StpUtil.logout(userId)} / {@code StpUtil.kickout(userId)}。
     *
     * @return 退出结果提示
     */
    String logout();
}
