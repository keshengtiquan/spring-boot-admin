package com.dadaz.admin.module.system.auth.domain;

import com.dadaz.admin.module.system.user.domain.vo.UserVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 当前登录用户资料及其角色、权限码。 */
@Data
@Schema(name = "CurrentUserInfoVO", description = "当前登录用户信息、角色及权限码")
public class CurrentUserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前用户基本信息")
    private UserVO user;

    @Schema(description = "角色标识列表", example = "[\"admin\"]")
    private List<String> roles;

    @Schema(description = "权限码列表", example = "[\"system:user:list\"]")
    private List<String> permissions;
}
