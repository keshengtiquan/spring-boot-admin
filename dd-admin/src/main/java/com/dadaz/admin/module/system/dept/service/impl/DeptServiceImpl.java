package com.dadaz.admin.module.system.dept.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadaz.admin.module.system.dept.dao.DeptDao;
import com.dadaz.admin.module.system.dept.domain.dto.DeptCreateDTO;
import com.dadaz.admin.module.system.dept.domain.entity.DeptEntity;
import com.dadaz.admin.module.system.dept.service.DeptService;
import com.dadaz.base.enums.StatusEnum;
import com.dadaz.base.exception.BusinessException;
import com.dadaz.base.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门业务服务实现
 *
 * @author dadaz
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptDao, DeptEntity> implements DeptService {

    private static final Logger log = LoggerFactory.getLogger(DeptServiceImpl.class);

    /**
     * 顶级部门的 parent_id 约定
     */
    private static final long ROOT_PARENT_ID = 0L;

    /**
     * 顶级部门的祖级列表：只包含占位根 0，不含自身
     */
    private static final String ROOT_ANCESTORS = "0";

    /**
     * 默认显示顺序，与 DDL 的 {@code DEFAULT 0} 保持一致
     */
    private static final int DEFAULT_ORDER_NUM = 0;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDept(DeptCreateDTO dto) {
        long parentId = dto.getParentId() == null ? ROOT_PARENT_ID : dto.getParentId();
        String deptName = dto.getDeptName().trim();

        // ancestors 只能由服务端计算：客户端传值会破坏整棵树的一致性
        String ancestors = ROOT_ANCESTORS;
        if (parentId > ROOT_PARENT_ID) {
            DeptEntity parent = getById(parentId);
            if (parent == null) {
                throw new BusinessException("上级部门不存在：" + parentId);
            }
            ancestors = parent.getAncestors() + "," + parent.getDeptId();
        }

        // 预检：同级重名绝大多数在这里就能给出友好提示。
        // 并发窗口由 (parent_id, dept_name) 唯一索引兜底，所以下面 save 仍要捕获 DuplicateKeyException。
        boolean sameNameExists = lambdaQuery()
                .eq(DeptEntity::getParentId, parentId)
                .eq(DeptEntity::getDeptName, deptName)
                .exists();
        if (sameNameExists) {
            throw new BusinessException("同级部门名称已存在：" + deptName);
        }

        DeptEntity entity = new DeptEntity();
        entity.setParentId(parentId);
        entity.setAncestors(ancestors);
        entity.setDeptName(deptName);
        entity.setOrderNum(dto.getOrderNum() == null ? DEFAULT_ORDER_NUM : dto.getOrderNum());
        entity.setLeader(StringUtils.trimToNull(dto.getLeader()));
        entity.setPhone(StringUtils.trimToNull(dto.getPhone()));
        entity.setEmail(StringUtils.trimToNull(dto.getEmail()));
        entity.setStatus(dto.getStatus() == null ? StatusEnum.NORMAL.getCode() : dto.getStatus());
        entity.setRemark(StringUtils.trimToNull(dto.getRemark()));

        try {
            save(entity);
        } catch (DuplicateKeyException e) {
            // 把数据库异常翻译成业务提示，不要让 DuplicateKeyException 冒到全局兜底变成"系统繁忙"
            log.warn("创建部门触发唯一索引冲突 parentId={}, deptName={}", parentId, deptName);
            throw new BusinessException("同级部门名称已存在：" + deptName, e);
        }

        // 只记录 ID 与父 ID，不打印整个实体
        log.info("创建部门成功 deptId={}, parentId={}", entity.getDeptId(), parentId);
        return entity.getDeptId();
    }
}
