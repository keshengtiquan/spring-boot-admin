package com.dadaz.base.module.captcha;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.dadaz.base.enums.ResultCode;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.model.R;
import com.dadaz.base.module.captcha.domain.CaptchaDTO;
import com.dadaz.base.module.captcha.domain.CaptchaVO;
import com.dadaz.base.module.redis.RedisService;
import com.dadaz.base.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Objects;

@Slf4j
@Service
public class CaptchaService {
    private static final long EXPIRE_SECOND = 65L;

    /**
     * 校验失败提示：入参缺失、已过期、不匹配三种情况分开提示，前端可以给出不同引导
     */
    private static final String CAPTCHA_EMPTY_MESSAGE = "请输入验证码";
    private static final String CAPTCHA_EXPIRED_MESSAGE = "验证码已过期，请刷新重试";
    private static final String CAPTCHA_MISMATCH_MESSAGE = "验证码错误，请输入正确的验证码";

    @Resource
    private RedisService redisService;

    public CaptchaVO generateCaptcha(){
        //生成四位验证码
        String captchaText = RandomUtil.randomNumbers(4);

        //定义图形验证码的长、宽、验证码位数、干扰线数量
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(125, 43, 4, 80);

        //设置背景颜色
        lineCaptcha.setBackground(new Color(230, 244, 255));

        //生成图片
        Image image = lineCaptcha.createImage(captchaText);

        //转为base64
        String base64Code = ImgUtil.toBase64(image, "jpg");

        // uuid 唯一标识
        String uuid = IdUtil.fastSimpleUUID();

        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setUuid(uuid);
        captchaVO.setImage("data:image/png;base64," + base64Code);
        captchaVO.setExpireSeconds(EXPIRE_SECOND);

        redisService.set(uuid, captchaText, EXPIRE_SECOND);
        return captchaVO;
    }

    /**
     * 校验验证码，校验失败直接抛出 {@link BusinessException}。
     * <p>
     * 登录这类「校验失败即中止流程」的场景用这个方法，业务代码不需要再判断返回值；
     *
     * @param uuid 验证码唯一标识（生成接口返回）
     * @param code 用户输入的验证码
     * @throws BusinessException 验证码缺失、已过期或与输入不匹配时抛出
     */
    public void verify(String uuid, String code) {
        String errorMessage = validate(uuid, code);
        if (errorMessage != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, errorMessage);
        }
    }



    /**
     * 验证码校验核心逻辑。
     * <p>
     * 校验通过后立即删除 Redis 中的验证码，保证一个验证码只能用一次；
     * 校验失败时保留到过期时间，用户输错后重新输入仍可继续校验。
     *
     * @param uuid 验证码唯一标识
     * @param code 用户输入的验证码
     * @return 校验失败原因；校验通过返回 {@code null}
     */
    private String validate(String uuid, String code) {
        // uuid 和 code 任意一个为空都算入参缺失：只判断 code 会出现 uuid 为空时直接查 Redis 的无效开销
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(code)) {
            return CAPTCHA_EMPTY_MESSAGE;
        }

        String redisCaptchaCode = redisService.get(uuid);
        if (StringUtils.isBlank(redisCaptchaCode)) {
            return CAPTCHA_EXPIRED_MESSAGE;
        }

        // 原逻辑是「相等返回失败」，判断写反了，这里修正为「不相等才算失败」；
        // 顺手 trim 一下，避免用户复制粘贴带进首尾空格
        if (!Objects.equals(redisCaptchaCode, code.trim())) {
            log.warn("验证码校验失败 uuid={}", uuid);
            return CAPTCHA_MISMATCH_MESSAGE;
        }

        redisService.delete(uuid);
        return null;
    }
}
