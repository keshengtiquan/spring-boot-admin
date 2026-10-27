package com.dadaz.admin.module.system.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dadaz.admin.module.system.auth.domain.LoginDTO;
import com.dadaz.admin.module.system.auth.domain.LoginResultVO;
import com.dadaz.admin.module.system.auth.domain.CurrentUserInfoVO;
import com.dadaz.admin.module.system.auth.dao.AuthPermissionDao;
import com.dadaz.admin.module.system.auth.service.LoginService;
import com.dadaz.admin.module.system.user.dao.UserDao;
import com.dadaz.admin.module.system.user.domain.entity.UserEntity;
import com.dadaz.admin.module.system.user.domain.vo.UserVO;
import com.dadaz.base.enums.ResultCode;
import com.dadaz.base.enums.StatusEnum;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.module.captcha.CaptchaProperties;
import com.dadaz.base.module.captcha.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录认证实现（账号密码登录 + 可配置图形验证码）
 * <p>
 * 验证码是否开启由 {@code captcha.enabled}（{@link CaptchaProperties}）控制：
 * 开启后登录必须先通过图形验证码校验，关闭后忽略入参中的验证码字段。
 *
 * @author dadaz
 */
@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);

    /**
     * 凭证错误统一提示：不区分"账号不存在"和"密码错误"，避免接口被用来枚举账号
     */
    private static final String BAD_CREDENTIALS_MESSAGE = "账号或密码错误";

    private final UserDao userDao;

    private final AuthPermissionDao authPermissionDao;

    private final PasswordEncoder passwordEncoder;

    private final CaptchaService captchaService;

    /**
     * 验证码开关：{@code captcha.enabled}，控制登录时是否强制校验图形验证码
     */
    private final CaptchaProperties captchaProperties;

    /**
     * 构造器注入：依赖显式、字段可 final、单测时直接 new 就能注入替身
     */
    public LoginServiceImpl(UserDao userDao, AuthPermissionDao authPermissionDao, PasswordEncoder passwordEncoder,
                            CaptchaService captchaService, CaptchaProperties captchaProperties) {
        this.userDao = userDao;
        this.authPermissionDao = authPermissionDao;
        this.passwordEncoder = passwordEncoder;
        this.captchaService = captchaService;
        this.captchaProperties = captchaProperties;
    }

    @Override
    public LoginResultVO login(LoginDTO loginDTO, String clientIp) {
        // 0. 验证码校验必须放在最前面：校验不通过就不查库、不比对密码。
        //    既避免脚本绕过验证码直接刷密码，也避免通过"账号不存在/密码错误"的响应差异枚举账号。
        //    开关见 CaptchaProperties（yaml: captcha.enabled），关闭时整个校验跳过。
        if (captchaProperties.isEnabled()) {
            captchaService.verify(loginDTO.getUuid(), loginDTO.getCode());
        }

        // 前后空格容错：用户在输入框里不小心带上空格也能登录
        String userName = loginDTO.getUserName().trim();

        // 1. 按账号查用户；查不到也返回统一的"账号或密码错误"，不暴露账号是否存在
        UserEntity user = userDao.selectOne(Wrappers.<UserEntity>lambdaQuery()
                .eq(UserEntity::getUserName, userName));
        if (user == null) {
            log.warn("登录失败：账号不存在 userName={}, ip={}", userName, clientIp);
            throw new BusinessException(ResultCode.PARAM_ERROR, BAD_CREDENTIALS_MESSAGE);
        }

        // 2. 状态校验放在密码校验之前：停用账号直接给明确提示，也省掉一次 BCrypt 计算
        if (StatusEnum.isDisabled(user.getStatus())) {
            log.warn("登录失败：账号已停用 userId={}, userName={}, ip={}",
                    user.getUserId(), userName, clientIp);
            throw new BusinessException(ResultCode.PARAM_ERROR, "账号已被停用，请联系管理员");
        }

        // 3. BCrypt 校验：只能用 matches(明文, 库里的密文)。
        //    不能比较密文 —— BCrypt 每次 encode 都会写入随机盐，同一明文两次密文不同
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.warn("登录失败：密码错误 userId={}, userName={}, ip={}",
                    user.getUserId(), userName, clientIp);
            throw new BusinessException(ResultCode.PARAM_ERROR, BAD_CREDENTIALS_MESSAGE);
        }

        // 4. 签发 token：loginId 使用 userId，后续按 userId 反查用户/权限最直接
        StpUtil.login(user.getUserId());

        // 5. 回写最近登录信息。
        //    这里用纯 Wrapper 更新（不经过实体），不会触发 MyBatis-Plus 的 updateFill，
        //    所以 updateBy / updateTime 显式赋值（MybatisMetaObjectHandler 注释里写明的已知边界）
        LocalDateTime now = LocalDateTime.now();
        userDao.update(null, Wrappers.<UserEntity>lambdaUpdate()
                .set(UserEntity::getLoginIp, clientIp)
                .set(UserEntity::getLoginDate, now)
                .set(UserEntity::getUpdateBy, userName)
                .set(UserEntity::getUpdateTime, now)
                .eq(UserEntity::getUserId, user.getUserId()));

        log.info("登录成功 userId={}, userName={}, ip={}", user.getUserId(), userName, clientIp);

        // 6. 组装返回：token 信息 + 用户信息。UserVO 不含 password
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        LoginResultVO result = new LoginResultVO();
        result.setToken(tokenInfo.getTokenValue());
        return result;
    }

    @Override
    public CurrentUserInfoVO getUserInfo() {
        // loginId 就是 userId（登录时 StpUtil.login(user.getUserId())），直接取当前登录用户 ID
        Long userId = StpUtil.getLoginIdAsLong();

        // 账号被物理删除后这里查不到
        UserEntity user = userDao.selectById(userId);
        if (user == null) {
            // 账号已不存在：清掉登录态，前端收到 401 后回登录页，避免继续拿无效 token 请求
            StpUtil.logout();
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录用户不存在，请重新登录");
        }

        // UserVO 只保留对外字段，天然不含 password。
        CurrentUserInfoVO result = new CurrentUserInfoVO();
        result.setUser(BeanUtil.copyProperties(user, UserVO.class));
        result.setRoles(authPermissionDao.selectRoleKeys(userId));
        result.setPermissions(authPermissionDao.selectPermissionCodes(userId));
        return result;
    }

    @Override
    public String logout() {
        // 先取 userId 记日志：logout 之后登录态就没了
        Long userId = StpUtil.getLoginIdAsLong();

        // 只注销当前请求携带的 token，同一账号在其他设备上的登录不受影响。
        // 若以后要做"退出所有端"，另加方法用 StpUtil.logout(userId) 或 StpUtil.kickout(userId)
        StpUtil.logout();

        log.info("退出登录 userId={}", userId);
        return "退出成功";
    }
}
