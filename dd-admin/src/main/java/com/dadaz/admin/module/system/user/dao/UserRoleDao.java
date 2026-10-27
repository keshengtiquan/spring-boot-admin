package com.dadaz.admin.module.system.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.user.domain.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-角色关联表数据访问对象
 * <p>
 * 与 {@code UserDao} / {@code RoleDao} 同一套约定：{@link Mapper} 注解不能少，
 * 否则 {@code @MapperScan(annotationClass = Mapper.class)} 扫不到，注入失败。
 * <p>
 * 注意这张表是联合主键 {@code (user_id, role_id)}，没有单列主键：
 * {@code BaseMapper} 里 {@code selectById} / {@code deleteById} 这类按单主键操作的方法不要用，
 * 按维度查询/删除请用 {@code LambdaQueryWrapper} 传条件。
 * <p>
 * 自定义 SQL 一律写在 {@code resources/mapper/system/UserRoleMapper.xml}，
 * 不在接口上用 {@code @Insert} 注解：SQL 属于资源，放 XML 便于格式化、复用
 * {@code <sql>} 片段、后续扩展 {@code <foreach>} / 动态条件，也避免注解里字符串拼接难以阅读。
 * XML 的 {@code namespace} 必须是本接口全限定名，方法名与 {@code <insert id>} 一致；
 * {@code mapper-locations: classpath:mapper/**\/*.xml} 已在 application-dev/prod.yaml 配置。
 * <p>
 * 批量写入走 {@link #insertBatch(Long, List)}：一条 INSERT 带多行 VALUES，
 * 而不是在 Service 里循环发 N 条单条 SQL（N 次网络往返 + N 次 SQL 解析）。
 *
 * @author dadaz
 */
@Mapper
public interface UserRoleDao extends BaseMapper<UserRoleEntity> {

    /**
     * 批量写入某用户的角色关联：单条 INSERT，多行 VALUES
     * <p>
     * 要求 {@code roleIds} 非空——空集合在 XML 里会拼出没有 VALUES 的非法 SQL，
     * 调用方负责先判空（{@code UserServiceImpl#saveUserRoles} 已处理），
     * 同时负责去重（重复的 {@code (user_id, role_id)} 会撞联合主键）。
     * <p>
     * SQL 见 {@code resources/mapper/system/UserRoleMapper.xml}。
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表，不能为 null 或空
     * @return 实际插入行数
     */
    int insertBatch(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
