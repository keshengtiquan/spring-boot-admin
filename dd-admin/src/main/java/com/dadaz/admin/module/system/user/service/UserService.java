package com.dadaz.admin.module.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadaz.admin.module.system.user.domain.dto.UserCreateDTO;
import com.dadaz.admin.module.system.user.domain.dto.UserQueryDTO;
import com.dadaz.admin.module.system.user.domain.dto.UserUpdateDTO;
import com.dadaz.admin.module.system.user.domain.entity.UserEntity;
import com.dadaz.admin.module.system.user.domain.vo.UserVO;
import com.dadaz.base.model.PageResult;
import jakarta.validation.Valid;

/**
 * 用户业务服务
 * <p>
 * 继承 {@link IService} 获得标准单表能力（getById、list、updateById 等），
 * 自定义方法只放需要业务规则的部分。
 *
 * @author dadaz
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 创建用户
     * <p>
     * 业务规则：
     * <ol>
     *   <li>登录账号全局唯一（忽略大小写由数据库排序规则保证）；</li>
     *   <li>密码必须 BCrypt 加密后入库，明文不出本方法；</li>
     *   <li>用户类型固定写 '00'（系统用户），不接受调用方指定；</li>
     *   <li>可空字段未传时按业务默认值补齐，不依赖数据库默认值；</li>
     *   <li>{@code roleIds} 可选：去重后校验角色是否都存在，创建用户成功后写入
     *       {@code sys_user_role}；有任一角色不存在则整体拒绝，不产生半成品用户。</li>
     * </ol>
     *
     * @param dto 创建入参，已通过 Bean Validation 校验
     * @return 新用户的 ID
     * @throws com.dadaz.base.exception.BusinessException 账号已存在或角色不存在时抛出
     */
    Long createUser(UserCreateDTO dto);

    /**
     * 按 ID 查询用户
     *
     * @param userId 用户 ID
     * @return 用户信息；不存在时返回 null
     */
    UserVO getUserById(Long userId);

    /**
     * 分页查询用户
     * <p>
     * 查询语义：
     * <ol>
     *   <li>{@code userName} / {@code nickName} 模糊匹配，空值不过滤；</li>
     *   <li>{@code status} 精确匹配，取值 0 正常、1 停用；</li>
     *   <li>默认按用户 ID 倒序；可通过 {@code orderByColumn} + {@code isAsc} 指定排序
     *       （字段名限实体属性或列名，走白名单校验，多列用逗号分隔）；</li>
     *   <li>结果不含密码字段。</li>
     * </ol>
     *
     * @param userQueryDTO 查询条件 + 分页参数；分页参数不传时使用
     *                     {@link com.dadaz.base.model.PageParam} 的字段默认值（1 / 10），
     *                     非法值由 {@code @Min} / {@code @Max} 在接口层拒绝
     * @return 总记录数与当页数据，无命中时 list 为空集合
     */
    PageResult<UserVO> queryPage(UserQueryDTO userQueryDTO);

    Long delete(@Valid Long userId);

    String updateUser(UserUpdateDTO dto);
}
