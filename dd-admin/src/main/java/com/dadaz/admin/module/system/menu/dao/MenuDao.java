package com.dadaz.admin.module.system.menu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.menu.domain.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单表数据访问对象
 * <p>
 * 与 {@code UserDao} / {@code RoleDao} / {@code DeptDao} 同一套约定：
 * {@link Mapper} 注解不能少，否则 {@code @MapperScan(annotationClass = Mapper.class)}
 * 扫不到，注入失败。
 * <p>
 * 继承 {@link BaseMapper} 即获得单表 CRUD；管理端菜单树是"一次查全表、内存组装"。
 * 当前用户路由菜单需要按用户-角色-菜单关系过滤，联表 SQL 放在
 * {@code resources/mapper/system/MenuMapper.xml}，不拼在 Service 里。
 *
 * @author dadaz
 */
@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {

    /** 查询当前用户通过有效角色获授的可用路由菜单（不含按钮）。 */
    List<MenuEntity> selectUserRoutes(@Param("userId") Long userId);
}
