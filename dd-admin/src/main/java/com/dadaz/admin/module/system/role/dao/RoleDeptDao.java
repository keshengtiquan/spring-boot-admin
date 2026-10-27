package com.dadaz.admin.module.system.role.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.role.domain.entity.RoleDeptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色-部门关联表数据访问对象
 * <p>
 * 与 {@code RoleDao} / {@code UserRoleDao} 同一套约定：{@link Mapper} 注解不能少，
 * 否则 {@code @MapperScan(annotationClass = Mapper.class)} 扫不到，注入失败。
 * <p>
 * 这张表是联合主键 {@code (role_id, dept_id)}，没有单列主键：
 * {@code selectById / deleteById} 这类按单主键操作的方法不要用，
 * 按角色或部门维度的查询/删除请用 {@code LambdaQueryWrapper} 传条件。
 * <p>
 * 自定义 SQL 一律写在 {@code resources/mapper/system/RoleDeptMapper.xml}，
 * 不在接口上用 {@code @Insert} 注解：SQL 属于资源，放 XML 便于格式化与后续扩展。
 * <p>
 * 本表只在 {@code sys_role.data_scope = '2'}（自定义数据权限）时使用；
 * 写入统一走 {@link #insertBatch(Long, List)}，一条 INSERT 带多行 VALUES。
 *
 * @author dadaz
 */
@Mapper
public interface RoleDeptDao extends BaseMapper<RoleDeptEntity> {

    /**
     * 批量写入某角色的自定义数据权限部门：单条 INSERT，多行 VALUES
     * <p>
     * 要求 {@code deptIds} 非空——空集合在 XML 里会拼出没有 VALUES 的非法 SQL，
     * 调用方负责先判空（{@code RoleServiceImpl#saveRoleDepts} 已处理），
     * 同时负责去重（重复的 {@code (role_id, dept_id)} 会撞联合主键）。
     * <p>
     * SQL 见 {@code resources/mapper/system/RoleDeptMapper.xml}。
     *
     * @param roleId  角色 ID
     * @param deptIds 部门 ID 列表，不能为 null 或空
     * @return 实际插入行数
     */
    int insertBatch(@Param("roleId") Long roleId, @Param("deptIds") List<Long> deptIds);
}
