package com.dadaz.admin.module.system.role.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadaz.admin.module.system.dept.dao.DeptDao;
import com.dadaz.admin.module.system.dept.domain.entity.DeptEntity;
import com.dadaz.admin.module.system.menu.dao.MenuDao;
import com.dadaz.admin.module.system.menu.domain.entity.MenuEntity;
import com.dadaz.admin.module.system.role.dao.RoleDao;
import com.dadaz.admin.module.system.role.dao.RoleDeptDao;
import com.dadaz.admin.module.system.role.dao.RoleMenuDao;
import com.dadaz.admin.module.system.role.domain.dto.RoleCreateDTO;
import com.dadaz.admin.module.system.role.domain.dto.RoleMenuAssignDTO;
import com.dadaz.admin.module.system.role.domain.entity.RoleEntity;
import com.dadaz.admin.module.system.role.domain.entity.RoleMenuEntity;
import com.dadaz.admin.module.system.role.service.RoleService;
import com.dadaz.base.enums.StatusEnum;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色业务服务实现
 *
 * @author dadaz
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleDao, RoleEntity> implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    /**
     * 数据范围默认值：全部数据权限。不传时按这个值入库
     */
    private static final String DATA_SCOPE_ALL = "1";

    /**
     * 数据范围：自定义数据权限。只有这个取值才使用 {@code sys_role_dept}
     */
    private static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 默认显示顺序，与 DDL 的 {@code DEFAULT 0} 保持一致
     */
    private static final int DEFAULT_ROLE_SORT = 0;

    private final DeptDao deptDao;

    private final RoleDeptDao roleDeptDao;

    private final RoleMenuDao roleMenuDao;

    private final MenuDao menuDao;

    /**
     * 构造器注入：依赖显式、字段可 final、单测时直接 new 就能注入替身
     */
    public RoleServiceImpl(DeptDao deptDao, RoleDeptDao roleDeptDao,
                           RoleMenuDao roleMenuDao, MenuDao menuDao) {
        this.deptDao = deptDao;
        this.roleDeptDao = roleDeptDao;
        this.roleMenuDao = roleMenuDao;
        this.menuDao = menuDao;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateDTO dto) {
        String roleKey = dto.getRoleKey().trim();

        // 预检：绝大多数重复场景在这里就能给出友好提示。
        // 注意这不是并发安全的最终保证——两个请求可能同时通过预检，
        // 真正的兜底是 role_key 唯一索引，所以下面的 save 必须捕获 DuplicateKeyException。
        if (lambdaQuery().eq(RoleEntity::getRoleKey, roleKey).exists()) {
            throw new BusinessException("角色权限标识 " + roleKey + " 已存在");
        }

        // 数据范围与部门的组合先校验、再落库：非法组合整体拒绝，不产生半成品角色。
        // 校验不依赖事务回滚——校验失败本来就不该走到写库。
        String dataScope = dto.getDataScope() == null ? DATA_SCOPE_ALL : dto.getDataScope();
        List<Long> deptIds = normalizeIds(dto.getDeptIds());
        validateDataScopeDepts(dataScope, deptIds);

        RoleEntity entity = new RoleEntity();
        entity.setRoleName(dto.getRoleName().trim());
        entity.setRoleKey(roleKey);
        entity.setRoleSort(dto.getRoleSort() == null ? DEFAULT_ROLE_SORT : dto.getRoleSort());
        entity.setDataScope(dataScope);
        entity.setStatus(dto.getStatus() == null ? StatusEnum.NORMAL.getCode() : dto.getStatus());
        entity.setRemark(StringUtils.trimToNull(dto.getRemark()));

        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            // 把数据库异常翻译成业务提示，不要让 DuplicateKeyException 冒到全局兜底变成"系统繁忙"
            log.warn("创建角色触发唯一索引冲突 roleKey={}", roleKey);
            throw new BusinessException("角色权限标识 " + roleKey + " 已存在", e);
        }

        saveRoleDepts(entity.getRoleId(), deptIds);

        // 只记录 ID、标识、数据范围与部门数量，不打印整个实体
        log.info("创建角色成功 roleId={}, roleKey={}, dataScope={}, deptCount={}",
                entity.getRoleId(), roleKey, dataScope, deptIds.size());
        return entity.getRoleId();
    }

    /**
     * 规范化 ID 列表：null/空返回空列表；去重并保持传入顺序；丢弃 null 元素
     * （Bean Validation 对容器元素为 null 是放行的，这里统一兜住）
     */
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> distinct = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) {
                distinct.add(id);
            }
        }
        return new ArrayList<>(distinct);
    }

    /**
     * 校验数据范围与部门的组合是否合法，并确认部门都存在。
     * <p>
     * 规则：
     * <ul>
     *   <li>{@code data_scope = '2'}（自定义）：{@code deptIds} 必须非空，且每个部门都存在；</li>
     *   <li>其他数据范围：{@code deptIds} 必须为空。范围由角色自身、用户所属部门或
     *       部门 ancestors 推导，本表不参与，残留关联只会造成"范围与数据对不上"的脏数据。</li>
     * </ul>
     */
    private void validateDataScopeDepts(String dataScope, List<Long> deptIds) {
        boolean custom = DATA_SCOPE_CUSTOM.equals(dataScope);
        if (!custom) {
            if (!deptIds.isEmpty()) {
                throw new BusinessException("数据范围不是自定义（2）时不能指定部门");
            }
            return;
        }
        if (deptIds.isEmpty()) {
            throw new BusinessException("自定义数据权限至少需要选择一个部门");
        }
        List<DeptEntity> depts = deptDao.selectByIds(deptIds);
        if (depts.size() == deptIds.size()) {
            return;
        }
        Set<Long> existing = new HashSet<>();
        for (DeptEntity dept : depts) {
            existing.add(dept.getDeptId());
        }
        List<Long> missing = new ArrayList<>();
        for (Long deptId : deptIds) {
            if (!existing.contains(deptId)) {
                missing.add(deptId);
            }
        }
        throw new BusinessException("部门不存在：" + missing);
    }

    /**
     * 写入自定义数据权限的部门关联：DAO 里一条 INSERT 批量写多行。
     * 只有 {@code data_scope = '2'} 才会走到这里，且调用前已保证 deptIds 非空、去重、部门存在。
     */
    private void saveRoleDepts(Long roleId, List<Long> deptIds) {
        if (deptIds.isEmpty()) {
            return;
        }
        roleDeptDao.insertBatch(roleId, deptIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, RoleMenuAssignDTO dto) {
        if (roleId == null || getById(roleId) == null) {
            throw new BusinessException("角色不存在：" + roleId);
        }

        List<Long> menuIds = normalizeIds(dto.getMenuIds());

        // 先校验、再清空：校验失败时不能把角色原有的菜单权限删掉
        validateMenusExist(menuIds);

        // 覆盖式分配：先删旧关联、再批量写新关联，同一事务保证"要么全换、要么不动"
        roleMenuDao.delete(Wrappers.<RoleMenuEntity>lambdaQuery()
                .eq(RoleMenuEntity::getRoleId, roleId));
        if (!menuIds.isEmpty()) {
            roleMenuDao.insertBatch(roleId, menuIds);
        }

        log.info("给角色分配菜单成功 roleId={}, menuCount={}", roleId, menuIds.size());
    }

    @Override
    public List<Long> listMenuIds(Long roleId) {
        if (roleId == null || getById(roleId) == null) {
            throw new BusinessException("角色不存在：" + roleId);
        }
        List<RoleMenuEntity> relations = roleMenuDao.selectList(Wrappers.<RoleMenuEntity>lambdaQuery()
                .eq(RoleMenuEntity::getRoleId, roleId));
        List<Long> menuIds = new ArrayList<>(relations.size());
        for (RoleMenuEntity relation : relations) {
            menuIds.add(relation.getMenuId());
        }
        menuIds.sort(Long::compareTo);
        return menuIds;
    }

    /**
     * 校验菜单 ID 是否都存在（目录/菜单/按钮都允许分配）
     */
    private void validateMenusExist(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return;
        }
        List<MenuEntity> menus = menuDao.selectByIds(menuIds);
        if (menus.size() == menuIds.size()) {
            return;
        }
        Set<Long> existing = new HashSet<>();
        for (MenuEntity menu : menus) {
            existing.add(menu.getMenuId());
        }
        List<Long> missing = new ArrayList<>();
        for (Long menuId : menuIds) {
            if (!existing.contains(menuId)) {
                missing.add(menuId);
            }
        }
        throw new BusinessException("菜单不存在：" + missing);
    }
}
