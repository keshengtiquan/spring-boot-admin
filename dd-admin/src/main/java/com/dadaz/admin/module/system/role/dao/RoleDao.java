package com.dadaz.admin.module.system.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.role.domain.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色表数据访问对象
 * <p>
 * 与 {@code UserDao} 保持同一套约定：包名/类名用 DAO 语义，但 {@link Mapper} 注解不能少——
 * {@code MainApplication} 是按 {@code @MapperScan(annotationClass = Mapper.class)} 扫描的，
 * 没有这个注解的接口不会被注册成代理 Bean，注入时会直接 {@code NoSuchBeanDefinitionException}。
 * <p>
 * 继承 {@link BaseMapper} 即获得单表 CRUD，无需 XML。角色目前只有单表操作；
 * 后续要写"角色 + 菜单 + 用户"的关联查询时，再增加
 * {@code resources/mapper/system/RoleDao.xml}，不要把 SQL 拼在 Service 里。
 * <p>
 * {@code role_key} 上有唯一索引：并发创建同名角色时，即使 Service 的预检通过，
 * 插入仍可能抛 {@link org.springframework.dao.DuplicateKeyException}，调用方必须捕获并翻译成业务提示。
 *
 * @author dadaz
 */
@Mapper
public interface RoleDao extends BaseMapper<RoleEntity> {
}
