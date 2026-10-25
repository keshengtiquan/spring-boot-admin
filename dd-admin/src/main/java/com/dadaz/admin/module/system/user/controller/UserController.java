package com.dadaz.admin.module.system.user.controller;

import com.dadaz.admin.module.system.user.domain.dto.UserCreateDTO;
import com.dadaz.admin.module.system.user.domain.dto.UserQueryDTO;
import com.dadaz.admin.module.system.user.domain.vo.UserVO;
import com.dadaz.admin.module.system.user.service.UserService;
import com.dadaz.base.enums.ResultCode;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.model.PageResult;
import com.dadaz.base.model.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户接口
 * <p>
 * 路径与若依前端约定一致（{@code /system/user}），前端无需额外适配。
 * <p>
 * <b>关于鉴权</b>：当前项目采用 Sa-Token 注解鉴权模式（见 {@code SaTokenConfigure}），
 * 未标注解的接口默认放行，且登录接口尚未落地。因此本类暂未加 {@code @SaCheckLogin} /
 * {@code @SaCheckPermission}，否则接口现在无法调用。
 * 等登录体系就绪后，必须补上：
 * <pre>
 * &#64;SaCheckPermission("system:user:add")      // 创建
 * &#64;SaCheckPermission("system:user:query")    // 查询
 * </pre>
 * 创建用户属于高权限操作，上线前务必完成这一步，不能只靠前端藏按钮。
 *
 * @author dadaz
 */
@RestController
@RequestMapping("/system/user")
@Tag(name = "系统用户", description = "用户的创建与查询")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 创建用户
     * <p>
     * 请求示例：
     * <pre>
     * POST /system/user
     * {
     *   "userName": "zhangsan",
     *   "nickName": "张三",
     *   "password": "Abc@123456",
     *   "deptId": 100,
     *   "email": "zhangsan@dadaz.com",
     *   "phoneNumber": "13800138000",
     *   "sex": "0",
     *   "remark": "语文教研组"
     * }
     * </pre>
     * 成功返回新用户 ID：{@code {"code":200,"message":"创建成功","data":100}}
     *
     * @param dto 创建入参，字段约束见 {@link UserCreateDTO}
     * @return 新用户 ID
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "登录账号全局唯一；密码服务端加密，不接受明文入库")
    public R<Long> create( @Valid @RequestBody UserCreateDTO dto) {
        return R.success("创建成功", userService.createUser(dto));
    }

    /**
     * 按 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息，不含密码
     */
    @GetMapping("/{userId}")
    @Operation(summary = "按 ID 查询用户")
    public R<UserVO> getById(
            @Parameter(description = "用户 ID", example = "100")
            @PathVariable Long userId) {
        UserVO user = userService.getUserById(userId);
        if (user == null) {
            // 查不到时统一表述为"不存在"
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return R.success(user);
    }

    @GetMapping("/pageList")
    @Operation(summary = "分页查询用户", description = "支持账号/昵称模糊、状态精确过滤")
    public R<PageResult<UserVO>> getPage(@ParameterObject @Valid UserQueryDTO userQueryDTO) {
        return R.success(userService.queryPage(userQueryDTO));
    }
}
