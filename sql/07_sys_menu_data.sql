-- =============================================================================
-- 07_sys_menu_data.sql：菜单初始化数据
-- 对应表：sys_menu（先执行 06_sys_menu.sql）
-- 目标库：dd-vue
--
-- 特点：
--   1. 显式指定 menu_id（2000 起），便于和后续的 sys_role_menu 授权数据对应；
--   2. 可重复执行：主键冲突时走 ON DUPLICATE KEY UPDATE，更新菜单字段而不是插重复行；
--   3. 不覆盖审计字段 create_by / create_time（保留首次写入的时间，便于排查）。
--
-- 执行前请确认：
--   1. 已在目标库上执行（USE `dd-vue`;）；
--   2. sys_menu 表已存在（06_sys_menu.sql）；
--   3. 本脚本不清理旧数据，只是 upsert 这 5 条；不需要时按文末的 DELETE 回滚。
-- =============================================================================

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,
                        `is_frame`, `is_cache`, `is_affix`, `menu_type`, `visible`, `status`,
                        `perms`, `icon`, `create_by`, `update_by`, `remark`)
VALUES (2000, '系统管理', 0, 1, 'system', NULL, 0, 0, 0, 'M', '0', '0', NULL, 'setting', 'system', 'system', '系统管理目录'),
       (2001, '用户管理', 2000, 1, 'user', '/system/user/index', 0, 0, 0, 'C', '0', '0', 'system:user:list', 'user', 'system', 'system', '系统用户管理'),
       (2002, '角色管理', 2000, 2, 'role', '/system/role/index', 0, 0, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'system', 'system', '角色与权限分配'),
       (2003, '菜单管理', 2000, 3, 'menu', '/system/menu/index', 0, 0, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'system', 'system', '菜单配置'),
       (2004, '部门管理', 2000, 4, 'dept', '/system/dept/index', 0, 0, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'system', 'system', '组织架构维护')
AS new
ON DUPLICATE KEY UPDATE
    `menu_name`  = new.`menu_name`,
    `parent_id`  = new.`parent_id`,
    `order_num`  = new.`order_num`,
    `path`       = new.`path`,
    `component`  = new.`component`,
    `is_frame`   = new.`is_frame`,
    `is_cache`   = new.`is_cache`,
    `is_affix`   = new.`is_affix`,
    `menu_type`  = new.`menu_type`,
    `visible`    = new.`visible`,
    `status`     = new.`status`,
    `perms`      = new.`perms`,
    `icon`       = new.`icon`,
    `update_by`  = new.`update_by`,
    `remark`     = new.`remark`;

-- 执行后自检（可选）：
--   SELECT menu_id, menu_name, parent_id, path, component, menu_type, perms, is_affix
--   FROM sys_menu WHERE menu_id BETWEEN 2000 AND 2004 ORDER BY parent_id, order_num;

-- -----------------------------------------------------------------------------
-- 说明（不执行，仅备忘）
--
-- 1) component 路径要和前端 views 目录保持一致
--    本例按 src/views 下的约定写成 system/user/index 等；前端用
--    () => import(`@/views/${component}.vue`) 映射。组件内部要用
--    defineOptions({ name: 'User' }) 声明与路由 name 相同的组件名，keep-alive 才生效
--    （前端 cacheName 由 component 派生，见 utils/route.ts 的 componentToName）。
--
-- 2) perms 与后端权限注解对应
--    后续登录/鉴权体系落地后，Controller 上会加
--    @SaCheckPermission("system:user:list") 等；这里的 perms 就是权限字符串来源。
--    按钮级权限（F 类型）按需再补，例如：
--
--    INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,
--                            `is_frame`, `is_cache`, `is_affix`, `menu_type`, `visible`, `status`,
--                            `perms`, `icon`)
--    VALUES (2101, '用户查询', 2001, 1, '', NULL, 0, 0, 0, 'F', '0', '0', 'system:user:query', '#'),
--           (2102, '用户新增', 2001, 2, '', NULL, 0, 0, 0, 'F', '0', '0', 'system:user:add', '#'),
--           (2103, '用户修改', 2001, 3, '', NULL, 0, 0, 0, 'F', '0', '0', 'system:user:edit', '#'),
--           (2104, '用户删除', 2001, 4, '', NULL, 0, 0, 0, 'F', '0', '0', 'system:user:remove', '#')
--    ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);
--
-- 3) 可选：首页（固定标签页，对应前端 RouteMeta.affix = true）
--    前端如果有首页组件 src/views/index.vue，可按需执行：
--
--    INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`,
--                            `is_frame`, `is_cache`, `is_affix`, `menu_type`, `visible`, `status`,
--                            `perms`, `icon`)
--    VALUES (2010, '首页', 0, 1, 'index', 'index', 0, 0, 1, 'C', '0', '0', NULL, 'home')
--    ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`);
--
-- 4) 回滚
--    删除本脚本写入的 5 条菜单（如果已经给角色授权，需要先清理 sys_role_menu）：
--
--    DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 2000 AND 2004;
--
-- 5) 为什么用 ON DUPLICATE KEY UPDATE 而不是 INSERT IGNORE
--    INSERT IGNORE 遇到主键冲突会静默跳过，改了菜单名/组件后重跑脚本不会生效；
--    ON DUPLICATE KEY UPDATE 会按脚本内容刷新可编辑字段，既能初始化也能纠正配置，
--    同时又不会像 REPLACE INTO 那样删行重建、把 create_time 冲掉。
-- -----------------------------------------------------------------------------
