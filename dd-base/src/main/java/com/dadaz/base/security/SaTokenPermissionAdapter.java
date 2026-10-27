package com.dadaz.base.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/** Sa-Token 的权限查询适配器，将权限数据委托给业务模块。 */
@Component
public class SaTokenPermissionAdapter implements StpInterface {

    private final ObjectProvider<PermissionCodeProvider> provider;

    public SaTokenPermissionAdapter(ObjectProvider<PermissionCodeProvider> provider) {
        this.provider = provider;
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        PermissionCodeProvider source = provider.getIfAvailable();
        return source == null ? Collections.emptyList() : source.getPermissionCodes(loginId, loginType);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        PermissionCodeProvider source = provider.getIfAvailable();
        return source == null ? Collections.emptyList() : source.getRoleKeys(loginId, loginType);
    }
}
