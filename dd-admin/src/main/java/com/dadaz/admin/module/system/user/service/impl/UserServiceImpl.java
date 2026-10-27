package com.dadaz.admin.module.system.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadaz.admin.module.system.role.dao.RoleDao;
import com.dadaz.admin.module.system.role.domain.entity.RoleEntity;
import com.dadaz.admin.module.system.user.dao.UserDao;
import com.dadaz.admin.module.system.user.dao.UserRoleDao;
import com.dadaz.admin.module.system.user.domain.dto.UserCreateDTO;
import com.dadaz.admin.module.system.user.domain.dto.UserQueryDTO;
import com.dadaz.admin.module.system.user.domain.dto.UserUpdateDTO;
import com.dadaz.admin.module.system.user.domain.entity.UserEntity;
import com.dadaz.admin.module.system.user.domain.entity.UserRoleEntity;
import com.dadaz.admin.module.system.user.domain.vo.UserVO;
import com.dadaz.admin.module.system.user.service.UserService;
import com.dadaz.base.enums.StatusEnum;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.model.PageResult;
import com.dadaz.base.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户业务服务实现
 *
 * @author dadaz
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 系统用户类型。不开放给前端选择
     */
    private static final String USER_TYPE_SYSTEM = "01";

    /**
     * 性别未知
     */
    private static final String SEX_UNKNOWN = "2";

    /**
     * 密码列名，列表查询时从 SELECT 列表里排除
     */
    private static final String PASSWORD_COLUMN = "password";


    private final PasswordEncoder passwordEncoder;

    private final RoleDao roleDao;

    private final UserRoleDao userRoleDao;

    private final UserDao userDao;

    /**
     * 构造器注入：依赖显式、字段可 final、单测时直接 new 就能注入替身
     */
    public UserServiceImpl(PasswordEncoder passwordEncoder, RoleDao roleDao, UserRoleDao userRoleDao, UserDao userDao) {
        this.passwordEncoder = passwordEncoder;
        this.roleDao = roleDao;
        this.userRoleDao = userRoleDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        String userName = dto.getUserName().trim();

        // 先校验角色、再落用户：角色不存在时整体拒绝，不产生"用户建了但角色没关联"的半成品数据。
        // 校验放在事务里也不该依赖回滚来表达业务规则——校验失败本来就不该走到写库。
        List<Long> roleIds = normalizeRoleIds(dto.getRoleIds());
        validateRolesExist(roleIds);

        // 预检：绝大多数重复场景在这里就能给出友好提示。
        // 注意这不是并发安全的最终保证 —— 两个请求可能同时通过预检，
        // 真正的兜底是下面的唯一索引，所以 save 必须捕获 DuplicateKeyException。
        if (lambdaQuery().eq(UserEntity::getUserName, userName).exists()) {
            throw new BusinessException("登录账号 " + userName + " 已存在");
        }

        UserEntity entity = new UserEntity();
        entity.setUserName(userName);
        entity.setNickName(dto.getNickName().trim());
        // 明文密码到此为止：加密后原始字符串不再被引用，也不会进入日志
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setDeptId(dto.getDeptId());
        entity.setUserType(USER_TYPE_SYSTEM);
        entity.setEmail(StringUtils.trimToNull(dto.getEmail()));
        entity.setPhoneNumber(StringUtils.trimToNull(dto.getPhoneNumber()));
        entity.setSex(dto.getSex() == null ? SEX_UNKNOWN : dto.getSex());
        entity.setAvatar(StringUtils.trimToNull(dto.getAvatar()));
        entity.setStatus(dto.getStatus() == null ? StatusEnum.NORMAL.getCode() : dto.getStatus());
        entity.setRemark(StringUtils.trimToNull(dto.getRemark()));

        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            // 预检和插入之间有时间窗口，并发创建同名账号时两个请求都可能通过预检，
            // 真正的兜底是 user_name 唯一索引。这里把数据库异常翻译成业务提示，
            // 不要让 DuplicateKeyException 冒到全局兜底变成"系统繁忙"。
            log.warn("创建用户触发唯一索引冲突 userName={}", userName);
            throw new BusinessException("登录账号 " + userName + " 已存在", e);
        }

        saveUserRoles(entity.getUserId(), roleIds);

        // 这里只记录 userId、账号和角色数量，绝不记录 dto 或密码
        log.info("创建用户成功 userId={}, userName={}, roleCount={}", entity.getUserId(), userName, roleIds.size());
        return entity.getUserId();
    }

    @Override
    public UserVO getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        UserEntity entity = getById(userId);
        return entity == null ? null : BeanUtil.copyProperties(entity, UserVO.class);
    }


    /**
     * 分页查询用户
     *
     * @param userQueryDTO 查询条件 + 分页参数，允许为 null
     * @return 总记录数与当页数据；无命中时 list 为空集合（不是 null）
     */
    @Override
    public PageResult<UserVO> queryPage(UserQueryDTO userQueryDTO) {
        // 允许调用方传 null：分页参数本身就是可选项，缺省语义就是"第一页、每页 10 条"
        UserQueryDTO query = userQueryDTO == null ? new UserQueryDTO() : userQueryDTO;

        // 空白串按"没传"处理：页面上清空搜索框再点查询，提交的是 "" 而不是省略参数
        String userName = StringUtils.trimToNull(query.getUserName());
        String nickName = StringUtils.trimToNull(query.getNickName());
        String status = StringUtils.trimToNull(query.getStatus());

        LambdaQueryWrapper<UserEntity> wrapper = Wrappers.<UserEntity>lambdaQuery()
                // 只裁掉密码列，其余列（含主键、审计字段）照常映射
                .select(UserEntity.class, field -> !PASSWORD_COLUMN.equals(field.getProperty()))
                // like 的第三个参数只在条件为 true 时才生效，null 时整段条件不会进入 SQL
                .like(userName != null, UserEntity::getUserName, userName)
                .like(nickName != null, UserEntity::getNickName, nickName)
                .eq(status != null, UserEntity::getStatus, status);

        // 分页参数、默认值和排序都在 PageParam#build 里处理完，这里直接拿现成的 Page
        Page<UserEntity> entityPage = page(query.build(UserEntity.class), wrapper);

        PageResult<UserVO> result = new PageResult<>();
        result.setTotal(entityPage.getTotal());
        // 空页返回空集合而不是 null，前端做 v-for / length 时不用再判空
        result.setList(BeanUtil.copyToList(entityPage.getRecords(), UserVO.class));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long delete(Long userId) {
        // 1. 先删除关联表 user_role 的数据
        userRoleDao.delete(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getUserId, userId));

        // 2. 再删除主表 user 的数据
        int rows = userDao.delete(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUserId, userId));

        if (rows == 0) {
            throw new BusinessException("账号" + userId + "不存在或已被删除");
        }

        // 3. 返回被删除的用户id
        return userId;
    }

    @Override
    public String updateUser(UserUpdateDTO dto) {
        return "";
    }

    /**
     * 规范化角色 ID：null/空列表返回空列表；去重并保持传入顺序；丢弃 null 元素
     * （Bean Validation 对容器元素为 null 是放行的，这里统一兜住）
     */
    private List<Long> normalizeRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        Set<Long> distinct = new LinkedHashSet<>();
        for (Long roleId : roleIds) {
            if (roleId != null) {
                distinct.add(roleId);
            }
        }
        return new ArrayList<>(distinct);
    }

    /**
     * 校验角色是否都存在。
     * <p>
     * 必须先于 save 执行：校验失败不应依赖事务回滚，直接拒绝比"写一半再回滚"更清晰，
     * 对没有事务上下文的调用方（单测、脚本）也更安全。
     */
    private void validateRolesExist(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        List<RoleEntity> roles = roleDao.selectByIds(roleIds);
        if (roles.size() == roleIds.size()) {
            return;
        }
        Set<Long> existing = new HashSet<>();
        for (RoleEntity role : roles) {
            existing.add(role.getRoleId());
        }
        List<Long> missing = new ArrayList<>();
        for (Long roleId : roleIds) {
            if (!existing.contains(roleId)) {
                missing.add(roleId);
            }
        }
        throw new BusinessException("角色不存在：" + missing);
    }

    /**
     * 写入用户-角色关联：一条 INSERT 批量写入（DAO 里 foreach 拼多行 VALUES），
     * 避免在循环里发 N 条单条 SQL。调用方已去重并校验角色存在性，这里只负责落库。
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        userRoleDao.insertBatch(userId, roleIds);
    }

}
