package com.dadaz.admin.module.system.auth.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** Sa-Token 所需权限码与角色标识查询。 */
@Mapper
public interface AuthPermissionDao {

    List<String> selectPermissionCodes(@Param("userId") Long userId);

    List<String> selectRoleKeys(@Param("userId") Long userId);
}
