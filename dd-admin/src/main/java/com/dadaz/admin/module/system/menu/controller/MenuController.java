package com.dadaz.admin.module.system.menu.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.dadaz.admin.module.system.menu.domain.dto.MenuCreateDTO;
import com.dadaz.admin.module.system.menu.domain.vo.MenuTreeVO;
import com.dadaz.admin.module.system.menu.service.MenuService;
import com.dadaz.base.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统菜单接口
 * <p>
 * 路径采用 {@code /system/menu}（模块/资源两级）。两个接口的用途：
 * <ul>
 *   <li>{@code POST /system/menu}：创建目录/菜单/按钮；</li>
 *   <li>{@code GET /system/menu/tree}：管理端菜单树（含按钮，用于菜单管理、角色分配菜单）。</li>
 * </ul>
 * <b>关于鉴权</b>：与其它模块一致，登录体系落地前不加 Sa-Token 注解；
 * 上线前本类需要补 {@code @SaCheckPermission("system:menu:add")} 等。
 *
 * @author dadaz
 */
@RestController
@RequestMapping("/system/menu")
@Tag(name = "系统菜单", description = "菜单管理")
public class MenuController {

    @Resource
    private MenuService menuService;

    /**
     * 创建菜单
     * 成功返回新菜单 ID：{@code {"code":200,"message":"创建成功","data":2001}}
     *
     * @param dto 创建入参，字段约束见 {@link MenuCreateDTO}
     * @return 新菜单 ID
     */
    @PostMapping
    @SaCheckPermission("system:menu:create")
    @Operation(summary = "创建菜单", description = "M 目录：必须传 path、不能传 component；C 菜单：path 与 component 都必填；F 按钮：只承载 perms")
    public R<Long> create(@Valid @RequestBody MenuCreateDTO dto) {
        return R.success("创建成功", menuService.createMenu(dto));
    }

    /**
     * 管理端菜单树（包含全部类型与停用菜单）
     *
     * @return 菜单树，无数据时为空列表
     */
    @GetMapping("/tree")
    @SaCheckPermission("system:menu:list")
    @Operation(summary = "菜单树", description = "含目录/菜单/按钮全部节点，供菜单管理与角色分配菜单使用")
    public R<List<MenuTreeVO>> tree() {
        return R.success(menuService.listTree());
    }
}
