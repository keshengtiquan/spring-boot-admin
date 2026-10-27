package com.dadaz.admin.module.system.dept.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.dadaz.admin.module.system.dept.domain.dto.DeptCreateDTO;
import com.dadaz.admin.module.system.dept.service.DeptService;
import com.dadaz.base.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统部门接口
 * <p>
 * 路径采用 {@code /system/dept}（模块/资源两级），前端按此路径对接即可。
 * <p>
 * 必须用 {@link RestController}（= {@code @Controller} + {@code @ResponseBody}），
 * 否则 {@code R<Long>} 会被当成视图名去解析，接口返回 404/模板错误而不是 JSON。
 * <p>
 * <b>关于鉴权</b>：与 {@code UserController} / {@code RoleController} 一致，登录体系就绪前
 * 不加 Sa-Token 注解，否则接口现在无法调用。登录/权限体系落地后，本接口必须补上：
 * <pre>
 * &#64;SaCheckPermission("system:dept:add")
 * </pre>
 * 部门是数据权限的挂载点，创建/调整部门属于高权限操作，不能只靠前端藏按钮。
 *
 * @author dadaz
 */
@RestController
@RequestMapping("/system/dept")
@Tag(name = "系统部门", description = "部门的创建")
public class DeptController {

    @Resource
    private DeptService deptService;

    /**
     * 创建部门
     * 成功返回新部门 ID：{@code {"code":200,"message":"创建成功","data":100}}
     *
     * @param dto 创建入参，字段约束见 {@link DeptCreateDTO}
     * @return 新部门 ID
     */
    @PostMapping
    @SaCheckPermission("system:dept:create")
    @Operation(summary = "创建部门", description = "parentId 不传/0 表示顶级部门；同一父部门下名称唯一；ancestors 由服务端计算")
    public R<Long> create(@Valid @RequestBody DeptCreateDTO dto) {
        return R.success("创建成功", deptService.createDept(dto));
    }
}
