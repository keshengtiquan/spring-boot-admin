package com.dadaz.admin.module.system.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.role.domain.entity.RoleMenuEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-菜单关联表数据访问对象
 * <p>
 * 与 {@code RoleDao} / {@code RoleDeptDao} 同一套约定：{@link Mapper} 注解不能少，
 * 否则 {@code @MapperScan(annotationClass = Mapper.class)} 扫不到，注入失败。
 * <p>
 * 本表是联合主键 {@code (role_id, menu_id)}，没有单列主键：
 * {@code selectById / deleteById} 这类按单主键操作的方法不要用；
 * 按角色删除关联用 {@code LambdaQueryWrapper} 传条件（覆盖式分配的第一步）。
 * <p>
 * 自定义 SQL 写在 {@code resources/mapper/system/RoleMenuMapper.xml}，
 * 不在接口上用注解写 SQL。
 *
 * @author dadaz
 */
@Mapper
public interface RoleMenuDao extends BaseMapper<RoleMenuEntity> {

    /**
     * 批量写入某角色的菜单授权：单条 INSERT，多行 VALUES
     * <p>
     * 要求 {@code menuIds} 非空——空集合在 XML 里会拼出没有 VALUES 的非法 SQL，
     * 调用方负责先判空（空数组表示清空权限，不调用本方法），
     * 并负责去重（重复的 {@code (role_id, menu_id)} 会撞联合主键）。
     * <p>
     * SQL 见 {@code resources/mapper/system/RoleMenuMapper.xml}。
     *
     * @param roleId  角色 ID
     * @param menuIds 菜单 ID 列表，不能为 null 或空
     * @return 实际插入行数
     */
    int insertBatch(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
}
