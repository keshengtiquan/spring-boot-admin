-- =============================================================================
-- sys_user：系统用户表
-- 对应实体：com.dadaz.admin.module.system.user.domain.entity.UserEntity
-- 目标库：dd-vue（MySQL 8.0.46，库默认 utf8mb4 / utf8mb4_0900_ai_ci）
--
-- 执行前请确认：
--   1. 已在目标库上执行（USE dd-vue;），本脚本不切库，避免误伤其他库；
--   2. 本脚本未包含 DROP TABLE，重复执行会因表已存在而报错，这是有意的保护；
--   3. 字段默认值与 UserEntity 的字段语义严格对应，改动任何一侧都要同步另一侧。
-- =============================================================================

CREATE TABLE IF NOT EXISTS `sys_user`
(
    `user_id`     bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `dept_id`     bigint                DEFAULT NULL COMMENT '所属部门ID，可为空（不归属具体部门的账号）',
    `user_name`   varchar(30)  NOT NULL COMMENT '登录账号，全局唯一',
    `nick_name`   varchar(30)  NOT NULL COMMENT '用户昵称，页面展示用',
    `user_type`   varchar(2)   NOT NULL DEFAULT '00' COMMENT '用户类型：00 系统用户、01 其他注册用户',
    `email`       varchar(50)           DEFAULT '' COMMENT '用户邮箱',
    `phonenumber` varchar(11)           DEFAULT '' COMMENT '手机号码（列名无下划线，沿用原始表结构）',
    `sex`         char(1)      NOT NULL DEFAULT '0' COMMENT '性别：0 男、1 女、2 未知',
    `avatar`      varchar(100)          DEFAULT '' COMMENT '头像地址',
    `password`    varchar(100) NOT NULL DEFAULT '' COMMENT '密码密文（BCrypt 哈希，禁止明文）',
    `status`      char(1)      NOT NULL DEFAULT '0' COMMENT '账号状态：0 正常、1 停用',
    `login_ip`    varchar(128)          DEFAULT '' COMMENT '最后登录IP',
    `login_date`  datetime              DEFAULT NULL COMMENT '最后登录时间',
    `create_by`   varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`),
    -- 登录账号唯一：并发创建同名账号时由数据库兜底
    UNIQUE KEY `uk_sys_user_user_name` (`user_name`),
    -- 按部门检索用户
    KEY `idx_sys_user_dept_id` (`dept_id`),
    -- 列表页最常见的过滤条件
    KEY `idx_sys_user_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='用户信息表';

-- -----------------------------------------------------------------------------
-- 说明（不执行，仅备忘）
--
-- 1) 为什么不加外键 dept_id -> sys_dept.dept_id
--    本项目一贯由应用层保证引用完整性，不加物理外键。原因是外键会让
--    部门删除、数据迁移、分库分表变得很麻烦，且失败信息对上层不友好。
--    引用校验放在 Service 层做。
--
-- 2) 为什么 create_time / update_time 既由 MyBatis-Plus 填充、又设了 DEFAULT
--    双保险：正常走 MyBatis-Plus 时由 MybatisMetaObjectHandler 赋值；
--    手工 SQL、数据迁移、其他系统写入时由 DEFAULT 兜底。
--    update_time 的 ON UPDATE CURRENT_TIMESTAMP 还顺带补上了 MetaObjectHandler
--    的已知边界——用 LambdaUpdateWrapper 做纯条件更新时不会触发 updateFill，
--    此时由 MySQL 自动刷新更新时间。
--
-- 3) 为什么不给 nick_name 加唯一索引
--    截图里该字段带 key 标记，但昵称不是登录凭证，重名是正常业务（两个"张三"）。
--    给昵称加唯一约束会让新建用户直接失败。唯一性只约束 user_name。
--
-- 4) 初始管理员账号
--    密码必须写入 BCrypt 哈希，不能写明文。生成方式（引入 spring-security-crypto 后）：
--      new BCryptPasswordEncoder().encode("你的密码")
--    或本地用任意 BCrypt 工具生成 $2a$10$ 开头的 60 字符哈希。
--    哈希生成后再执行 INSERT，本脚本不预置任何账号，避免留下弱口令。
-- -----------------------------------------------------------------------------
