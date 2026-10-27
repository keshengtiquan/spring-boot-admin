package com.dadaz.admin.module.system.menu.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadaz.admin.module.system.menu.dao.MenuDao;
import com.dadaz.admin.module.system.menu.domain.dto.MenuCreateDTO;
import com.dadaz.admin.module.system.menu.domain.entity.MenuEntity;
import com.dadaz.admin.module.system.menu.domain.vo.MenuTreeVO;
import com.dadaz.admin.module.system.menu.domain.vo.MenuRouteVO;
import com.dadaz.admin.module.system.menu.service.MenuService;
import com.dadaz.base.enums.StatusEnum;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.util.StringUtils;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单业务服务实现
 *
 * @author dadaz
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuDao, MenuEntity> implements MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);

    /**
     * 顶级菜单的 parent_id 约定
     */
    private static final long ROOT_PARENT_ID = 0L;

    /**
     * 菜单类型：目录
     */
    private static final String TYPE_DIR = "M";

    /**
     * 菜单类型：菜单
     */
    private static final String TYPE_MENU = "C";

    /**
     * 菜单类型：按钮
     */
    private static final String TYPE_BUTTON = "F";

    /**
     * 显示状态：显示
     */
    private static final String VISIBLE_SHOW = "0";

    /**
     * 默认显示顺序，与 DDL 的 {@code DEFAULT 0} 保持一致
     */
    private static final int DEFAULT_ORDER_NUM = 0;

    /**
     * 默认图标
     */
    private static final String DEFAULT_ICON = "#";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMenu(MenuCreateDTO dto) {
        long parentId = dto.getParentId() == null ? ROOT_PARENT_ID : dto.getParentId();
        String menuType = dto.getMenuType();
        String menuName = dto.getMenuName().trim();

        validateParent(parentId);
        boolean frame = "1".equals(dto.getIsFrame());
        RouteFields routeFields = normalizeRouteFields(menuType, frame, dto);

        MenuEntity entity = new MenuEntity();
        entity.setMenuName(menuName);
        entity.setParentId(parentId);
        entity.setOrderNum(dto.getOrderNum() == null ? DEFAULT_ORDER_NUM : dto.getOrderNum());
        entity.setPath(routeFields.path());
        entity.setComponent(routeFields.component());
        entity.setQuery(routeFields.query());
        entity.setIsFrame(frame);
        entity.setIsCache("1".equals(dto.getIsCache()));
        entity.setIsAffix("1".equals(dto.getIsAffix()));
        entity.setMenuType(menuType);
        entity.setVisible(dto.getVisible() == null ? VISIBLE_SHOW : dto.getVisible());
        entity.setStatus(dto.getStatus() == null ? StatusEnum.NORMAL.getCode() : dto.getStatus());
        entity.setPerms(StringUtils.trimToNull(dto.getPerms()));
        entity.setIcon(StringUtils.trimToNull(dto.getIcon()) == null ? DEFAULT_ICON : dto.getIcon().trim());
        entity.setRemark(StringUtils.trimToNull(dto.getRemark()));

        save(entity);

        log.info("创建菜单成功 menuId={}, parentId={}, menuType={}", entity.getMenuId(), parentId, menuType);
        return entity.getMenuId();
    }

    @Override
    public List<MenuTreeVO> listTree() {
        List<MenuEntity> menus = lambdaQuery()
                .orderByAsc(MenuEntity::getParentId)
                .orderByAsc(MenuEntity::getOrderNum)
                .orderByAsc(MenuEntity::getMenuId)
                .list();
        if (menus.isEmpty()) {
            return List.of();
        }

        Map<Long, MenuTreeVO> nodes = new LinkedHashMap<>(menus.size());
        for (MenuEntity menu : menus) {
            MenuTreeVO node = new MenuTreeVO();
            BeanUtil.copyProperties(menu, node);
            node.setChildren(new ArrayList<>());
            nodes.put(menu.getMenuId(), node);
        }

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO node : nodes.values()) {
            MenuTreeVO parent = nodes.get(node.getParentId());
            if (parent == null) {
                // 顶级节点，或父节点已不存在的历史脏数据，统一挂到根上展示，避免整棵子树消失
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

    @Override
    public List<MenuRouteVO> listCurrentUserRoutes() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<MenuEntity> menus = baseMapper.selectUserRoutes(userId);
        if (menus.isEmpty()) {
            return List.of();
        }
        return menus.stream()
                .map(menu -> BeanUtil.copyProperties(menu, MenuRouteVO.class))
                .toList();
    }

    /**
     * 校验父菜单：存在且不是按钮（按钮是叶子节点，不能再挂子项）
     */
    private void validateParent(long parentId) {
        if (parentId <= ROOT_PARENT_ID) {
            return;
        }
        MenuEntity parent = getById(parentId);
        if (parent == null) {
            throw new BusinessException("上级菜单不存在：" + parentId);
        }
        if (TYPE_BUTTON.equals(parent.getMenuType())) {
            throw new BusinessException("按钮下不能创建子菜单");
        }
    }

    /**
     * 按菜单类型规范化路由字段，并校验必需项
     * <p>
     * 返回 record 而不是直接改 DTO：DTO 是外部入参，Service 不应回写它，
     * 后续若要复用同一 DTO 做别的操作也不会互相污染。
     */
    private RouteFields normalizeRouteFields(String menuType, boolean frame, MenuCreateDTO dto) {
        String path = StringUtils.trimToNull(dto.getPath());
        String component = StringUtils.trimToNull(dto.getComponent());
        String query = StringUtils.trimToNull(dto.getQuery());

        if (TYPE_BUTTON.equals(menuType)) {
            // 按钮只承载权限标识，不参与菜单/路由展示；即使前端传了路由字段也直接丢弃
            return new RouteFields(null, null, null);
        }
        if (path == null) {
            throw new BusinessException("目录/菜单的路由地址不能为空");
        }
        if (frame) {
            // 外链：path 是完整 URL，不配置组件，由前端按外链方式打开（iframe / 新窗口）
            return new RouteFields(path, null, query);
        }
        if (TYPE_MENU.equals(menuType) && component == null) {
            throw new BusinessException("菜单的组件路径不能为空");
        }
        if (TYPE_DIR.equals(menuType)) {
            // 目录只做分组，不绑定具体组件
            component = null;
        }
        return new RouteFields(path, component, query);
    }

    /**
     * 路由字段规范化结果
     */
    private record RouteFields(String path, String component, String query) {
    }
}
