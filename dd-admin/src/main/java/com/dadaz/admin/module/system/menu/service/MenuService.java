package com.dadaz.admin.module.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadaz.admin.module.system.menu.domain.dto.MenuCreateDTO;
import com.dadaz.admin.module.system.menu.domain.entity.MenuEntity;
import com.dadaz.admin.module.system.menu.domain.vo.MenuTreeVO;

import java.util.List;

/**
 * 菜单业务服务
 * <p>
 * 继承 {@link IService} 获得标准单表能力；自定义方法负责两件事：
 * 创建菜单、管理端菜单树。
 *
 * @author dadaz
 */
public interface MenuService extends IService<MenuEntity> {

    /**
     * 创建菜单
     * <p>
     * 业务规则：
     * <ol>
     *   <li>{@code parentId} 不传或 0 表示顶级；传正数时必须存在，且父节点不能是按钮（F）；</li>
     *   <li>目录（M）/菜单（C）必须有 {@code path}；菜单（C）在非外链时还必须有 {@code component}；</li>
     *   <li>外链（{@code isFrame = '1'}）的 {@code path} 是完整 URL，不配置 {@code component}；</li>
     *   <li>目录（M）的 {@code component} 会被强制清空——目录由前端布局组件包裹 children；</li>
     *   <li>按钮（F）只承载 {@code perms}，{@code path/component/query} 会被忽略并置空；</li>
     *   <li>可选字段未传时按业务默认值补齐：{@code order_num = 0}、{@code visible = '0'}、
     *       {@code status = '0'}、{@code icon = '#'}。</li>
     * </ol>
     * <p>
     * 菜单名、权限标识允许重复（不同父节点下的"查询"按钮是正常业务），不加唯一约束。
     *
     * @param dto 创建入参，已通过 Bean Validation 校验
     * @return 新菜单的 ID
     * @throws com.dadaz.base.exception.BusinessException 父菜单不存在/父节点是按钮/路由字段组合非法时抛出
     */
    Long createMenu(MenuCreateDTO dto);

    /**
     * 管理端菜单树：包含目录、菜单、按钮全部节点
     * <p>
     * 用于菜单管理页面和"给角色分配菜单"的树选择器。
     * {@code children} 已按 {@code orderNum}、{@code menuId} 排序，叶子节点为空数组。
     *
     * @return 顶级节点列表，无数据时为空列表
     */
    List<MenuTreeVO> listTree();
}
