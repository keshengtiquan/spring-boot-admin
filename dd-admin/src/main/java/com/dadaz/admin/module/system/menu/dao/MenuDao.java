package com.dadaz.admin.module.system.menu.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.menu.domain.entity.MenuEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜单表数据访问对象
 * <p>
 * 与 {@code UserDao} / {@code RoleDao} / {@code DeptDao} 同一套约定：
 * {@link Mapper} 注解不能少，否则 {@code @MapperScan(annotationClass = Mapper.class)}
 * 扫不到，注入失败。
 * <p>
 * 继承 {@link BaseMapper} 即获得单表 CRUD，无需 XML。菜单树是"一次查全表、内存组装"
 * （菜单量级小，几十到几百条），不要为递归查子节点写 N+1 的 SQL；
 * 将来若要按用户权限过滤菜单，再增加
 * {@code resources/mapper/system/MenuMapper.xml} 写"用户-角色-菜单"的联表查询，
 * 不要把 SQL 拼在 Service 里。
 *
 * @author dadaz
 */
@Mapper
public interface MenuDao extends BaseMapper<MenuEntity> {
}
