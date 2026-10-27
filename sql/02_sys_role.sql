-- =============================================================================
-- sys_role：系统角色表
-- 对应实体：com.dadaz.admin.module.system.role.domain.entity.RoleEntity
-- 目标库：dd-vue（MySQL 8.0.46，库默认 utf8mb4 / utf8mb4_0900_ai_ci）
--
-- 执行前请确认：
--   1. 已在目标库上执行（USE `dd-vue`;），本脚本不切库，避免误伤其他库；
--   2. 本脚本未包含 DROP TABLE，重复执行会因表已存在而报错，这是有意的保护；
--   3. 字段默认值与 RoleEntity 的字段语义严格对应，改动任何一侧都要同步另一侧。
-- =============================================================================

CREATE TABLE IF NOT EXISTS `sys_role`
(
    `role_id`             bigint       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`           varchar(30)  NOT NULL COMMENT '角色名称',
    `role_key`            varchar(100) NOT NULL COMMENT '角色权限字符串，全表唯一',
    `role_sort`           int          NOT NULL DEFAULT 0 COMMENT '显示顺序，越小越靠前',
    `data_scope`          char(1)      NOT NULL DEFAULT '1' COMMENT '数据范围：1 全部数据权限、2 自定义数据权限、3 本部门数据权限、4 本部门及以下数据权限、5 仅本人数据权限',
    `status`              char(1)      NOT NULL DEFAULT '0' COMMENT '角色状态：0 正常、1 停用',
    `create_by`           varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`           varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`              varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`role_id`),
    -- 权限字符串是鉴权判断依据，必须唯一；并发创建同名角色时由数据库兜底
    UNIQUE KEY `uk_sys_role_role_key` (`role_key`),
    -- 列表页最常见的过滤条件
    KEY `idx_sys_role_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='角色信息表';

-- -----------------------------------------------------------------------------
-- 说明（不执行，仅备忘）
--
-- 1) 为什么 role_key 建唯一索引，role_name 不建
--    role_key 是鉴权判断依据（hasRole("admin") 一类），重复会让"谁有权限"变得不可预期，
--    必须在数据库层面兜住；Application 层的"先查再插"永远有并发时间窗口。
--    role_name 只是展示名，两个"运营"是正常业务，加唯一约束会让新建角色直接失败。
--    注意 utf8mb4_0900_ai_ci 不区分大小写，'Admin' 与 'admin' 视为重复，
--    这正好符合角色标识的语义：大小写不同的两个角色只会制造混乱。
--
-- 2) 为什么没有 del_flag
--    原版模板的 sys_role / sys_user 都有 del_flag 逻辑删除列，本项目在 sys_user 上已经去掉了，
--    本表保持一致：物理删除。好处是不用在每条查询里都记得补 del_flag = '0'，
--    坏处是删了就找不回来。真要改成逻辑删除，请连同 sys_user、关联表一起改，
--    并给实体加 @TableLogic；同时上面的唯一索引要改成 (role_key, del_flag) 之类的组合，
--    否则删掉的角色 key 永远无法重建。
--
-- 3) 固定角色 ID 约定
--    约定预留两个 ID：1 = 超级管理员（role_key = 'admin'）、2 = 普通角色（role_key = 'common'）。
--    很多权限实现会把 role_id = 1 当作"跳过权限校验"的硬编码判断，请勿挪作他用。
--    新角色从 AUTO_INCREMENT = 100 开始分配，给内置角色留足空间。
--    本脚本不预置角色数据（与 01_sys_user.sql 不预置管理员账号同理），需要时手工执行：
--
--    INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `status`, `remark`)
--    VALUES (1, '超级管理员', 'admin', 1, '1', '0', '超级管理员，拥有全部权限'),
--           (2, '普通角色', 'common', 2, '1', '0', '普通角色');
--
-- 4) 为什么 role_sort 不建索引
--    role_sort 只用于列表排序，不参与过滤；角色数量是个位数到几十的量级，
--    排序交给 filesort 完全够用，建索引反而是维护成本。status 才是高频过滤条件。
--
-- 5) 为什么没有 menu_check_strictly / dept_check_strictly
--    这两列只服务于"角色分配时菜单/部门树勾选是否父子联动"的前端配置，本项目不需要该交互，
--    因此表里不建列、实体里不建字段，避免一个只有前端用、后端永远不读的字段长期占位。
--    将来需要时成对补：tinyint(1) NOT NULL DEFAULT 1 + Java Boolean 字段，实体与 DDL 同步改。
--
-- 6) 为什么 create_time / update_time 既由 MyBatis-Plus 填充、又设了 DEFAULT
--    双保险：正常走 MyBatis-Plus 时由 MybatisMetaObjectHandler 赋值；
--    手工 SQL、数据迁移、其他系统写入时由 DEFAULT 兜底。
--    update_time 的 ON UPDATE CURRENT_TIMESTAMP 还顺带补上了 MetaObjectHandler
--    的已知边界——用 LambdaUpdateWrapper 做纯条件更新时不会触发 updateFill，
--    此时由 MySQL 自动刷新更新时间。
--
-- 7) 关联表另行建，不在本脚本中
--    sys_user_role（用户-角色）、sys_role_menu（角色-菜单）、sys_role_dept（角色-自定义数据范围）
--    都是多对多关联，等对应用户/菜单模块落地时各自成表；
--    关联表上要建 (user_id, role_id) / (role_id, menu_id) 这类联合主键或唯一索引，防止重复授权。
-- -----------------------------------------------------------------------------
