-- =============================================================================
-- sys_menu：菜单权限表
-- 对应实体：com.dadaz.admin.module.system.menu.domain.entity.MenuEntity
-- 目标库：dd-vue（MySQL 8.0.46，库默认 utf8mb4 / utf8mb4_0900_ai_ci）
--
-- 执行前请确认：
--   1. 已在目标库上执行（USE `dd-vue`;），本脚本不切库，避免误伤其他库；
--   2. 本脚本未包含 DROP TABLE，重复执行会因表已存在而报错，这是有意的保护；
--   3. 字段默认值与 MenuEntity 的字段语义严格对应，改动任何一侧都要同步另一侧。
-- =============================================================================

CREATE TABLE IF NOT EXISTS `sys_menu`
(
    `menu_id`     bigint       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `menu_name`   varchar(50)  NOT NULL COMMENT '菜单名称',
    `parent_id`   bigint       NOT NULL DEFAULT 0 COMMENT '父菜单ID，0 表示顶级',
    `order_num`   int          NOT NULL DEFAULT 0 COMMENT '显示顺序，越小越靠前',
    `path`        varchar(200)          DEFAULT '' COMMENT '路由地址（前端 path，外链时放完整 URL）',
    `component`   varchar(255)          DEFAULT NULL COMMENT '组件路径（前端 import 路径，目录为空）',
    `query`       varchar(255)          DEFAULT NULL COMMENT '路由参数（JSON 字符串）',
    `is_frame`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否外链：0 否、1 是',
    `is_cache`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否缓存（keep-alive）：0 否、1 是',
    `is_affix`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否固定标签页（不可关闭）：0 否、1 是',
    `menu_type`   char(1)      NOT NULL COMMENT '菜单类型：M 目录、C 菜单、F 按钮',
    `visible`     char(1)      NOT NULL DEFAULT '0' COMMENT '显示状态：0 显示、1 隐藏',
    `status`      char(1)      NOT NULL DEFAULT '0' COMMENT '菜单状态：0 正常、1 停用',
    `perms`       varchar(100)          DEFAULT NULL COMMENT '权限标识，如 system:user:list',
    `icon`        varchar(100)          DEFAULT '#' COMMENT '菜单图标',
    `create_by`   varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      varchar(500)          DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`menu_id`),
    -- 菜单管理树、角色分配菜单都按"父节点 + 顺序"查，parent_id 是高频条件
    KEY `idx_sys_menu_parent_id` (`parent_id`),
    -- 构建前端菜单时通常只取启用菜单
    KEY `idx_sys_menu_status` (`status`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2000
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='菜单权限表';

-- -----------------------------------------------------------------------------
-- 说明（不执行，仅备忘）
--
-- 1) 树结构与"路由字段"的分工
--    parent_id = 0 表示顶级菜单；目录（M）下挂菜单（C），菜单（C）下挂按钮（F）。
--      - M 目录：只负责分组，component 必须为空，前端用布局组件包裹 children；
--      - C 菜单：必须同时有 path 和 component（外链除外：path 是完整 URL，不配 component），
--        path 是相对父级的路由地址，component 是前端页面组件路径（如 system/user/index）；
--      - F 按钮：只承载 perms 权限标识，不参与菜单/路由展示。
--    这些一致性由 MenuServiceImpl#createMenu 校验，数据库层只保证字段可空性。
--
-- 2) 为什么没有 del_flag
--    与 sys_user / sys_role / sys_dept 一致：统一物理删除。
--    菜单删除前必须先检查：有没有子菜单、有没有被 sys_role_menu 引用（关联表待建），
--    否则会留下点不进去的菜单或悬空的权限标识。当前只实现了创建，删除接口后续补。
--
-- 3) 为什么没有给 menu_name / perms 加唯一索引
--    菜单名在不同父节点下重复是正常的（两个模块下都有"查询"按钮）；
--    perms 也允许重复（同一个权限点被多个按钮复用）。真正的唯一性靠应用层约定，
--    不要加唯一索引，否则正常的菜单配置会插入失败。
--
-- 4) 外链（is_frame = 1）
--    path 存完整 URL（如 https://example.com）。外链不配置 component，
--    由前端按外链方式渲染（iframe 或新窗口打开）；
--    外链不参与 keep-alive 缓存。
--
-- 5) visible / status / affix 的区别
--    - visible = '1'（隐藏）：路由仍会下发，但带 hidden = true，用于"详情页/编辑页"
--      这类不显示在侧边栏、但需要路由存在的页面；
--    - status = '1'（停用）：菜单整体失效，管理树仍可见，但前端菜单/路由不应再使用；
--    - is_affix = '1'（固定标签页）：前端标签页不可关闭，一般只给首页这类菜单配置，
--      对应前端 RouteMeta.affix。
--    想从侧边栏隐藏用 visible，想下线一个功能用 status，想固定标签页用 is_affix，三者不要混用。
--
--    另外注意前端 RouteMeta 用的是 noCache（是否不缓存），而本表存的是 is_cache（是否缓存），
--    后端下发时会取反：meta.noCache = !is_cache。写反的话 keep-alive 行为会完全颠倒。
--
-- 6) 初始菜单（可选，按需手工执行）
--    本脚本不预置数据（与 01/02/03 同理）。最小示例：
--
--    INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`,
--                            `component`, `is_frame`, `is_cache`, `is_affix`, `menu_type`,
--                            `visible`, `status`, `perms`, `icon`, `remark`)
--    VALUES (2000, '系统管理', 0, 1, 'system', NULL, 0, 0, 0, 'M', '0', '0', NULL, 'setting', '目录'),
--           (2001, '用户管理', 2000, 1, 'user', 'system/user/index', 0, 0, 0, 'C', '0', '0', 'system:user:list', 'user', NULL),
--           (2002, '用户查询', 2001, 1, '', NULL, 0, 0, 0, 'F', '0', '0', 'system:user:query', '#', NULL),
--           (2003, '首页', 0, 1, 'index', 'index', 0, 0, 1, 'C', '0', '0', NULL, 'home', '固定标签页');
--
--    如果 06 已经在 dd-vue 执行过（表里没有 is_affix 列），补执行：
--      ALTER TABLE `sys_menu` ADD COLUMN `is_affix` tinyint(1) NOT NULL DEFAULT 0
--          COMMENT '是否固定标签页（不可关闭）：0 否、1 是' AFTER `is_cache`;
--
-- 7) 与角色/用户的关联
--    菜单与角色是多对多，落在 sys_role_menu（待建）。前端菜单/路由数据后续应按
--    "登录用户 → 角色 → 菜单"取并集；当前只提供管理端菜单树接口（/system/menu/tree）。
--
-- 8) 为什么 create_time / update_time 既由 MyBatis-Plus 填充、又设了 DEFAULT
--    与 01/02/03 一致：双保险，MetaObjectHandler 负责常规写入，DEFAULT 兜底
--    手工 SQL / 数据迁移。
-- -----------------------------------------------------------------------------
