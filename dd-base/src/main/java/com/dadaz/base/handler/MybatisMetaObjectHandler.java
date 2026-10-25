package com.dadaz.base.handler;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充器：为继承了 {@code BaseEntity} 的实体填充创建人、创建时间、更新人、更新时间
 * <p>
 * 触发时机由实体字段上的 {@code @TableField(fill = ...)} 决定：
 * <ul>
 *   <li>{@code FieldFill.INSERT}：仅在 insert 时填充；</li>
 *   <li>{@code FieldFill.INSERT_UPDATE}：insert 与 update 都填充。</li>
 * </ul>
 * <p>
 * 已知边界（MyBatis-Plus 自身行为，不是本类的缺陷）：
 * <ul>
 *   <li>只通过 {@code LambdaUpdateWrapper.set(...)} 做纯条件更新时，不会触发 updateFill，
 *       需要在业务层显式 set 更新时间，或者改用 {@code update(entity, wrapper)}；</li>
 *   <li>XML 手写 SQL 不经过该填充器，需要自己维护审计字段。</li>
 * </ul>
 * <p>
 * 本类不加 {@code @Component}：由 {@code BaseAutoConfiguration} 以 Bean 方式注册，
 * 避免将来组件扫描范围扩大到 {@code com.dadaz} 时被重复注册。
 *
 * @author dadaz
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    private static final Logger log = LoggerFactory.getLogger(MybatisMetaObjectHandler.class);

    /**
     * 无登录上下文时使用的操作人（定时任务、数据初始化、单元测试等场景）
     */
    public static final String DEFAULT_OPERATOR = "system";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String operator = currentOperator();

        // strictInsertFill 只在实体确实声明了该字段、且 fill 策略匹配时才写入，
        // 因此未继承 BaseEntity 的实体会被安全跳过；
        // 其内部走 strictFillStrategy：字段已有值时不覆盖，便于数据迁移显式保留原始时间。
        strictInsertFill(metaObject, "createBy", String.class, operator);
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateBy", String.class, operator);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新场景强制覆盖，不采用 strictUpdateFill：
        // 否则前端传来的旧值会把审计字段覆盖成历史值，失去审计意义。
        // setFieldValByName 内部有 hasSetter 判断，实体没有该字段时安全跳过。
        setFieldValByName("updateBy", currentOperator(), metaObject);
        setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }

    /**
     * 取当前登录账号；非 Web 线程或未登录时降级为 {@link #DEFAULT_OPERATOR}
     *
     * @return 操作人标识，不会为 null
     */
    private String currentOperator() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            return loginId == null ? DEFAULT_OPERATOR : String.valueOf(loginId);
        } catch (SaTokenException e) {
            // 非 Web 上下文（定时任务、异步线程）没有 SaTokenContext，属于预期情况
            log.debug("无法获取登录上下文，操作人降级为 {}", DEFAULT_OPERATOR);
            return DEFAULT_OPERATOR;
        }
    }
}
