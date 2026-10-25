package com.dadaz.admin.module.system.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dadaz.admin.module.system.user.domain.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表数据访问对象
 * <p>
 * 包名与类名使用 DAO 语义（{@code dao.UserDao}），因为它承担的职责是"数据访问"，
 * 而不是 MyBatis 框架意义上的 Mapper 映射文件绑定。
 * <p>
 * <b>为什么类名不叫 Mapper 却仍然要标 {@link Mapper}</b>：
 * 本项目的 {@code MainApplication} 用 {@code @MapperScan(annotationClass = Mapper.class)}
 * 按注解扫描数据访问接口，没有这个注解的接口不会被注册成代理 Bean，
 * 注入时会出现 NoSuchBeanDefinitionException。这个注解只是 MyBatis 的注册标记，
 * 与类名叫 Mapper 还是 Dao 无关，重构命名时不要顺手删掉它。
 * <p>
 * 继承 {@link BaseMapper} 即可获得单表 CRUD，无需写 XML。
 * 当前没有自定义 SQL，但保留了扩展位：复杂列表查询（多条件 + 部门关联）建议后续用
 * {@code resources/mapper/system/UserDao.xml} 实现，不要在 Service 里拼字符串条件。
 * <p>
 * {@code user_name} 上有唯一索引：并发创建同名账号时，即使 Service 的预检全部通过，
 * 插入仍可能抛 {@code DuplicateKeyException}，调用方必须捕获并翻译成业务提示。
 *
 * @author dadaz
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
