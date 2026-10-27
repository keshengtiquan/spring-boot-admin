package com.dadaz.admin.module.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadaz.admin.module.system.role.domain.dto.RoleCreateDTO;
import com.dadaz.admin.module.system.role.domain.dto.RoleMenuAssignDTO;
import com.dadaz.admin.module.system.role.domain.entity.RoleEntity;

import java.util.List;

/**
 * 角色业务服务
 * <p>
 * 继承 {@link IService} 获得标准单表能力（getById、list、updateById 等），
 * 自定义方法只放需要业务规则的部分。
 *
 * @author dadaz
 */
public interface RoleService extends IService<RoleEntity> {

    /**
     * 创建角色
     * <p>
     * 业务规则：
     * <ol>
     *   <li>{@code roleKey} 全表唯一（唯一索引兜底），重复时抛业务异常；</li>
     *   <li>{@code roleName} 允许重名，仅作展示；</li>
     *   <li>可选字段未传时按业务默认值补齐：{@code role_sort = 0}、{@code data_scope = '1'}、
     *       {@code status = '0'}，不依赖数据库默认值，保证"接口看到的就是入库的"；</li>
     *   <li>名称/标识统一 trim，备注空白串归一为 null，避免 {@code ''} 与 {@code null} 两种空值并存；</li>
     *   <li>{@code dataScope = '2'}（自定义数据权限）时必须传 {@code deptIds} 且非空，
     *       逐个校验部门存在，创建成功后写入 {@code sys_role_dept}；</li>
     *   <li>其他数据范围不允许传 {@code deptIds}，避免残留与数据范围不一致的关联记录。</li>
     * </ol>
     * <p>
     * 方法参数不需要 {@code @Valid}：Bean Validation 在 Controller 的
     * {@code @Valid @RequestBody} 上触发，这里的校验注解不会再生效。
     *
     * @param dto 创建入参，已通过 Bean Validation 校验
     * @return 新角色的 ID
     * @throws com.dadaz.base.exception.BusinessException 角色权限标识已存在、数据范围与部门组合非法、
     *                                                   或指定部门不存在时抛出
     */
    Long createRole(RoleCreateDTO dto);

    /**
     * 给角色分配菜单（全量覆盖）
     * <p>
     * 语义：入参即该角色最终拥有的全部菜单。
     * <ol>
     *   <li>先校验角色存在、菜单 ID 全部存在；校验失败时不改动原有关联；</li>
     *   <li>校验通过后，同一事务内删除该角色旧的 {@code sys_role_menu} 关联，再批量写入新关联；</li>
     *   <li>{@code menuIds} 传空数组表示清空该角色的菜单权限；</li>
     *   <li>重复 ID 去重；目录（M）、菜单（C）、按钮（F）都可以分配。</li>
     * </ol>
     * 不做"增量追加"：否则前端取消勾选的菜单永远不会被撤销。
     *
     * @param roleId 角色 ID
     * @param dto    菜单 ID 列表（不能为 null，清空请传空数组）
     * @throws com.dadaz.base.exception.BusinessException 角色不存在或菜单 ID 不存在时抛出
     */
    void assignMenus(Long roleId, RoleMenuAssignDTO dto);

    /**
     * 查询角色已分配的菜单 ID（含目录/菜单/按钮）
     * <p>
     * 用于菜单分配页把已授权菜单回显为树上的勾选状态。按菜单 ID 升序返回，
     * 无授权时返回空列表（不是 null）。
     *
     * @param roleId 角色 ID
     * @return 菜单 ID 列表
     * @throws com.dadaz.base.exception.BusinessException 角色不存在时抛出
     */
    List<Long> listMenuIds(Long roleId);
}
