package com.dadaz.admin.module.system.auth.service.impl;

import com.dadaz.admin.module.system.auth.dao.AuthPermissionDao;
import com.dadaz.base.security.PermissionCodeProvider;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/** 从当前系统的用户-角色-菜单关系中读取 Sa-Token 权限数据。 */
@Service
public class AuthPermissionProvider implements PermissionCodeProvider {

    private final AuthPermissionDao permissionDao;

    public AuthPermissionProvider(AuthPermissionDao permissionDao) {
        this.permissionDao = permissionDao;
    }

    @Override
    public List<String> getPermissionCodes(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        return userId == null ? Collections.emptyList() : permissionDao.selectPermissionCodes(userId);
    }

    @Override
    public List<String> getRoleKeys(Object loginId, String loginType) {
        Long userId = toUserId(loginId);
        return userId == null ? Collections.emptyList() : permissionDao.selectRoleKeys(userId);
    }

    private Long toUserId(Object loginId) {
        if (loginId == null) {
            return null;
        }
        try {
            return Long.valueOf(loginId.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
