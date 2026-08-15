-- V4: 技术管理员角色 + 监控中心菜单（职责分离：admin 管业务，tech_admin 管运行）

-- 1) 技术管理员角色（data_scope=1 为角色体系约定值，实际以 monitor:* 权限域为准）
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_time, update_time, deleted)
VALUES (4, '技术管理员', 'tech_admin', 4, '1', '0', NOW(), NOW(), 0);

-- 2) 技术管理员账号（默认密码 admin123，BCrypt 与 admin 相同哈希，部署后应立即修改）
INSERT INTO sys_user (id, username, password, nickname, user_type, status, create_time, update_time, deleted)
VALUES (2, 'tech_admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '技术管理员', 'TECH_ADMIN', '0', NOW(), NOW(), 0);

INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 4);

-- 3) 日志菜单权限串迁移 system:* → monitor:*（admin 有 *:*:* 通配不受影响）
UPDATE sys_menu SET perms = 'monitor:operlog:list' WHERE id = 15;
UPDATE sys_menu SET perms = 'monitor:loginlog:list' WHERE id = 16;

-- 4) 监控中心菜单（M 目录 + C 页面）
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, icon, status, create_time, update_time, deleted)
VALUES
(20, '监控中心', 0, 3, 'monitor', NULL, NULL, 'M', 'monitor', '0', NOW(), NOW(), 0),
(21, '运行概览', 20, 1, 'overview', 'monitor/overview/index', 'monitor:overview:list', 'C', 'dashboard', '0', NOW(), NOW(), 0);

-- 5) 操作日志/登录日志菜单挪入监控中心（原目录 14 移除）
UPDATE sys_menu SET parent_id = 20, order_num = 2 WHERE id = 15;
UPDATE sys_menu SET parent_id = 20, order_num = 3 WHERE id = 16;
DELETE FROM sys_role_menu WHERE menu_id = 14;
DELETE FROM sys_menu WHERE id = 14;

-- 6) 技术管理员关联监控菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (4, 20), (4, 21), (4, 15), (4, 16);
