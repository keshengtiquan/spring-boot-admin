package com.dadaz.admin.module.system.auth.domain;

import com.dadaz.admin.module.system.user.domain.vo.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录结果
 * <p>
 * 除了 token，同时把登录用户信息带回去，前端登录成功后不用再额外请求一次用户详情。
 *
 * @author dadaz
 */
@Data
@Schema(name = "LoginResultVO", description = "登录结果")
public class LoginResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "登录令牌，后续请求放在请求头中携带", example = "b0c3e6f6-8fef-4b0e-9b7f-2d4d5c1a6e8f")
    private String token;

}
