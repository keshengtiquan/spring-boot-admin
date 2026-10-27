-- =============================================================================
-- sys_dept：系统部门表
-- 对应实体：com.dadaz.admin.module.system.dept.domain.entity.DeptEntity
-- 目标库：dd-vue（MySQL 8.0.46，库默认 utf8mb4 / utf8mb4_0900_ai_ci）
--
-- 执行前请确认：
--   1. 已在目标库上执行（USE `dd-vue`;），本脚本不切库，避免误伤其他库；
--   2. 本脚本未包含 DROP TABLE，重复执行会因表已存在而报错，这是有意的保护；
--   3. 字段默认值与 DeptEntity 的字段语义严格对应，改动任何一侧都要同步另一侧。
-- =============================================================================

CREATE TABLE IF NOT EXISTS `sys_dept`
(
    `dept_id`     bigint      NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   bigint      NOT NULL DEFAULT 0 COMMENT '父部门ID，0 表示顶级部门',
    `ancestors`   varchar(50) NOT NULL DEFAULT '0' COMMENT '祖级列表，逗号分隔，不含自身，如 0,100,101',
    `dept_name`   varchar(30) NOT NULL COMMENT '部门名称',
    `order_num`   int         NOT NULL DEFAULT 0 COMMENT '显示顺序，越小越靠前',
    `leader`      varchar(20)          DEFAULT NULL COMMENT '负责人',
    `phone`       varchar(11)          DEFAULT NULL COMMENT '联系电话',
    `email`       varchar(50)          DEFAULT NULL COMMENT '邮箱',
    `status`      char(1)     NOT NULL DEFAULT '0' COMMENT '部门状态：0 正常、1 停用',
    `create_by`   varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      varchar(500)         DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`dept_id`),
    -- 同一父部门下不允许重名；并发创建由数据库兜底
    UNIQUE KEY `uk_sys_dept_parent_name` (`parent_id`, `dept_name`),
    -- 列表页最常见的过滤条件
    KEY `idx_sys_dept_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 100
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='部门信息表';

-- -----------------------------------------------------------------------------
-- 说明（不执行，仅备忘）
--
-- 1) ancestors 的维护约定
--    祖级列表不含自身，顶级部门固定为 '0'；子部门 = 父部门 ancestors + ',' + 父部门 ID。
--    例如 100（总部）→ 101（研发部）→ 1011（后端组）：
--      100 的 ancestors = '0'
--      101 的 ancestors = '0,100'
--      1011 的 ancestors = '0,100,101'
--    新增时由 DeptServiceImpl 计算，客户端不可指定。
--    注意：当前模块只实现了「创建」。将来做「修改父部门」时必须级联更新整棵子树的 ancestors，
--    做「删除」时要先检查子部门和 sys_user.dept_id 的引用，否则会出现孤儿部门/用户。
--    用 ancestors 过滤「本部门及以下」时用 FIND_IN_SET(dept_id, ancestors) 或
--    ancestors LIKE '0,100,%'，不要按普通字符串截取。
--
-- 2) 为什么没有 del_flag
--    与 sys_user / sys_role 保持一致：统一物理删除。逻辑删除一旦某条查询漏写 del_flag = '0'，
--    已删部门仍会出现在部门树和下拉框里。真要改成逻辑删除，请三张表一起改，
--    并注意下面的唯一索引要改成 (parent_id, dept_name, del_flag) 之类的组合。
--
-- 3) 为什么给 (parent_id, dept_name) 加唯一索引
--    同一父部门下重名会让部门树、下拉选择器、按部门检索都无法区分是哪一个。
--    本项目是物理删除，所以删掉的部门名可以重新使用，唯一索引不会误伤。
--    并发创建同级同名部门时，Service 的预检存在时间窗口，最终由这个索引兜底。
--    如果业务上确实允许同级重名，删掉该索引并去掉 Service 里的预检即可。
--
-- 4) 为什么 parent_id 不单独建索引
--    唯一索引 uk_sys_dept_parent_name 的最左前缀就是 parent_id，
--    "查某父部门下的子部门"会直接走它，再建一个是重复维护成本。
--
-- 5) 初始部门（可选，按需手工执行）
--    AUTO_INCREMENT = 100 是给根部门留的位置；显式插入 dept_id = 100 后，
--    下一个自增 ID 从 101 开始。本脚本不预置数据（与 01/02 同理），需要时执行：
--
--    INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`,
--                            `leader`, `phone`, `email`, `status`, `remark`)
--    VALUES (100, 0, '0', '总部', 1, '管理员', '13800138000', 'admin@dadaz.com', '0', '顶级部门');
--
-- 6) 为什么 create_time / update_time 既由 MyBatis-Plus 填充、又设了 DEFAULT
--    双保险：正常走 MyBatis-Plus 时由 MybatisMetaObjectHandler 赋值；
--    手工 SQL、数据迁移、其他系统写入时由 DEFAULT 兜底。
--    update_time 的 ON UPDATE CURRENT_TIMESTAMP 还顺带补上了 MetaObjectHandler
--    的已知边界——用 LambdaUpdateWrapper 做纯条件更新时不会触发 updateFill，
--    此时由 MySQL 自动刷新更新时间。
--
-- 7) 与其他表的关系
--    sys_user.dept_id 指向本表，但不加物理外键：外键会让部门删除、数据迁移、
--    分库分表变得很麻烦，引用完整性统一放应用层校验。
--    角色的数据范围（sys_role.data_scope = 3/4）也是按本表 dept_id / ancestors 过滤。
--
-- 8) 为什么 leader / phone / email / remark 的默认值是 NULL 而不是 ''
--    Service 统一用 StringUtils.trimToNull() 把空白串归一成 null（配合 MyBatis-Plus
--    的 NOT_NULL 插入策略，字段为 null 时不进 INSERT 语句）。如果列上写 DEFAULT ''，
--    数据库会把"没传"又变回空串，导致同一列同时出现 null 和 '' 两种空值，
--    查询条件（IS NULL / = ''）就得写两遍。
--    sys_user / sys_role 里同类列沿用了 DEFAULT '' 的历史写法，本次不改；
--    后续统一时建议一起改成 DEFAULT NULL。
-- -----------------------------------------------------------------------------
