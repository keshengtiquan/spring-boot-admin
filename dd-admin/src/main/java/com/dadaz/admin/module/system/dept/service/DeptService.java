package com.dadaz.admin.module.system.dept.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadaz.admin.module.system.dept.domain.dto.DeptCreateDTO;
import com.dadaz.admin.module.system.dept.domain.entity.DeptEntity;

/**
 * 部门业务服务
 * <p>
 * 继承 {@link IService} 获得标准单表能力（getById、list、updateById 等），
 * 自定义方法只放需要业务规则的部分。
 *
 * @author dadaz
 */
public interface DeptService extends IService<DeptEntity> {

    /**
     * 创建部门
     * <p>
     * 业务规则：
     * <ol>
     *   <li>{@code parentId} 不传或 0 表示顶级部门，{@code ancestors = "0"}；传正数时必须存在，
     *       不存在抛业务异常；</li>
     *   <li>{@code ancestors} 由服务端计算：父部门 ancestors + "," + 父部门 ID，客户端不可指定；</li>
     *   <li>同一父部门下部门名称唯一（DB 联合唯一索引兜底），重复抛业务异常；</li>
     *   <li>可选字段未传时按业务默认值补齐：{@code order_num = 0}、{@code status = '0'}；</li>
     *   <li>名称/负责人/电话/邮箱/备注统一 trim，空白串归一为 null。</li>
     * </ol>
     *
     * @param dto 创建入参，已通过 Bean Validation 校验
     * @return 新部门的 ID
     * @throws com.dadaz.base.exception.BusinessException 父部门不存在或同级部门重名时抛出
     */
    Long createDept(DeptCreateDTO dto);
}
