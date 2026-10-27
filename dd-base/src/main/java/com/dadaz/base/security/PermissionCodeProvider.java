package com.dadaz.base.security;

import java.util.List;

/**
 * 权限数据提供接口。业务模块负责按自身的数据模型查询权限码和角色标识，
 * dd-base 负责将其接入 Sa-Token。
 */
public interface PermissionCodeProvider {

    List<String> getPermissionCodes(Object loginId, String loginType);

    List<String> getRoleKeys(Object loginId, String loginType);
}
