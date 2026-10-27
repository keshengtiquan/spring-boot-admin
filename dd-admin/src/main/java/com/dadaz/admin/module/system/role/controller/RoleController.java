package com.dadaz.admin.module.system.role.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.dadaz.admin.module.system.role.domain.dto.RoleCreateDTO;
import com.dadaz.admin.module.system.role.domain.dto.RoleMenuAssignDTO;
import com.dadaz.admin.module.system.role.service.RoleService;
import com.dadaz.base.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统角色接口
 * <p>
 * 路径采用 {@code /system/role}（模块/资源两级），前端按此路径对接即可。
 * <p>
 * 必须用 {@link RestController}（= {@code @Controller} + {@code @ResponseBody}），
 * 否则 {@code R<Long>} 会被当成视图名去解析，接口返回 404/模板错误而不是 JSON。
 * <p>
 * <b>关于鉴权</b>：与 {@code UserController} 一致，登录体系就绪前不加 Sa-Token 注解，
 * 否则接口现在无法调用。登录/权限体系落地后，本接口必须补上：
 * <pre>
 * &#64;SaCheckPermission("system:role:add")
 * </pre>
 * 创建角色属于高权限操作，不能只靠前端藏按钮。
 *
 * @author dadaz
 */
@RestController
@RequestMapping("/system/role")
@Tag(name = "系统角色", description = "角色的创建与菜单授权")
public class RoleController {

    @Resource
    private RoleService roleService;

    /**
     * 创建角色
     * 成功返回新角色 ID：{@code {"code":200,"message":"创建成功","data":100}}
     *
     * @param dto 创建入参，字段约束见 {@link RoleCreateDTO}
     * @return 新角色 ID
     */
    @PostMapping("/create")
    @SaCheckPermission("system:role:create")
    @Operation(summary = "创建角色", description = "角色权限标识全表唯一；数据范围/状态不传时使用默认值")
    public R<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        return R.success("创建成功", roleService.createRole(dto));
    }

    /**
     * 给角色分配菜单（全量覆盖）
     * <p>
     * 语义：请求体里的菜单列表就是该角色最终拥有的全部菜单。
     * 一个事务内先删旧关联、再批量写新关联；传空数组表示清空该角色的菜单权限。
     * 菜单 ID 包含目录/菜单/按钮，前端菜单树勾选结果整体传入即可。
     *
     * @param roleId 角色 ID
     * @param dto    菜单 ID 列表
     * @return 成功提示
     */
    @PutMapping("/{roleId}/menus")
    @SaCheckPermission("system:role:edit")
    @Operation(summary = "给角色分配菜单",
            description = "全量覆盖：先清空该角色原有关联再批量写入；menuIds 传空数组表示清空权限")
    public R<Void> assignMenus(@PathVariable Long roleId,
                               @Valid @RequestBody RoleMenuAssignDTO dto) {
        roleService.assignMenus(roleId, dto);
        return R.success("分配成功", null);
    }

    /**
     * 查询角色已分配的菜单 ID
     * <p>
     * 用于菜单分配页回显：前端拿到 ID 列表后把菜单树对应节点设为勾选态。
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表（含目录/菜单/按钮），按 ID 升序；无授权时为空数组
     */
    @GetMapping("/{roleId}/menus")
    @SaCheckPermission("system:role:query")
    @Operation(summary = "查询角色已分配的菜单 ID", description = "用于菜单分配页回显勾选，无授权时返回空数组")
    public R<List<Long>> listMenuIds(@PathVariable Long roleId) {
        return R.success(roleService.listMenuIds(roleId));
    }
}
