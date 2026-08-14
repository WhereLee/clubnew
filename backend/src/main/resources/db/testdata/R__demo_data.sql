-- =============================================
-- 演示测试数据 · Club Flow
-- 生成日期: 2026-08-14
-- 对应规范: 测试数据规范 v1.0
-- 数据量: ~680 条记录 / 17 张表
-- 账号密码: 统一 admin123
-- =============================================

-- ===== 幂等清理（按依赖逆序） =====
DELETE FROM sys_oper_log WHERE id >= 16001;
DELETE FROM sys_login_log WHERE id >= 15001;
DELETE FROM fund_record WHERE id >= 13001;
DELETE FROM fund WHERE id >= 12001;
DELETE FROM user_like WHERE id >= 11001;
DELETE FROM `comment` WHERE id >= 10001;
DELETE FROM post WHERE id >= 9001;
DELETE FROM notice WHERE id >= 14001;
DELETE FROM activity_checkin WHERE id >= 8001;
DELETE FROM activity_signup WHERE id >= 7001;
DELETE FROM activity WHERE id >= 6001;
DELETE FROM recruit_record WHERE id >= 5001;
DELETE FROM recruit WHERE id >= 4001;
DELETE FROM club_member WHERE id >= 3001;
DELETE FROM club WHERE id >= 2001;
DELETE FROM sys_user_role WHERE user_id >= 1001;
DELETE FROM sys_user WHERE id >= 1001;

-- ===== 1. sys_user（28条: 18学生 + 10社长） =====
-- 密码统一 BCrypt(admin123): $2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2

INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1001, 'stu1001', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '陈明远', 'chenmy@example.com', '13800001001', 'STUDENT', '0', '2026-05-01 09:00:00', '2026-05-01 09:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1002, 'stu1002', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '王思琪', 'wangsq@example.com', '13800001002', 'STUDENT', '0', '2026-05-01 09:10:00', '2026-05-01 09:10:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1003, 'stu1003', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '李浩然', 'lihr@example.com', '13800001003', 'STUDENT', '0', '2026-05-02 08:30:00', '2026-05-02 08:30:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1004, 'stu1004', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '张雨萱', 'zhangyx@example.com', '13800001004', 'STUDENT', '0', '2026-05-02 09:00:00', '2026-05-02 09:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1005, 'stu1005', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '刘子涵', 'liuzh@example.com', '13800001005', 'STUDENT', '0', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1006, 'stu1006', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '赵晨曦', 'zhaochx@example.com', '13800001006', 'STUDENT', '0', '2026-05-03 10:30:00', '2026-05-03 10:30:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1007, 'stu1007', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '孙雅婷', 'sunyt@example.com', '13800001007', 'STUDENT', '0', '2026-05-04 08:00:00', '2026-05-04 08:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1008, 'stu1008', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '周文博', 'zhouwb@example.com', '13800001008', 'STUDENT', '0', '2026-05-04 09:30:00', '2026-05-04 09:30:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1009, 'stu1009', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '吴佳欣', 'wujx@example.com', '13800001009', 'STUDENT', '0', '2026-05-05 10:00:00', '2026-05-05 10:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1010, 'stu1010', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '郑思远', 'zhengsy@example.com', '13800001010', 'STUDENT', '0', '2026-05-05 11:00:00', '2026-05-05 11:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1011, 'stu1011', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '黄雨桐', 'huangyt@example.com', '13800001011', 'STUDENT', '0', '2026-05-06 08:00:00', '2026-05-06 08:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1012, 'stu1012', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '林子轩', 'linzx@example.com', '13800001012', 'STUDENT', '0', '2026-05-06 09:30:00', '2026-05-06 09:30:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1013, 'stu1013', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '杨紫薇', 'yangzw@example.com', '13800001013', 'STUDENT', '0', '2026-05-07 08:00:00', '2026-05-07 08:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1014, 'stu1014', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '徐浩宇', 'xuhy@example.com', '13800001014', 'STUDENT', '0', '2026-05-07 09:00:00', '2026-05-07 09:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1015, 'stu1015', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '马晓彤', 'maxt@example.com', '13800001015', 'STUDENT', '0', '2026-05-08 10:00:00', '2026-05-08 10:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1016, 'stu1016', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '胡俊杰', 'hujj@example.com', '13800001016', 'STUDENT', '0', '2026-05-08 11:00:00', '2026-05-08 11:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1017, 'stu1017', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '朱雅文', 'zhuyw@example.com', '13800001017', 'STUDENT', '0', '2026-05-09 08:30:00', '2026-05-09 08:30:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1018, 'stu1018', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '高翔宇', 'gaosxy@example.com', '13800001018', 'STUDENT', '0', '2026-05-09 09:00:00', '2026-05-09 09:00:00', 0);

-- 社长（1019-1028，user_type 仍为 STUDENT，身份由 club_member.member_role 表达）
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1019, 'pres1001', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '沈梦瑶', 'shenmy@example.com', '13800001019', 'STUDENT', '0', '2026-05-10 08:00:00', '2026-05-10 08:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1020, 'pres1002', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '陆思远', 'ludsy@example.com', '13800001020', 'STUDENT', '0', '2026-05-10 09:00:00', '2026-05-10 09:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1021, 'pres1003', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '唐浩然', 'tanghr@example.com', '13800001021', 'STUDENT', '0', '2026-05-10 10:00:00', '2026-05-10 10:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1022, 'pres1004', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '韩雨薇', 'hanyw@example.com', '13800001022', 'STUDENT', '0', '2026-05-10 11:00:00', '2026-05-10 11:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1023, 'pres1005', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '宋子豪', 'songzh@example.com', '13800001023', 'STUDENT', '0', '2026-05-11 08:00:00', '2026-05-11 08:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1024, 'pres1006', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '曹雅琳', 'caoyl@example.com', '13800001024', 'STUDENT', '0', '2026-05-11 09:00:00', '2026-05-11 09:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1025, 'pres1007', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '邓思远', 'dengsy@example.com', '13800001025', 'STUDENT', '0', '2026-05-11 10:00:00', '2026-05-11 10:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1026, 'pres1008', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '冯雨彤', 'fengyt@example.com', '13800001026', 'STUDENT', '0', '2026-05-11 11:00:00', '2026-05-11 11:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1027, 'pres1009', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '彭子轩', 'pengzx@example.com', '13800001027', 'STUDENT', '0', '2026-05-12 08:00:00', '2026-05-12 08:00:00', 0);
INSERT INTO sys_user (id, username, password, nickname, email, phone, user_type, status, create_time, update_time, deleted)
VALUES (1028, 'pres1010', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '蒋浩宇', 'jianghy@example.com', '13800001028', 'STUDENT', '0', '2026-05-12 09:00:00', '2026-05-12 09:00:00', 0);

-- ===== 2. sys_user_role（38条） =====
-- 社长: role_id=2(社长) + role_id=3(学生)
INSERT INTO sys_user_role (user_id, role_id) VALUES (1019, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1019, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1020, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1020, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1021, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1021, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1022, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1022, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1023, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1023, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1024, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1024, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1025, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1025, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1026, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1026, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1027, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1027, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1028, 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1028, 3);
-- 学生: role_id=3
INSERT INTO sys_user_role (user_id, role_id) VALUES (1001, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1002, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1003, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1004, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1005, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1006, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1007, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1008, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1009, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1010, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1011, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1012, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1013, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1014, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1015, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1016, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1017, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (1018, 3);

-- ===== 3. club（15条） =====
-- APPROVED ×10, PENDING ×2, SUSPENDED ×1, REJECTED ×1, DISSOLVED ×1

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2001, '晨光摄影社', 'CLUB2001', '用镜头记录校园四季，每周外拍交流，定期举办摄影展和讲座。', 'CULTURE', 1019, 'APPROVED', 6, 4, 1019, '2026-05-10 10:00:00', '2026-05-12 14:00:00', 1, '2026-05-10 10:00:00', '2026-05-12 14:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2002, '编程爱好者协会', 'CLUB2002', '以代码会友，定期组织Hackathon、技术分享和开源项目协作。', 'ACADEMIC', 1020, 'APPROVED', 7, 5, 1020, '2026-05-10 11:00:00', '2026-05-13 09:00:00', 1, '2026-05-10 11:00:00', '2026-05-13 09:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2003, '篮球社', 'CLUB2003', '热爱篮球的聚集地，每周训练+对抗赛，代表学校参加各级比赛。', 'SPORTS', 1021, 'APPROVED', 5, 4, 1021, '2026-05-11 08:00:00', '2026-05-14 10:00:00', 1, '2026-05-11 08:00:00', '2026-05-14 10:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2004, '青年志愿者协会', 'CLUB2004', '汇聚公益力量，组织敬老院服务、环保行动、支教帮扶等志愿活动。', 'VOLUNTEER', 1022, 'APPROVED', 8, 5, 1022, '2026-05-11 09:00:00', '2026-05-14 14:00:00', 1, '2026-05-11 09:00:00', '2026-05-14 14:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2005, '国学社', 'CLUB2005', '传承经典文化，定期举办诗词朗诵、读书会和书法体验活动。', 'CULTURE', 1023, 'APPROVED', 5, 3, 1023, '2026-05-12 10:00:00', '2026-05-15 09:00:00', 1, '2026-05-12 10:00:00', '2026-05-15 09:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2006, '机器人创客社', 'CLUB2006', '探索机器人技术与创客文化，拥有3D打印机和开发板，定期举办创意大赛。', 'ACADEMIC', 1024, 'APPROVED', 7, 5, 1024, '2026-05-12 11:00:00', '2026-05-16 10:00:00', 1, '2026-05-12 11:00:00', '2026-05-16 10:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2007, '足球社', 'CLUB2007', '绿茵场上的热血青春，每周训练+友谊赛，组队参加校际联赛。', 'SPORTS', 1025, 'APPROVED', 6, 4, 1025, '2026-05-13 08:00:00', '2026-05-17 11:00:00', 1, '2026-05-13 08:00:00', '2026-05-17 11:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2008, '街舞社', 'CLUB2008', 'Breaking、Popping、Locking全舞种交流，每周排练，校园晚会常驻嘉宾。', 'CULTURE', 1026, 'APPROVED', 5, 4, 1026, '2026-05-13 09:00:00', '2026-05-17 14:00:00', 1, '2026-05-13 09:00:00', '2026-05-17 14:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2009, '书画社', 'CLUB2009', '翰墨飘香，传承书画艺术，定期举办书法培训和作品展览。', 'CULTURE', 1027, 'APPROVED', 5, 3, 1027, '2026-05-14 10:00:00', '2026-05-18 09:00:00', 1, '2026-05-14 10:00:00', '2026-05-18 09:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, create_time, update_time, deleted)
VALUES (2010, '乒乓球社', 'CLUB2010', '国球风采，以球会友，每周训练+月度积分赛。', 'SPORTS', 1028, 'APPROVED', 6, 3, 1028, '2026-05-14 11:00:00', '2026-05-18 14:00:00', 1, '2026-05-14 11:00:00', '2026-05-18 14:00:00', 0);

-- PENDING 社团
INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, audit_remark, create_time, update_time, deleted)
VALUES (2011, '动漫社', 'CLUB2011', '二次元文化爱好者的聚集地，定期举办观影会、cosplay交流和漫画创作活动。', 'CULTURE', 1001, 'PENDING', 0, 0, 1001, '2026-08-01 10:00:00', NULL, NULL, '', '2026-08-01 10:00:00', '2026-08-01 10:00:00', 0);

INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, audit_remark, create_time, update_time, deleted)
VALUES (2012, '心理学社', 'CLUB2012', '关注心理健康，组织心理学读书会、压力管理讲座和朋辈辅导活动。', 'ACADEMIC', 1002, 'PENDING', 0, 0, 1002, '2026-08-05 09:00:00', NULL, NULL, '', '2026-08-05 09:00:00', '2026-08-05 09:00:00', 0);

-- SUSPENDED 社团
INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, audit_remark, create_time, update_time, deleted)
VALUES (2013, '吉他社', 'CLUB2013', '从零基础到弹唱达人，定期举办吉他教学和小型音乐会。因场地违规被暂停。', 'CULTURE', 1003, 'SUSPENDED', 3, 2, 1003, '2026-05-15 10:00:00', '2026-05-20 14:00:00', 1, '场地使用违规，暂停活动待整改', '2026-05-15 10:00:00', '2026-07-15 10:00:00', 0);

-- REJECTED 社团
INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, audit_remark, create_time, update_time, deleted)
VALUES (2014, '电竞社', 'CLUB2014', '电子竞技爱好者社团，组织校内电竞赛事和观赛活动。', 'SPORTS', 1004, 'REJECTED', 0, 0, 1004, '2026-06-01 10:00:00', '2026-06-05 09:00:00', 1, '当前学校已有类似体育竞技社团，建议合并申请', '2026-06-01 10:00:00', '2026-06-05 09:00:00', 0);

-- DISSOLVED 社团
INSERT INTO club (id, name, code, description, category, president_id, status, member_count, star_level, create_user_id, apply_time, audit_time, audit_user_id, audit_remark, create_time, update_time, deleted)
VALUES (2015, '汉服社', 'CLUB2015', '汉服文化推广与传统礼仪传承。因社长毕业且无人接任，主动申请注销。', 'CULTURE', 1005, 'DISSOLVED', 0, 1, 1005, '2026-04-01 10:00:00', '2026-04-05 14:00:00', 1, '社长毕业，无人接任，同意注销', '2026-04-01 10:00:00', '2026-07-01 10:00:00', 0);

-- ===== 4. club_member（66条） =====
-- 每个 APPROVED 社团: PRESIDENT×1 + VICE×1 + MEMBER×3-6 + 可选 QUIT/REMOVED
-- PENDING 社团: 仅 PRESIDENT(PENDING)
-- SUSPENDED 社团: PRESIDENT + MEMBER（成员保留但活动暂停）
-- REJECTED/DISSOLVED: 无成员

-- --- 社团 2001 晨光摄影社 (president=1019, member_count=6) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3001, 2001, 1019, 'PRESIDENT', 'ACTIVE', '2026-05-10 10:05:00', '2026-05-12 14:30:00', 1, '2026-05-10 10:05:00', '2026-05-12 14:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3002, 2001, 1001, 'VICE', 'ACTIVE', '2026-05-13 09:00:00', '2026-05-15 10:00:00', 1019, '2026-05-13 09:00:00', '2026-05-15 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3003, 2001, 1002, 'MEMBER', 'ACTIVE', '2026-05-13 10:00:00', '2026-05-15 14:00:00', 1019, '2026-05-13 10:00:00', '2026-05-15 14:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3004, 2001, 1003, 'MEMBER', 'ACTIVE', '2026-05-14 08:00:00', '2026-05-16 09:00:00', 1019, '2026-05-14 08:00:00', '2026-05-16 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3005, 2001, 1004, 'MEMBER', 'ACTIVE', '2026-05-14 09:00:00', '2026-05-16 10:00:00', 1019, '2026-05-14 09:00:00', '2026-05-16 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3006, 2001, 1005, 'MEMBER', 'ACTIVE', '2026-05-15 08:00:00', '2026-05-17 09:00:00', 1019, '2026-05-15 08:00:00', '2026-05-17 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3007, 2001, 1006, 'MEMBER', 'QUIT', '2026-05-15 09:00:00', '2026-05-17 14:00:00', 1019, '2026-05-15 09:00:00', '2026-07-01 10:00:00', 0);

-- --- 社团 2002 编程爱好者协会 (president=1020, member_count=7) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3008, 2002, 1020, 'PRESIDENT', 'ACTIVE', '2026-05-10 11:05:00', '2026-05-13 09:30:00', 1, '2026-05-10 11:05:00', '2026-05-13 09:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3009, 2002, 1007, 'VICE', 'ACTIVE', '2026-05-14 09:00:00', '2026-05-16 10:00:00', 1020, '2026-05-14 09:00:00', '2026-05-16 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3010, 2002, 1008, 'MEMBER', 'ACTIVE', '2026-05-14 10:00:00', '2026-05-16 14:00:00', 1020, '2026-05-14 10:00:00', '2026-05-16 14:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3011, 2002, 1009, 'MEMBER', 'ACTIVE', '2026-05-15 08:00:00', '2026-05-17 09:00:00', 1020, '2026-05-15 08:00:00', '2026-05-17 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3012, 2002, 1010, 'MEMBER', 'ACTIVE', '2026-05-15 09:00:00', '2026-05-17 10:00:00', 1020, '2026-05-15 09:00:00', '2026-05-17 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3013, 2002, 1011, 'MEMBER', 'ACTIVE', '2026-05-16 08:00:00', '2026-05-18 09:00:00', 1020, '2026-05-16 08:00:00', '2026-05-18 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3014, 2002, 1012, 'MEMBER', 'ACTIVE', '2026-05-16 09:00:00', '2026-05-18 10:00:00', 1020, '2026-05-16 09:00:00', '2026-05-18 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3015, 2002, 1006, 'MEMBER', 'REMOVED', '2026-05-16 10:00:00', '2026-05-18 14:00:00', 1020, '2026-05-16 10:00:00', '2026-07-15 10:00:00', 0);

-- --- 社团 2003 篮球社 (president=1021, member_count=5) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3016, 2003, 1021, 'PRESIDENT', 'ACTIVE', '2026-05-11 08:05:00', '2026-05-14 10:30:00', 1, '2026-05-11 08:05:00', '2026-05-14 10:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3017, 2003, 1013, 'VICE', 'ACTIVE', '2026-05-15 08:00:00', '2026-05-17 09:00:00', 1021, '2026-05-15 08:00:00', '2026-05-17 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3018, 2003, 1014, 'MEMBER', 'ACTIVE', '2026-05-15 09:00:00', '2026-05-17 10:00:00', 1021, '2026-05-15 09:00:00', '2026-05-17 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3019, 2003, 1015, 'MEMBER', 'ACTIVE', '2026-05-16 08:00:00', '2026-05-18 09:00:00', 1021, '2026-05-16 08:00:00', '2026-05-18 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3020, 2003, 1016, 'MEMBER', 'ACTIVE', '2026-05-16 09:00:00', '2026-05-18 10:00:00', 1021, '2026-05-16 09:00:00', '2026-05-18 10:00:00', 0);

-- --- 社团 2004 青年志愿者协会 (president=1022, member_count=8) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3021, 2004, 1022, 'PRESIDENT', 'ACTIVE', '2026-05-11 09:05:00', '2026-05-14 14:30:00', 1, '2026-05-11 09:05:00', '2026-05-14 14:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3022, 2004, 1017, 'VICE', 'ACTIVE', '2026-05-15 08:00:00', '2026-05-17 09:00:00', 1022, '2026-05-15 08:00:00', '2026-05-17 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3023, 2004, 1018, 'MEMBER', 'ACTIVE', '2026-05-15 09:00:00', '2026-05-17 10:00:00', 1022, '2026-05-15 09:00:00', '2026-05-17 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3024, 2004, 1001, 'MEMBER', 'ACTIVE', '2026-05-16 08:00:00', '2026-05-18 09:00:00', 1022, '2026-05-16 08:00:00', '2026-05-18 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3025, 2004, 1002, 'MEMBER', 'ACTIVE', '2026-05-16 09:00:00', '2026-05-18 10:00:00', 1022, '2026-05-16 09:00:00', '2026-05-18 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3026, 2004, 1003, 'MEMBER', 'ACTIVE', '2026-05-17 08:00:00', '2026-05-19 09:00:00', 1022, '2026-05-17 08:00:00', '2026-05-19 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3027, 2004, 1004, 'MEMBER', 'ACTIVE', '2026-05-17 09:00:00', '2026-05-19 10:00:00', 1022, '2026-05-17 09:00:00', '2026-05-19 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3028, 2004, 1005, 'MEMBER', 'ACTIVE', '2026-05-18 08:00:00', '2026-05-20 09:00:00', 1022, '2026-05-18 08:00:00', '2026-05-20 09:00:00', 0);

-- --- 社团 2005 国学社 (president=1023, member_count=5) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3029, 2005, 1023, 'PRESIDENT', 'ACTIVE', '2026-05-12 10:05:00', '2026-05-15 09:30:00', 1, '2026-05-12 10:05:00', '2026-05-15 09:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3030, 2005, 1006, 'VICE', 'ACTIVE', '2026-05-16 08:00:00', '2026-05-18 09:00:00', 1023, '2026-05-16 08:00:00', '2026-05-18 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3031, 2005, 1007, 'MEMBER', 'ACTIVE', '2026-05-16 09:00:00', '2026-05-18 10:00:00', 1023, '2026-05-16 09:00:00', '2026-05-18 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3032, 2005, 1008, 'MEMBER', 'ACTIVE', '2026-05-17 08:00:00', '2026-05-19 09:00:00', 1023, '2026-05-17 08:00:00', '2026-05-19 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3033, 2005, 1009, 'MEMBER', 'ACTIVE', '2026-05-17 09:00:00', '2026-05-19 10:00:00', 1023, '2026-05-17 09:00:00', '2026-05-19 10:00:00', 0);

-- --- 社团 2006 机器人创客社 (president=1024, member_count=7) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3034, 2006, 1024, 'PRESIDENT', 'ACTIVE', '2026-05-12 11:05:00', '2026-05-16 10:30:00', 1, '2026-05-12 11:05:00', '2026-05-16 10:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3035, 2006, 1010, 'VICE', 'ACTIVE', '2026-05-16 08:00:00', '2026-05-18 09:00:00', 1024, '2026-05-16 08:00:00', '2026-05-18 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3036, 2006, 1011, 'MEMBER', 'ACTIVE', '2026-05-16 09:00:00', '2026-05-18 10:00:00', 1024, '2026-05-16 09:00:00', '2026-05-18 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3037, 2006, 1012, 'MEMBER', 'ACTIVE', '2026-05-17 08:00:00', '2026-05-19 09:00:00', 1024, '2026-05-17 08:00:00', '2026-05-19 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3038, 2006, 1013, 'MEMBER', 'ACTIVE', '2026-05-17 09:00:00', '2026-05-19 10:00:00', 1024, '2026-05-17 09:00:00', '2026-05-19 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3039, 2006, 1014, 'MEMBER', 'ACTIVE', '2026-05-18 08:00:00', '2026-05-20 09:00:00', 1024, '2026-05-18 08:00:00', '2026-05-20 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3040, 2006, 1015, 'MEMBER', 'ACTIVE', '2026-05-18 09:00:00', '2026-05-20 10:00:00', 1024, '2026-05-18 09:00:00', '2026-05-20 10:00:00', 0);

-- --- 社团 2007 足球社 (president=1025, member_count=6) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3041, 2007, 1025, 'PRESIDENT', 'ACTIVE', '2026-05-13 08:05:00', '2026-05-17 11:30:00', 1, '2026-05-13 08:05:00', '2026-05-17 11:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3042, 2007, 1016, 'VICE', 'ACTIVE', '2026-05-17 08:00:00', '2026-05-19 09:00:00', 1025, '2026-05-17 08:00:00', '2026-05-19 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3043, 2007, 1017, 'MEMBER', 'ACTIVE', '2026-05-17 09:00:00', '2026-05-19 10:00:00', 1025, '2026-05-17 09:00:00', '2026-05-19 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3044, 2007, 1018, 'MEMBER', 'ACTIVE', '2026-05-18 08:00:00', '2026-05-20 09:00:00', 1025, '2026-05-18 08:00:00', '2026-05-20 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3045, 2007, 1001, 'MEMBER', 'ACTIVE', '2026-05-18 09:00:00', '2026-05-20 10:00:00', 1025, '2026-05-18 09:00:00', '2026-05-20 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3046, 2007, 1002, 'MEMBER', 'ACTIVE', '2026-05-19 08:00:00', '2026-05-21 09:00:00', 1025, '2026-05-19 08:00:00', '2026-05-21 09:00:00', 0);

-- --- 社团 2008 街舞社 (president=1026, member_count=5) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3047, 2008, 1026, 'PRESIDENT', 'ACTIVE', '2026-05-13 09:05:00', '2026-05-17 14:30:00', 1, '2026-05-13 09:05:00', '2026-05-17 14:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3048, 2008, 1003, 'VICE', 'ACTIVE', '2026-05-18 08:00:00', '2026-05-20 09:00:00', 1026, '2026-05-18 08:00:00', '2026-05-20 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3049, 2008, 1004, 'MEMBER', 'ACTIVE', '2026-05-18 09:00:00', '2026-05-20 10:00:00', 1026, '2026-05-18 09:00:00', '2026-05-20 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3050, 2008, 1005, 'MEMBER', 'ACTIVE', '2026-05-19 08:00:00', '2026-05-21 09:00:00', 1026, '2026-05-19 08:00:00', '2026-05-21 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3051, 2008, 1006, 'MEMBER', 'ACTIVE', '2026-05-19 09:00:00', '2026-05-21 10:00:00', 1026, '2026-05-19 09:00:00', '2026-05-21 10:00:00', 0);

-- --- 社团 2009 书画社 (president=1027, member_count=5) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3052, 2009, 1027, 'PRESIDENT', 'ACTIVE', '2026-05-14 10:05:00', '2026-05-18 09:30:00', 1, '2026-05-14 10:05:00', '2026-05-18 09:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3053, 2009, 1007, 'VICE', 'ACTIVE', '2026-05-18 08:00:00', '2026-05-20 09:00:00', 1027, '2026-05-18 08:00:00', '2026-05-20 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3054, 2009, 1008, 'MEMBER', 'ACTIVE', '2026-05-18 09:00:00', '2026-05-20 10:00:00', 1027, '2026-05-18 09:00:00', '2026-05-20 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3055, 2009, 1009, 'MEMBER', 'ACTIVE', '2026-05-19 08:00:00', '2026-05-21 09:00:00', 1027, '2026-05-19 08:00:00', '2026-05-21 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3056, 2009, 1010, 'MEMBER', 'ACTIVE', '2026-05-19 09:00:00', '2026-05-21 10:00:00', 1027, '2026-05-19 09:00:00', '2026-05-21 10:00:00', 0);

-- --- 社团 2010 乒乓球社 (president=1028, member_count=6) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3057, 2010, 1028, 'PRESIDENT', 'ACTIVE', '2026-05-14 11:05:00', '2026-05-18 14:30:00', 1, '2026-05-14 11:05:00', '2026-05-18 14:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3058, 2010, 1011, 'VICE', 'ACTIVE', '2026-05-19 08:00:00', '2026-05-21 09:00:00', 1028, '2026-05-19 08:00:00', '2026-05-21 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3059, 2010, 1012, 'MEMBER', 'ACTIVE', '2026-05-19 09:00:00', '2026-05-21 10:00:00', 1028, '2026-05-19 09:00:00', '2026-05-21 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3060, 2010, 1013, 'MEMBER', 'ACTIVE', '2026-05-20 08:00:00', '2026-05-22 09:00:00', 1028, '2026-05-20 08:00:00', '2026-05-22 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3061, 2010, 1014, 'MEMBER', 'ACTIVE', '2026-05-20 09:00:00', '2026-05-22 10:00:00', 1028, '2026-05-20 09:00:00', '2026-05-22 10:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3062, 2010, 1015, 'MEMBER', 'ACTIVE', '2026-05-21 08:00:00', '2026-05-23 09:00:00', 1028, '2026-05-21 08:00:00', '2026-05-23 09:00:00', 0);

-- --- 社团 2011 动漫社 PENDING (member_count=0) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3063, 2011, 1001, 'PRESIDENT', 'PENDING', '2026-08-01 10:05:00', NULL, NULL, '2026-08-01 10:05:00', '2026-08-01 10:05:00', 0);

-- --- 社团 2012 心理学社 PENDING (member_count=0) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3064, 2012, 1002, 'PRESIDENT', 'PENDING', '2026-08-05 09:05:00', NULL, NULL, '2026-08-05 09:05:00', '2026-08-05 09:05:00', 0);

-- --- 社团 2013 吉他社 SUSPENDED (member_count=3) ---
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3065, 2013, 1003, 'PRESIDENT', 'ACTIVE', '2026-05-15 10:05:00', '2026-05-20 14:30:00', 1, '2026-05-15 10:05:00', '2026-05-20 14:30:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3066, 2013, 1009, 'MEMBER', 'ACTIVE', '2026-05-20 08:00:00', '2026-05-22 09:00:00', 1003, '2026-05-20 08:00:00', '2026-05-22 09:00:00', 0);
INSERT INTO club_member (id, club_id, user_id, member_role, status, apply_time, join_time, audit_user_id, create_time, update_time, deleted)
VALUES (3067, 2013, 1010, 'MEMBER', 'ACTIVE', '2026-05-20 09:00:00', '2026-05-22 10:00:00', 1003, '2026-05-20 09:00:00', '2026-05-22 10:00:00', 0);

-- 社团 2014 电竞社 REJECTED: 无成员
-- 社团 2015 汉服社 DISSOLVED: 无成员

-- ===== 5. recruit（14条） =====
-- IN_PROGRESS ×5, ENDED ×4, NOT_STARTED ×3, CANCELLED ×2

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4001, 2001, '晨光摄影社2026秋季纳新', '热爱摄影、有无基础均可报名。提交3张原创摄影作品作为面试材料。', 25, 4, '2026-08-01 09:00:00', '2026-09-15 18:00:00', 'IN_PROGRESS', '热爱摄影，能保证每周参加一次外拍活动', '2026-07-25 10:00:00', '2026-08-01 09:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4002, 2001, '晨光摄影社2026春季纳新', '上学期春季纳新，已圆满结束。', 20, 6, '2026-03-01 09:00:00', '2026-04-15 18:00:00', 'ENDED', '热爱摄影，提交作品集', '2026-02-20 10:00:00', '2026-04-20 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4003, 2002, '编程协会2026秋季招新', '欢迎所有对编程感兴趣的同学！不限专业，零基础也欢迎。', 50, 6, '2026-08-05 09:00:00', '2026-09-20 18:00:00', 'IN_PROGRESS', '对编程有兴趣，能参加周末Hackathon', '2026-07-28 10:00:00', '2026-08-05 09:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4004, 2002, '编程协会2026春季招新', '上学期春季招新，已圆满结束。', 40, 7, '2026-03-05 09:00:00', '2026-04-20 18:00:00', 'ENDED', '有编程基础优先，需完成在线测评', '2026-02-25 10:00:00', '2026-04-25 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4005, 2004, '志愿者协会2026秋季招募', '用行动传递温暖，欢迎热心公益的同学加入！', 60, 8, '2026-08-10 09:00:00', '2026-09-25 18:00:00', 'IN_PROGRESS', '热心公益，每月至少参加一次志愿服务', '2026-08-01 10:00:00', '2026-08-10 09:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4006, 2004, '志愿者协会2026春季招募', '上学期春季招募，已圆满结束。', 50, 5, '2026-03-10 09:00:00', '2026-04-25 18:00:00', 'ENDED', '热心公益，能坚持参与', '2026-03-01 10:00:00', '2026-04-30 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4007, 2006, '创客社2026秋季招新', '对机器人、3D打印、Arduino感兴趣的同学看过来！', 30, 5, '2026-08-08 09:00:00', '2026-09-18 18:00:00', 'IN_PROGRESS', '对创客文化感兴趣，动手能力强', '2026-07-30 10:00:00', '2026-08-08 09:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4008, 2008, '街舞社2026秋季纳新', 'Breaking、Popping、Locking，总有一款适合你！零基础教学班同步开放。', 25, 3, '2026-08-12 09:00:00', '2026-09-22 18:00:00', 'IN_PROGRESS', '热爱舞蹈，能保证每周参加排练', '2026-08-05 10:00:00', '2026-08-12 09:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4009, 2009, '书画社2026秋季纳新', '传承翰墨文化，欢迎书法和国画爱好者。', 15, 5, '2026-06-01 09:00:00', '2026-07-15 18:00:00', 'ENDED', '对书画有兴趣，自带笔墨更佳', '2026-05-25 10:00:00', '2026-07-20 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4010, 2003, '篮球社秋季纳新', '新生杯在即，篮球社招兵买马！', 30, 0, '2026-09-01 09:00:00', '2026-09-20 18:00:00', 'NOT_STARTED', '热爱篮球，能参加训练和比赛', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4011, 2005, '国学社秋季纳新', '喜欢传统文化的同学看过来！', 20, 0, '2026-08-20 09:00:00', '2026-09-10 18:00:00', 'CANCELLED', '对国学有基本了解', '2026-08-05 10:00:00', '2026-08-12 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4012, 2013, '吉他社2026春季纳新', '春季纳新已结束。', 20, 4, '2026-03-15 09:00:00', '2026-04-30 18:00:00', 'ENDED', '喜欢音乐，有无基础均可', '2026-03-05 10:00:00', '2026-05-05 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4013, 2010, '乒乓球社秋季招新', '以球会友，欢迎乒乓球爱好者！', 20, 0, '2026-09-05 09:00:00', '2026-09-25 18:00:00', 'NOT_STARTED', '喜欢乒乓球运动', '2026-08-12 10:00:00', '2026-08-12 10:00:00', 0);

INSERT INTO recruit (id, club_id, title, description, quota, applied_count, start_time, end_time, status, requirements, create_time, update_time, deleted)
VALUES (4014, 2013, '吉他社2026秋季纳新', '因社团暂停，纳新已取消。', 20, 0, '2026-08-15 09:00:00', '2026-09-30 18:00:00', 'CANCELLED', '喜欢音乐', '2026-08-01 10:00:00', '2026-08-10 10:00:00', 0);

-- ===== 6. recruit_record（57条） =====
-- PENDING: 正在等待面试的报名者
-- PASSED: 面试通过（有面试评语）
-- FAILED: 面试未通过

-- recruit 4001 晨光秋季 IN_PROGRESS (applied=4: 4 PENDING)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5001, 4001, 1011, 'PENDING', '2026-08-02 10:00:00', NULL, '', '2026-08-02 10:00:00', '2026-08-02 10:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5002, 4001, 1012, 'PENDING', '2026-08-03 14:00:00', NULL, '', '2026-08-03 14:00:00', '2026-08-03 14:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5003, 4001, 1016, 'PENDING', '2026-08-05 09:00:00', NULL, '', '2026-08-05 09:00:00', '2026-08-05 09:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5004, 4001, 1018, 'PENDING', '2026-08-06 16:00:00', NULL, '', '2026-08-06 16:00:00', '2026-08-06 16:00:00', 0);

-- recruit 4002 晨光春季 ENDED (applied=6: 6 PASSED + 2 FAILED)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5005, 4002, 1001, 'PASSED', '2026-03-05 10:00:00', '2026-03-20 14:00:00', '摄影作品集出色，构图意识好，录取为副社长', '2026-03-05 10:00:00', '2026-03-20 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5006, 4002, 1002, 'PASSED', '2026-03-06 11:00:00', '2026-03-20 14:30:00', '摄影热情高，后期处理能力强，录取', '2026-03-06 11:00:00', '2026-03-20 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5007, 4002, 1003, 'PASSED', '2026-03-07 09:00:00', '2026-03-21 10:00:00', '有潜力，器材专业，录取', '2026-03-07 09:00:00', '2026-03-21 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5008, 4002, 1004, 'PASSED', '2026-03-08 14:00:00', '2026-03-21 10:30:00', '作品风格独特，有想法，录取', '2026-03-08 14:00:00', '2026-03-21 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5009, 4002, 1005, 'PASSED', '2026-03-09 10:00:00', '2026-03-22 14:00:00', '经验丰富，人像拍摄出色，录取', '2026-03-09 10:00:00', '2026-03-22 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5010, 4002, 1006, 'PASSED', '2026-03-10 11:00:00', '2026-03-22 14:30:00', '基础扎实，录取（后因个人原因退出）', '2026-03-10 11:00:00', '2026-03-22 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5011, 4002, 1014, 'FAILED', '2026-03-11 09:00:00', '2026-03-23 10:00:00', '作品集不够丰富，建议多练习后下次再报', '2026-03-11 09:00:00', '2026-03-23 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5012, 4002, 1015, 'FAILED', '2026-03-12 14:00:00', '2026-03-23 10:30:00', '时间安排与外拍活动冲突，无法保证出勤', '2026-03-12 14:00:00', '2026-03-23 11:30:00', 0);

-- recruit 4003 编程秋季 IN_PROGRESS (applied=6: 6 PENDING)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5013, 4003, 1013, 'PENDING', '2026-08-06 10:00:00', NULL, '', '2026-08-06 10:00:00', '2026-08-06 10:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5014, 4003, 1014, 'PENDING', '2026-08-07 11:00:00', NULL, '', '2026-08-07 11:00:00', '2026-08-07 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5015, 4003, 1015, 'PENDING', '2026-08-08 09:00:00', NULL, '', '2026-08-08 09:00:00', '2026-08-08 09:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5016, 4003, 1016, 'PENDING', '2026-08-09 14:00:00', NULL, '', '2026-08-09 14:00:00', '2026-08-09 14:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5017, 4003, 1017, 'PENDING', '2026-08-10 10:00:00', NULL, '', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5018, 4003, 1018, 'PENDING', '2026-08-11 16:00:00', NULL, '', '2026-08-11 16:00:00', '2026-08-11 16:00:00', 0);

-- recruit 4004 编程春季 ENDED (applied=7: 7 PASSED + 3 FAILED)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5019, 4004, 1007, 'PASSED', '2026-03-10 10:00:00', '2026-03-25 14:00:00', '算法能力强，有ACM经验，录取为副社长', '2026-03-10 10:00:00', '2026-03-25 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5020, 4004, 1008, 'PASSED', '2026-03-11 11:00:00', '2026-03-25 14:30:00', 'Python基础扎实，数据分析有经验，录取', '2026-03-11 11:00:00', '2026-03-25 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5021, 4004, 1009, 'PASSED', '2026-03-12 09:00:00', '2026-03-26 10:00:00', '前端开发经验，React项目经验丰富，录取', '2026-03-12 09:00:00', '2026-03-26 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5022, 4004, 1010, 'PASSED', '2026-03-13 14:00:00', '2026-03-26 10:30:00', '全栈能力突出，有独立项目经验，录取', '2026-03-13 14:00:00', '2026-03-26 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5023, 4004, 1011, 'PASSED', '2026-03-14 10:00:00', '2026-03-27 14:00:00', '数据结构理解深入，LeetCode刷题量200+，录取', '2026-03-14 10:00:00', '2026-03-27 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5024, 4004, 1012, 'PASSED', '2026-03-15 11:00:00', '2026-03-27 14:30:00', 'Linux运维经验丰富，后端开发能力扎实，录取', '2026-03-15 11:00:00', '2026-03-27 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5025, 4004, 1006, 'PASSED', '2026-03-16 09:00:00', '2026-03-28 10:00:00', '有编程基础，态度积极，录取（后被移除）', '2026-03-16 09:00:00', '2026-03-28 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5026, 4004, 1016, 'FAILED', '2026-03-17 14:00:00', '2026-03-29 10:00:00', '编程基础薄弱，建议先学习入门课程', '2026-03-17 14:00:00', '2026-03-29 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5027, 4004, 1017, 'FAILED', '2026-03-18 10:00:00', '2026-03-29 10:30:00', '时间安排与课程冲突严重，无法参加活动', '2026-03-18 10:00:00', '2026-03-29 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5028, 4004, 1018, 'FAILED', '2026-03-19 11:00:00', '2026-03-30 14:00:00', '测评成绩未达标，建议加强基础后再次申请', '2026-03-19 11:00:00', '2026-03-30 15:00:00', 0);

-- recruit 4005 志愿者秋季 IN_PROGRESS (applied=8: 8 PENDING)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5029, 4005, 1011, 'PENDING', '2026-08-11 09:00:00', NULL, '', '2026-08-11 09:00:00', '2026-08-11 09:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5030, 4005, 1012, 'PENDING', '2026-08-11 10:00:00', NULL, '', '2026-08-11 10:00:00', '2026-08-11 10:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5031, 4005, 1013, 'PENDING', '2026-08-12 08:00:00', NULL, '', '2026-08-12 08:00:00', '2026-08-12 08:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5032, 4005, 1014, 'PENDING', '2026-08-12 09:00:00', NULL, '', '2026-08-12 09:00:00', '2026-08-12 09:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5033, 4005, 1015, 'PENDING', '2026-08-12 14:00:00', NULL, '', '2026-08-12 14:00:00', '2026-08-12 14:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5034, 4005, 1016, 'PENDING', '2026-08-13 10:00:00', NULL, '', '2026-08-13 10:00:00', '2026-08-13 10:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5035, 4005, 1017, 'PENDING', '2026-08-13 11:00:00', NULL, '', '2026-08-13 11:00:00', '2026-08-13 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5036, 4005, 1018, 'PENDING', '2026-08-13 14:00:00', NULL, '', '2026-08-13 14:00:00', '2026-08-13 14:00:00', 0);

-- recruit 4006 志愿者春季 ENDED (applied=5: 5 PASSED + 2 FAILED)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5037, 4006, 1001, 'PASSED', '2026-03-15 10:00:00', '2026-03-30 14:00:00', '志愿服务经验丰富，态度认真，录取', '2026-03-15 10:00:00', '2026-03-30 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5038, 4006, 1002, 'PASSED', '2026-03-16 11:00:00', '2026-03-30 14:30:00', '热心公益，沟通能力强，录取', '2026-03-16 11:00:00', '2026-03-30 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5039, 4006, 1003, 'PASSED', '2026-03-17 09:00:00', '2026-03-31 10:00:00', '有急救证书，特长突出，录取', '2026-03-17 09:00:00', '2026-03-31 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5040, 4006, 1004, 'PASSED', '2026-03-18 14:00:00', '2026-03-31 10:30:00', '积极参与社区服务，有组织能力，录取', '2026-03-18 14:00:00', '2026-03-31 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5041, 4006, 1005, 'PASSED', '2026-03-19 10:00:00', '2026-04-01 14:00:00', '志愿服务意愿强烈，时间充裕，录取', '2026-03-19 10:00:00', '2026-04-01 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5042, 4006, 1013, 'FAILED', '2026-03-20 11:00:00', '2026-04-02 10:00:00', '时间安排不稳定，无法保证出勤率', '2026-03-20 11:00:00', '2026-04-02 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5043, 4006, 1014, 'FAILED', '2026-03-21 09:00:00', '2026-04-02 10:30:00', '对志愿服务理解不足，建议先参与体验活动', '2026-03-21 09:00:00', '2026-04-02 11:30:00', 0);

-- recruit 4007 创客社秋季 IN_PROGRESS (applied=5: 5 PENDING)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5044, 4007, 1011, 'PENDING', '2026-08-09 10:00:00', NULL, '', '2026-08-09 10:00:00', '2026-08-09 10:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5045, 4007, 1013, 'PENDING', '2026-08-10 11:00:00', NULL, '', '2026-08-10 11:00:00', '2026-08-10 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5046, 4007, 1016, 'PENDING', '2026-08-11 09:00:00', NULL, '', '2026-08-11 09:00:00', '2026-08-11 09:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5047, 4007, 1017, 'PENDING', '2026-08-12 14:00:00', NULL, '', '2026-08-12 14:00:00', '2026-08-12 14:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5048, 4007, 1018, 'PENDING', '2026-08-13 10:00:00', NULL, '', '2026-08-13 10:00:00', '2026-08-13 10:00:00', 0);

-- recruit 4008 街舞社秋季 IN_PROGRESS (applied=3: 3 PENDING)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5049, 4008, 1011, 'PENDING', '2026-08-13 09:00:00', NULL, '', '2026-08-13 09:00:00', '2026-08-13 09:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5050, 4008, 1014, 'PENDING', '2026-08-13 14:00:00', NULL, '', '2026-08-13 14:00:00', '2026-08-13 14:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5051, 4008, 1017, 'PENDING', '2026-08-14 09:00:00', NULL, '', '2026-08-14 09:00:00', '2026-08-14 09:00:00', 0);

-- recruit 4009 书画社秋季 ENDED (applied=5: 5 PASSED + 3 FAILED)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5052, 4009, 1011, 'PASSED', '2026-06-05 10:00:00', '2026-06-20 14:00:00', '书法功底扎实，楷书行书均有涉猎，录取', '2026-06-05 10:00:00', '2026-06-20 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5053, 4009, 1012, 'PASSED', '2026-06-06 11:00:00', '2026-06-20 14:30:00', '国画基础好，山水画有韵味，录取', '2026-06-06 11:00:00', '2026-06-20 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5054, 4009, 1015, 'PASSED', '2026-06-07 09:00:00', '2026-06-21 10:00:00', '热爱书画，有学习热情，录取', '2026-06-07 09:00:00', '2026-06-21 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5055, 4009, 1016, 'PASSED', '2026-06-08 14:00:00', '2026-06-21 10:30:00', '篆刻有基础，多才多艺，录取', '2026-06-08 14:00:00', '2026-06-21 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5056, 4009, 1018, 'PASSED', '2026-06-09 10:00:00', '2026-06-22 14:00:00', '对传统文化有浓厚兴趣，态度端正，录取', '2026-06-09 10:00:00', '2026-06-22 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5057, 4009, 1013, 'FAILED', '2026-06-10 11:00:00', '2026-06-23 10:00:00', '零基础且时间不稳定，建议先自学基础', '2026-06-10 11:00:00', '2026-06-23 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5058, 4009, 1014, 'FAILED', '2026-06-11 09:00:00', '2026-06-23 10:30:00', '作品水平尚需提高，欢迎明年再报', '2026-06-11 09:00:00', '2026-06-23 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5059, 4009, 1017, 'FAILED', '2026-06-12 14:00:00', '2026-06-24 10:00:00', '与课程时间冲突严重', '2026-06-12 14:00:00', '2026-06-24 11:00:00', 0);

-- recruit 4012 吉他社春季 ENDED (applied=4: 4 PASSED + 2 FAILED)
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5060, 4012, 1009, 'PASSED', '2026-03-20 10:00:00', '2026-04-05 14:00:00', '有吉他基础，指弹水平不错，录取', '2026-03-20 10:00:00', '2026-04-05 15:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5061, 4012, 1010, 'PASSED', '2026-03-21 11:00:00', '2026-04-05 14:30:00', '热爱音乐，学习能力强，录取', '2026-03-21 11:00:00', '2026-04-05 15:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5062, 4012, 1011, 'PASSED', '2026-03-22 09:00:00', '2026-04-06 10:00:00', '零基础但态度认真，愿意坚持练习，录取', '2026-03-22 09:00:00', '2026-04-06 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5063, 4012, 1012, 'PASSED', '2026-03-23 14:00:00', '2026-04-06 10:30:00', '有乐队经验，贝斯和吉他都能弹，录取', '2026-03-23 14:00:00', '2026-04-06 11:30:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5064, 4012, 1016, 'FAILED', '2026-03-24 10:00:00', '2026-04-07 10:00:00', '时间冲突，无法参加排练', '2026-03-24 10:00:00', '2026-04-07 11:00:00', 0);
INSERT INTO recruit_record (id, recruit_id, user_id, status, apply_time, interview_time, interview_result, create_time, update_time, deleted)
VALUES (5065, 4012, 1017, 'FAILED', '2026-03-25 11:00:00', '2026-04-07 10:30:00', '零基础且无练习条件，建议先租琴练习', '2026-03-25 11:00:00', '2026-04-07 11:30:00', 0);

-- ===== 7. activity（16条） =====
-- DRAFT ×2, PENDING ×2, PUBLISHED ×3, ONGOING ×5, ENDED ×3, CANCELLED ×1

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6001, 2001, '校园秋季摄影大赛', '以"秋日校园"为主题，征集全校师生摄影作品。设一等奖1名、二等奖3名、三等奖5名，获奖作品将在图书馆展出两周。', '2026-09-01 09:00:00', '2026-09-03 18:00:00', 50, 6, 'PUBLISHED', 'N', '2026-08-01 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6002, 2001, '江边日落外拍活动', '一起去江边拍摄日落！集合时间下午4点，校门口出发。请自备相机或手机，三脚架可选。预计晚上7点返回。', '2026-07-20 16:00:00', '2026-07-20 19:00:00', 20, 8, 'ENDED', 'Y', '2026-07-10 10:00:00', '2026-07-21 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6003, 2002, '2026秋季编程马拉松', '48小时极限编程挑战！3-5人组队，主题现场公布。提供免费餐饮和休息区，前三名有丰厚奖金。', '2026-09-15 09:00:00', '2026-09-17 09:00:00', 100, 0, 'PENDING', 'Y', '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6004, 2002, 'Python入门工作坊', '面向零基础同学的Python入门课程，3小时手把手教学。自备笔记本电脑，提前安装Python 3.10+。', '2026-07-10 14:00:00', '2026-07-10 17:00:00', 30, 10, 'ENDED', 'Y', '2026-06-25 10:00:00', '2026-07-11 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6005, 2003, '新生杯篮球赛', '一年一度的新生杯篮球赛！各院系组队参赛，采用FIBA三人制规则。报名以院系为单位，每队3-5人。', '2026-09-20 09:00:00', '2026-09-22 18:00:00', 80, 0, 'PUBLISHED', 'N', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6006, 2003, '三人篮球对抗赛', '社团内部三人制篮球对抗赛，随机分组，循环赛制。奖品：冠军队每人一个篮球。', '2026-08-13 09:00:00', '2026-08-15 18:00:00', 24, 8, 'ONGOING', 'Y', '2026-08-01 10:00:00', '2026-08-13 09:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6007, 2004, '社区敬老院志愿服务', '前往阳光敬老院看望老人，陪聊天、表演节目、打扫卫生。集合时间上午8:30，校门口统一出发。', '2026-08-14 09:00:00', '2026-08-14 12:00:00', 30, 6, 'ONGOING', 'Y', '2026-08-05 10:00:00', '2026-08-14 09:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6008, 2004, '环保校园行', '计划中的校园环保活动，正在策划阶段。', '2026-09-25 09:00:00', '2026-09-25 17:00:00', 40, 0, 'DRAFT', 'N', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6009, 2005, '中秋诗词朗诵会', '中秋节诗词朗诵活动，可以朗诵自己喜欢的古诗词，也可以原创。现场评选最佳朗诵奖。', '2026-09-17 19:00:00', '2026-09-17 21:00:00', 60, 5, 'PUBLISHED', 'N', '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6010, 2005, '国学经典读书会', '共读《论语》选篇，讨论"仁义礼智信"的现代意义。每人分享一段最喜欢的章节。', '2026-07-05 14:00:00', '2026-07-05 16:00:00', 20, 6, 'ENDED', 'N', '2026-06-20 10:00:00', '2026-07-06 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6011, 2006, '机器人创意大赛', '3-5人组队，设计并制作一个能完成指定任务的机器人。提供Arduino套件和3D打印支持。', '2026-10-10 09:00:00', '2026-10-12 17:00:00', 50, 0, 'PENDING', 'Y', '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6012, 2007, '五人制足球赛', '社团内部五人制足球赛，分组循环+淘汰赛。场地：校足球场。请自备球鞋和护具。', '2026-08-12 09:00:00', '2026-08-16 18:00:00', 40, 10, 'ONGOING', 'Y', '2026-08-01 10:00:00', '2026-08-12 09:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6013, 2008, '校园街舞大赛', 'Breaking 1v1 Battle + 团体齐舞展示，欢迎全校同学报名或观战。评委由校外专业舞者担任。', '2026-09-05 18:00:00', '2026-09-05 21:00:00', 30, 3, 'PUBLISHED', 'N', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6014, 2009, '书画作品展览', '征集社员书画作品，在图书馆一楼展厅展出一周。欢迎全校师生参观。', '2026-10-01 09:00:00', '2026-10-07 17:00:00', 0, 0, 'DRAFT', 'N', '2026-08-12 10:00:00', '2026-08-12 10:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6015, 2010, '乒乓球友谊赛', '单打+双打，自由报名。采用11分制，三局两胜。奖品丰厚，欢迎观战！', '2026-08-14 14:00:00', '2026-08-16 18:00:00', 32, 6, 'ONGOING', 'Y', '2026-08-05 10:00:00', '2026-08-14 14:00:00', 0);

INSERT INTO activity (id, club_id, title, content, start_time, end_time, quota, applied_count, status, checkin_enabled, create_time, update_time, deleted)
VALUES (6016, 2013, '吉他之夜音乐会', '原定于9月1日的吉他社专场音乐会，因社团暂停已取消。', '2026-09-01 19:00:00', '2026-09-01 21:00:00', 100, 0, 'CANCELLED', 'N', '2026-07-01 10:00:00', '2026-08-10 10:00:00', 0);

-- ===== 8. activity_signup（68条） =====
-- 仅给 applied_count > 0 的活动创建 SIGNED 记录

-- activity 6001 校园秋季摄影大赛 (applied=6)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7001, 6001, 1019, 'SIGNED', '2026-08-02 10:00:00', '2026-08-02 10:00:00', '2026-08-02 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7002, 6001, 1001, 'SIGNED', '2026-08-02 11:00:00', '2026-08-02 11:00:00', '2026-08-02 11:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7003, 6001, 1002, 'SIGNED', '2026-08-03 09:00:00', '2026-08-03 09:00:00', '2026-08-03 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7004, 6001, 1003, 'SIGNED', '2026-08-03 14:00:00', '2026-08-03 14:00:00', '2026-08-03 14:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7005, 6001, 1004, 'SIGNED', '2026-08-04 10:00:00', '2026-08-04 10:00:00', '2026-08-04 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7006, 6001, 1005, 'SIGNED', '2026-08-05 08:00:00', '2026-08-05 08:00:00', '2026-08-05 08:00:00', 0);

-- activity 6002 江边日落外拍 (applied=8)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7007, 6002, 1019, 'SIGNED', '2026-07-12 09:00:00', '2026-07-12 09:00:00', '2026-07-12 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7008, 6002, 1001, 'SIGNED', '2026-07-12 10:00:00', '2026-07-12 10:00:00', '2026-07-12 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7009, 6002, 1002, 'SIGNED', '2026-07-12 11:00:00', '2026-07-12 11:00:00', '2026-07-12 11:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7010, 6002, 1003, 'SIGNED', '2026-07-13 09:00:00', '2026-07-13 09:00:00', '2026-07-13 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7011, 6002, 1004, 'SIGNED', '2026-07-13 10:00:00', '2026-07-13 10:00:00', '2026-07-13 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7012, 6002, 1005, 'SIGNED', '2026-07-14 08:00:00', '2026-07-14 08:00:00', '2026-07-14 08:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7013, 6002, 1022, 'SIGNED', '2026-07-14 09:00:00', '2026-07-14 09:00:00', '2026-07-14 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7014, 6002, 1017, 'SIGNED', '2026-07-15 10:00:00', '2026-07-15 10:00:00', '2026-07-15 10:00:00', 0);

-- activity 6004 Python入门工作坊 (applied=10)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7015, 6004, 1020, 'SIGNED', '2026-06-26 09:00:00', '2026-06-26 09:00:00', '2026-06-26 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7016, 6004, 1007, 'SIGNED', '2026-06-26 10:00:00', '2026-06-26 10:00:00', '2026-06-26 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7017, 6004, 1008, 'SIGNED', '2026-06-26 11:00:00', '2026-06-26 11:00:00', '2026-06-26 11:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7018, 6004, 1009, 'SIGNED', '2026-06-27 09:00:00', '2026-06-27 09:00:00', '2026-06-27 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7019, 6004, 1010, 'SIGNED', '2026-06-27 10:00:00', '2026-06-27 10:00:00', '2026-06-27 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7020, 6004, 1011, 'SIGNED', '2026-06-28 08:00:00', '2026-06-28 08:00:00', '2026-06-28 08:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7021, 6004, 1012, 'SIGNED', '2026-06-28 09:00:00', '2026-06-28 09:00:00', '2026-06-28 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7022, 6004, 1013, 'SIGNED', '2026-06-29 10:00:00', '2026-06-29 10:00:00', '2026-06-29 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7023, 6004, 1014, 'SIGNED', '2026-06-29 11:00:00', '2026-06-29 11:00:00', '2026-06-29 11:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7024, 6004, 1015, 'SIGNED', '2026-06-30 09:00:00', '2026-06-30 09:00:00', '2026-06-30 09:00:00', 0);

-- activity 6006 三人篮球对抗赛 (applied=8)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7025, 6006, 1021, 'SIGNED', '2026-08-02 09:00:00', '2026-08-02 09:00:00', '2026-08-02 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7026, 6006, 1013, 'SIGNED', '2026-08-02 10:00:00', '2026-08-02 10:00:00', '2026-08-02 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7027, 6006, 1014, 'SIGNED', '2026-08-03 09:00:00', '2026-08-03 09:00:00', '2026-08-03 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7028, 6006, 1015, 'SIGNED', '2026-08-03 10:00:00', '2026-08-03 10:00:00', '2026-08-03 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7029, 6006, 1016, 'SIGNED', '2026-08-04 09:00:00', '2026-08-04 09:00:00', '2026-08-04 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7030, 6006, 1001, 'SIGNED', '2026-08-04 14:00:00', '2026-08-04 14:00:00', '2026-08-04 14:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7031, 6006, 1002, 'SIGNED', '2026-08-05 09:00:00', '2026-08-05 09:00:00', '2026-08-05 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7032, 6006, 1003, 'SIGNED', '2026-08-05 10:00:00', '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);

-- activity 6007 社区敬老院志愿服务 (applied=6)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7033, 6007, 1022, 'SIGNED', '2026-08-06 09:00:00', '2026-08-06 09:00:00', '2026-08-06 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7034, 6007, 1017, 'SIGNED', '2026-08-06 10:00:00', '2026-08-06 10:00:00', '2026-08-06 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7035, 6007, 1018, 'SIGNED', '2026-08-07 09:00:00', '2026-08-07 09:00:00', '2026-08-07 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7036, 6007, 1001, 'SIGNED', '2026-08-07 14:00:00', '2026-08-07 14:00:00', '2026-08-07 14:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7037, 6007, 1002, 'SIGNED', '2026-08-08 09:00:00', '2026-08-08 09:00:00', '2026-08-08 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7038, 6007, 1003, 'SIGNED', '2026-08-08 10:00:00', '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

-- activity 6009 中秋诗词朗诵会 (applied=5)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7039, 6009, 1023, 'SIGNED', '2026-08-09 09:00:00', '2026-08-09 09:00:00', '2026-08-09 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7040, 6009, 1006, 'SIGNED', '2026-08-09 10:00:00', '2026-08-09 10:00:00', '2026-08-09 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7041, 6009, 1007, 'SIGNED', '2026-08-10 09:00:00', '2026-08-10 09:00:00', '2026-08-10 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7042, 6009, 1008, 'SIGNED', '2026-08-10 14:00:00', '2026-08-10 14:00:00', '2026-08-10 14:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7043, 6009, 1009, 'SIGNED', '2026-08-11 09:00:00', '2026-08-11 09:00:00', '2026-08-11 09:00:00', 0);

-- activity 6010 国学经典读书会 (applied=6)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7044, 6010, 1023, 'SIGNED', '2026-06-22 09:00:00', '2026-06-22 09:00:00', '2026-06-22 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7045, 6010, 1006, 'SIGNED', '2026-06-22 10:00:00', '2026-06-22 10:00:00', '2026-06-22 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7046, 6010, 1007, 'SIGNED', '2026-06-23 09:00:00', '2026-06-23 09:00:00', '2026-06-23 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7047, 6010, 1008, 'SIGNED', '2026-06-23 14:00:00', '2026-06-23 14:00:00', '2026-06-23 14:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7048, 6010, 1009, 'SIGNED', '2026-06-24 09:00:00', '2026-06-24 09:00:00', '2026-06-24 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7049, 6010, 1001, 'SIGNED', '2026-06-24 10:00:00', '2026-06-24 10:00:00', '2026-06-24 10:00:00', 0);

-- activity 6012 五人制足球赛 (applied=10)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7050, 6012, 1025, 'SIGNED', '2026-08-03 09:00:00', '2026-08-03 09:00:00', '2026-08-03 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7051, 6012, 1016, 'SIGNED', '2026-08-03 10:00:00', '2026-08-03 10:00:00', '2026-08-03 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7052, 6012, 1017, 'SIGNED', '2026-08-04 09:00:00', '2026-08-04 09:00:00', '2026-08-04 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7053, 6012, 1018, 'SIGNED', '2026-08-04 10:00:00', '2026-08-04 10:00:00', '2026-08-04 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7054, 6012, 1001, 'SIGNED', '2026-08-05 09:00:00', '2026-08-05 09:00:00', '2026-08-05 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7055, 6012, 1002, 'SIGNED', '2026-08-05 10:00:00', '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7056, 6012, 1003, 'SIGNED', '2026-08-06 09:00:00', '2026-08-06 09:00:00', '2026-08-06 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7057, 6012, 1004, 'SIGNED', '2026-08-06 10:00:00', '2026-08-06 10:00:00', '2026-08-06 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7058, 6012, 1005, 'SIGNED', '2026-08-07 09:00:00', '2026-08-07 09:00:00', '2026-08-07 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7059, 6012, 1006, 'SIGNED', '2026-08-07 14:00:00', '2026-08-07 14:00:00', '2026-08-07 14:00:00', 0);

-- activity 6013 校园街舞大赛 (applied=3)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7060, 6013, 1026, 'SIGNED', '2026-08-11 09:00:00', '2026-08-11 09:00:00', '2026-08-11 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7061, 6013, 1003, 'SIGNED', '2026-08-11 10:00:00', '2026-08-11 10:00:00', '2026-08-11 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7062, 6013, 1004, 'SIGNED', '2026-08-12 09:00:00', '2026-08-12 09:00:00', '2026-08-12 09:00:00', 0);

-- activity 6015 乒乓球友谊赛 (applied=6)
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7063, 6015, 1028, 'SIGNED', '2026-08-06 09:00:00', '2026-08-06 09:00:00', '2026-08-06 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7064, 6015, 1011, 'SIGNED', '2026-08-06 10:00:00', '2026-08-06 10:00:00', '2026-08-06 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7065, 6015, 1012, 'SIGNED', '2026-08-07 09:00:00', '2026-08-07 09:00:00', '2026-08-07 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7066, 6015, 1013, 'SIGNED', '2026-08-07 10:00:00', '2026-08-07 10:00:00', '2026-08-07 10:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7067, 6015, 1014, 'SIGNED', '2026-08-08 09:00:00', '2026-08-08 09:00:00', '2026-08-08 09:00:00', 0);
INSERT INTO activity_signup (id, activity_id, user_id, status, signup_time, create_time, update_time, deleted)
VALUES (7068, 6015, 1015, 'SIGNED', '2026-08-08 10:00:00', '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

-- ===== 9. activity_checkin（33条） =====
-- 仅 checkin_enabled='Y' 且 ONGOING/ENDED 的活动

-- activity 6002 江边日落外拍 ENDED checkin=Y (从8人中选6人签到)
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8001, 6002, 1019, '2026-07-20 15:55:00', '1', '2026-07-20 15:55:00', '2026-07-20 15:55:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8002, 6002, 1001, '2026-07-20 15:58:00', '1', '2026-07-20 15:58:00', '2026-07-20 15:58:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8003, 6002, 1002, '2026-07-20 16:00:00', '1', '2026-07-20 16:00:00', '2026-07-20 16:00:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8004, 6002, 1003, '2026-07-20 16:02:00', '1', '2026-07-20 16:02:00', '2026-07-20 16:02:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8005, 6002, 1004, '2026-07-20 16:05:00', '1', '2026-07-20 16:05:00', '2026-07-20 16:05:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8006, 6002, 1005, '2026-07-20 16:08:00', '1', '2026-07-20 16:08:00', '2026-07-20 16:08:00', 0);

-- activity 6004 Python入门工作坊 ENDED checkin=Y (从10人中选8人签到)
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8007, 6004, 1020, '2026-07-10 13:55:00', '1', '2026-07-10 13:55:00', '2026-07-10 13:55:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8008, 6004, 1007, '2026-07-10 13:58:00', '1', '2026-07-10 13:58:00', '2026-07-10 13:58:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8009, 6004, 1008, '2026-07-10 14:00:00', '1', '2026-07-10 14:00:00', '2026-07-10 14:00:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8010, 6004, 1009, '2026-07-10 14:02:00', '1', '2026-07-10 14:02:00', '2026-07-10 14:02:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8011, 6004, 1010, '2026-07-10 14:03:00', '1', '2026-07-10 14:03:00', '2026-07-10 14:03:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8012, 6004, 1011, '2026-07-10 14:05:00', '1', '2026-07-10 14:05:00', '2026-07-10 14:05:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8013, 6004, 1012, '2026-07-10 14:08:00', '1', '2026-07-10 14:08:00', '2026-07-10 14:08:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8014, 6004, 1013, '2026-07-10 14:10:00', '1', '2026-07-10 14:10:00', '2026-07-10 14:10:00', 0);

-- activity 6006 三人篮球对抗赛 ONGOING checkin=Y (从8人中选5人签到)
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8015, 6006, 1021, '2026-08-13 08:55:00', '1', '2026-08-13 08:55:00', '2026-08-13 08:55:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8016, 6006, 1013, '2026-08-13 08:58:00', '1', '2026-08-13 08:58:00', '2026-08-13 08:58:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8017, 6006, 1014, '2026-08-13 09:00:00', '1', '2026-08-13 09:00:00', '2026-08-13 09:00:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8018, 6006, 1015, '2026-08-13 09:02:00', '1', '2026-08-13 09:02:00', '2026-08-13 09:02:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8019, 6006, 1016, '2026-08-13 09:05:00', '1', '2026-08-13 09:05:00', '2026-08-13 09:05:00', 0);

-- activity 6007 社区敬老院志愿服务 ONGOING checkin=Y (从6人中选4人签到)
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8020, 6007, 1022, '2026-08-14 08:55:00', '1', '2026-08-14 08:55:00', '2026-08-14 08:55:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8021, 6007, 1017, '2026-08-14 08:58:00', '1', '2026-08-14 08:58:00', '2026-08-14 08:58:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8022, 6007, 1018, '2026-08-14 09:00:00', '1', '2026-08-14 09:00:00', '2026-08-14 09:00:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8023, 6007, 1001, '2026-08-14 09:02:00', '1', '2026-08-14 09:02:00', '2026-08-14 09:02:00', 0);

-- activity 6012 五人制足球赛 ONGOING checkin=Y (从10人中选6人签到)
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8024, 6012, 1025, '2026-08-12 08:55:00', '1', '2026-08-12 08:55:00', '2026-08-12 08:55:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8025, 6012, 1016, '2026-08-12 08:58:00', '1', '2026-08-12 08:58:00', '2026-08-12 08:58:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8026, 6012, 1017, '2026-08-12 09:00:00', '1', '2026-08-12 09:00:00', '2026-08-12 09:00:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8027, 6012, 1018, '2026-08-12 09:02:00', '1', '2026-08-12 09:02:00', '2026-08-12 09:02:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8028, 6012, 1001, '2026-08-12 09:05:00', '1', '2026-08-12 09:05:00', '2026-08-12 09:05:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8029, 6012, 1002, '2026-08-12 09:08:00', '1', '2026-08-12 09:08:00', '2026-08-12 09:08:00', 0);

-- activity 6015 乒乓球友谊赛 ONGOING checkin=Y (从6人中选4人签到)
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8030, 6015, 1028, '2026-08-14 13:55:00', '1', '2026-08-14 13:55:00', '2026-08-14 13:55:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8031, 6015, 1011, '2026-08-14 13:58:00', '1', '2026-08-14 13:58:00', '2026-08-14 13:58:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8032, 6015, 1012, '2026-08-14 14:00:00', '1', '2026-08-14 14:00:00', '2026-08-14 14:00:00', 0);
INSERT INTO activity_checkin (id, activity_id, user_id, checkin_time, status, create_time, update_time, deleted)
VALUES (8033, 6015, 1013, '2026-08-14 14:02:00', '1', '2026-08-14 14:02:00', '2026-08-14 14:02:00', 0);

-- ===== 10. notice（12条） =====
-- 平台级 ×3 (club_id=0), 社团级 ×9

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14001, 0, '2026年秋季学期社团注册通知', '各社团请于9月15日前完成秋季学期注册，提交学期活动计划和经费预算。逾期未注册的社团将被暂停活动资格。', 1, '2026-08-01 09:00:00', '0', 'Y', '2026-08-01 09:00:00', '2026-08-01 09:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14002, 0, '社团活动场地申请系统升级公告', '为提升场地使用效率，社团活动场地申请系统已完成升级。新系统支持在线预约、冲突检测和自动审批。请各社团负责人熟悉新系统操作。', 1, '2026-07-15 10:00:00', '0', 'Y', '2026-07-15 10:00:00', '2026-07-15 10:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14003, 0, '暑期社团活动安全提醒', '暑期高温，请各社团在组织户外活动时注意防暑降温，确保参与者人身安全。活动前需提交安全预案。', 1, '2026-07-01 09:00:00', '0', 'N', '2026-07-01 09:00:00', '2026-07-01 09:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14004, 2001, '摄影社器材借用须知', '社团器材（三脚架、闪光灯、反光板等）可免费借用，借用期限3天，需提前在群里预约。如有损坏需照价赔偿。', 1019, '2026-06-01 10:00:00', '0', 'N', '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14005, 2002, '编程协会服务器使用规范', '协会服务器仅供学习和项目开发使用，禁止挖矿、代理等违规行为。违规者将被永久取消使用资格。', 1020, '2026-05-20 14:00:00', '0', 'N', '2026-05-20 14:00:00', '2026-05-20 14:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14006, 2003, '篮球社训练时间调整通知', '因体育馆维修，本周训练临时调整至室外篮球场。如遇雨天则取消，请关注群消息。', 1021, '2026-08-10 09:00:00', '0', 'Y', '2026-08-10 09:00:00', '2026-08-10 09:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14007, 2004, '志愿者服务时长认定说明', '每次志愿服务需现场签到签退，服务时长由系统自动计算。如有疑问请联系副社长韩雨薇。', 1022, '2026-06-15 10:00:00', '0', 'N', '2026-06-15 10:00:00', '2026-06-15 10:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14008, 2006, '创客空间安全守则', '使用3D打印机、焊接工具等设备前必须阅读安全手册并通过在线测试。未经培训禁止操作激光切割机。', 1024, '2026-05-25 14:00:00', '0', 'N', '2026-05-25 14:00:00', '2026-05-25 14:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14009, 2007, '足球社球衣定制通知', '新赛季球衣开始定制，请各队员于8月20日前在群内接龙填写尺码。费用由社团经费承担。', 1025, '2026-08-05 10:00:00', '0', 'N', '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14010, 2008, '街舞社排练纪律提醒', '排练迟到超过15分钟视为缺席，一学期缺席3次以上将取消社员资格。请合理安排时间。', 1026, '2026-07-20 09:00:00', '0', 'N', '2026-07-20 09:00:00', '2026-07-20 09:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14011, 2013, '吉他社暂停活动通知', '因场地使用违规，吉他社即日起暂停一切活动。整改期间社员不得以社团名义组织任何活动。恢复时间另行通知。', 1003, '2026-07-15 10:00:00', '0', 'N', '2026-07-15 10:00:00', '2026-07-15 10:00:00', 0);

INSERT INTO notice (id, club_id, title, content, publish_user_id, publish_time, status, top, create_time, update_time, deleted)
VALUES (14012, 2010, '乒乓球社器材更新', '新购入10副红双喜三星球拍和50个比赛用球，欢迎大家试用。器材存放在体育馆C区储物柜。', 1028, '2026-08-08 14:00:00', '0', 'N', '2026-08-08 14:00:00', '2026-08-08 14:00:00', 0);

-- ===== 11. post（35条） =====
-- like_count / comment_count 将在 comment + user_like 插入后精确计算

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9001, 2001, 1019, '今天外拍去了江边，日落绝了，出片200张！分享几张给大家，光线真的太美了。摄影的魅力就在于捕捉那些转瞬即逝的瞬间。', 3, 3, '0', '2026-07-20 20:00:00', '2026-07-20 20:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9002, 2001, 1001, '摄影社秋季招新开始啦！想学摄影的同学快来报名，零基础也欢迎~我们有专业器材免费借用，每周外拍活动，还有学长学姐一对一指导。', 2, 2, '0', '2026-08-01 10:00:00', '2026-08-01 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9003, 2001, 1002, '上周外拍的成片出来了，这次拍的是校园银杏大道，秋意浓浓。用了新买的50mm f/1.4，虚化效果真的绝了。', 2, 2, '0', '2026-08-05 14:00:00', '2026-08-05 14:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9004, 2002, 1020, '周末Hackathon圆满结束！24小时极限编程，最终有6个项目完成demo展示，太燃了。冠军项目是一个校园二手交易平台，功能完整度令人惊叹。', 4, 2, '0', '2026-07-28 22:00:00', '2026-07-28 22:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9005, 2002, 1007, '推荐一个学习算法的好网站，LeetCode每日一题打卡群已建，想加入的私聊我。坚持刷题，秋招offer不是梦！', 2, 1, '0', '2026-08-02 09:00:00', '2026-08-02 09:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9006, 2002, 1008, '下周三晚7点有Python入门讲座，适合零基础同学，教室A301，记得带电脑。我会从环境搭建讲起，到写第一个爬虫，全程实操。', 3, 2, '0', '2026-07-05 15:00:00', '2026-07-05 15:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9007, 2002, 1020, '恭喜协会成员在全国大学生编程大赛中获得二等奖！🥈 三位同学辛苦了，你们是协会的骄傲！', 3, 2, '0', '2026-07-20 18:00:00', '2026-07-20 18:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9008, 2003, 1021, '新生杯篮球赛即将开幕！各院系代表队已经开始训练了，期待精彩对决。欢迎同学们来体育馆观战，为自己院系加油！', 2, 1, '0', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9009, 2003, 1013, '今天的三人制对抗赛太精彩了，加时赛最后一秒绝杀！观众都沸腾了。这就是篮球的魅力啊！', 3, 2, '0', '2026-08-13 20:00:00', '2026-08-13 20:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9010, 2003, 1021, '篮球社日常训练时间调整为每周二四下午4点，体育馆B场地。请大家准时到场，迟到要做体能惩罚哦。', 1, 1, '0', '2026-08-08 09:00:00', '2026-08-08 09:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9011, 2004, 1022, '上周末的敬老院志愿服务圆满完成，感谢12位志愿者的付出！老人们的笑容是最好的回报。特别感谢带了吉他表演的同学，老人们都跟着唱起来了。', 3, 2, '0', '2026-07-22 15:00:00', '2026-07-22 15:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9012, 2004, 1017, '环保校园行活动招募中！本周六一起清理校园河道垃圾，为美丽校园出一份力。参与即可获得志愿服务时长认证。', 2, 1, '0', '2026-08-12 10:00:00', '2026-08-12 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9013, 2004, 1022, '志愿者培训通知：本周五下午2点，行政楼208室，内容为急救知识和志愿服务礼仪。请所有社员务必参加。', 1, 1, '0', '2026-08-10 14:00:00', '2026-08-10 14:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9014, 2004, 1018, '协会本学期累计志愿服务时长已突破500小时！感恩每一位志愿者的付出。让我们继续用行动传递温暖。', 2, 2, '0', '2026-08-01 16:00:00', '2026-08-01 16:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9015, 2005, 1023, '中秋诗词朗诵会报名开始！想朗诵自己喜欢的古诗词的同学快来报名~可以单人朗诵，也可以组队合作。', 2, 1, '0', '2026-08-09 10:00:00', '2026-08-09 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9016, 2005, 1006, '上周读书会讨论了《论语》中的"君子不器"，大家的见解都很深刻。整理了笔记分享给大家，欢迎讨论。', 2, 2, '0', '2026-07-06 10:00:00', '2026-07-06 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9017, 2005, 1007, '推荐一本好书：《美的历程》李泽厚，对中国美学的发展脉络梳理得非常清晰。从远古图腾到明清文艺，每一页都有收获。', 1, 1, '0', '2026-07-15 14:00:00', '2026-07-15 14:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9018, 2006, 1024, '我们的机器人小车终于能自动避障了！历时三周的调试，超声波传感器+舵机转向+电机驱动，感动到想哭。下一步加入视觉识别。', 3, 2, '0', '2026-08-05 20:00:00', '2026-08-05 20:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9019, 2006, 1010, '创客空间开放时间延长至晚上9点，欢迎大家来做项目。3D打印机已维护完毕，新增了PLA和PETG两种耗材。', 1, 1, '0', '2026-08-01 09:00:00', '2026-08-01 09:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9020, 2006, 1011, '机器人创意大赛开始组队了！3-5人一组，10月份比赛，奖金丰厚。有想法的同学赶紧找队友。', 2, 1, '0', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9021, 2007, 1025, '五人制足球赛正在进行中！今天A组和B组的比赛非常激烈，比分3:2。明天C组和D组的比赛同样精彩，欢迎来观战。', 2, 2, '0', '2026-08-13 19:00:00', '2026-08-13 19:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9022, 2007, 1016, '足球社招新啦！喜欢踢球的同学不要错过，我们每周三和周六都有训练，还有定期的友谊赛和校际联赛。', 2, 1, '0', '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9023, 2007, 1017, '今天的训练赛大家都很拼，后卫线配合越来越默契了。继续保持，校际联赛加油！', 1, 1, '0', '2026-08-10 20:00:00', '2026-08-10 20:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9024, 2008, 1026, '校园街舞大赛9月5日开赛！Breaking 1v1 Battle + 团体齐舞展示，各路高手齐聚一堂，欢迎来观战！', 3, 2, '0', '2026-08-12 10:00:00', '2026-08-12 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9025, 2008, 1003, '今天学了一个新的Breaking动作，终于能连上了！练了整整两周，膝盖都青了，但值了。分享练习视频给大家。', 2, 1, '0', '2026-08-08 21:00:00', '2026-08-08 21:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9026, 2008, 1004, '街舞社新学期第一次排练，场地换到了舞蹈教室202，空间更大了，音响设备也升级了。', 1, 1, '0', '2026-08-06 19:00:00', '2026-08-06 19:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9027, 2009, 1027, '书画作品展览正在筹备中，征集同学们的书法和国画作品，投稿截止9月25日。优秀作品将在图书馆一楼展厅展出一周。', 1, 1, '0', '2026-08-13 10:00:00', '2026-08-13 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9028, 2009, 1007, '今天的书法练习课，老师教了行书的基本笔法，进步很大。行云流水的感觉真好，继续努力。', 2, 1, '0', '2026-08-05 17:00:00', '2026-08-05 17:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9029, 2009, 1008, '分享一幅最近临摹的《兰亭序》局部，虽然还差很多，但比上个月进步了不少。书法真的是越练越上瘾。', 2, 2, '0', '2026-08-10 15:00:00', '2026-08-10 15:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9030, 2010, 1028, '乒乓球友谊赛正在进行！今天的混双比赛太精彩了，观众都沸腾了。明天是男子单打半决赛，敬请期待。', 2, 2, '0', '2026-08-14 18:00:00', '2026-08-14 18:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9031, 2010, 1011, '乒乓球社招新：每周一三五下午5点训练，体育馆C场地，欢迎加入。我们有专业教练指导，器材免费提供。', 1, 1, '0', '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9032, 2010, 1012, '恭喜社员小明在校级乒乓球比赛中获得男子单打第三名！🥉 为社团争光，继续加油！', 2, 1, '0', '2026-08-12 16:00:00', '2026-08-12 16:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9033, NULL, 1001, '今天天气真好，去图书馆看了一下午书，充实的一天。最近在读《百年孤独》，马尔克斯的魔幻现实主义真的很迷人。', 1, 1, '0', '2026-08-10 18:00:00', '2026-08-10 18:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9034, NULL, 1009, '期末考试终于结束了！暑假计划：学车、旅游、做项目。第一步先把驾照考了。', 2, 1, '0', '2026-07-05 20:00:00', '2026-07-05 20:00:00', 0);

INSERT INTO post (id, club_id, author_id, content, like_count, comment_count, status, create_time, update_time, deleted)
VALUES (9035, 2013, 1003, '吉他社因场地问题暂时停止活动，恢复时间另行通知。给大家造成不便，深感抱歉。希望尽快整改完毕，重新开社。', 0, 0, '0', '2026-07-15 11:00:00', '2026-07-15 11:00:00', 0);

-- ===== 12. comment（55条） =====
-- POST ×50, NOTICE ×3, ACTIVITY ×2

-- post 9001 评论 (3条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10001, 'POST', 9001, 1001, '太好看了！最后一张逆光的构图绝了，能分享一下参数吗？', 2, '2026-07-20 21:00:00', '2026-07-20 21:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10002, 'POST', 9001, 1002, '好想去外拍！下次活动是什么时候？', 0, '2026-07-20 21:30:00', '2026-07-20 21:30:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10003, 'POST', 9001, 1003, '日落时分的色温真的太美了，摄影社氛围好好。', 0, '2026-07-20 22:00:00', '2026-07-20 22:00:00', 0);

-- post 9002 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10004, 'POST', 9002, 1011, '零基础也能报名吗？我一直想学摄影但没有相机。', 0, '2026-08-01 11:00:00', '2026-08-01 11:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10005, 'POST', 9002, 1019, '当然可以！社团有器材可以借用，手机摄影也很棒，欢迎加入~', 0, '2026-08-01 11:30:00', '2026-08-01 11:30:00', 0);

-- post 9003 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10006, 'POST', 9003, 1004, '银杏大道真的太出片了，求原图！', 0, '2026-08-05 15:00:00', '2026-08-05 15:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10007, 'POST', 9003, 1005, '50mm f/1.4确实是人像利器，拍银杏也很适合。', 0, '2026-08-05 16:00:00', '2026-08-05 16:00:00', 0);

-- post 9004 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10008, 'POST', 9004, 1007, '24小时不睡觉写代码，太疯狂了，但也很爽！', 1, '2026-07-28 22:30:00', '2026-07-28 22:30:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10009, 'POST', 9004, 1008, '冠军项目的前端是我写的！React+TypeScript，下次分享技术细节。', 0, '2026-07-29 09:00:00', '2026-07-29 09:00:00', 0);

-- post 9005 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10010, 'POST', 9005, 1009, '已私聊，想加入打卡群！最近在准备面试，需要刷题。', 0, '2026-08-02 10:00:00', '2026-08-02 10:00:00', 0);

-- post 9006 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10011, 'POST', 9006, 1013, 'Python入门讲座太棒了！终于知道怎么装环境了。', 0, '2026-07-05 16:00:00', '2026-07-05 16:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10012, 'POST', 9006, 1014, '爬虫那部分讲得太好了，已经能自己抓数据了。', 0, '2026-07-05 17:00:00', '2026-07-05 17:00:00', 0);

-- post 9007 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10013, 'POST', 9007, 1009, '太厉害了！明年我也想参加，有组队的吗？', 1, '2026-07-20 19:00:00', '2026-07-20 19:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10014, 'POST', 9007, 1010, '二等奖已经很厉害了，继续加油！', 0, '2026-07-20 20:00:00', '2026-07-20 20:00:00', 0);

-- post 9008 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10015, 'POST', 9008, 1014, '计算机学院已经组好队了，目标冠军！🏆', 0, '2026-08-10 11:00:00', '2026-08-10 11:00:00', 0);

-- post 9009 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10016, 'POST', 9009, 1015, '那个绝杀球真的太帅了！慢动作回放看了好几遍。', 0, '2026-08-13 21:00:00', '2026-08-13 21:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10017, 'POST', 9009, 1016, '篮球社的比赛质量越来越高了，加油！', 0, '2026-08-13 22:00:00', '2026-08-13 22:00:00', 0);

-- post 9010 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10018, 'POST', 9010, 1013, '收到，准时到场！', 0, '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

-- post 9011 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10019, 'POST', 9011, 1001, '那天我也去了，老人们真的很开心。特别是张奶奶，拉着我聊了好久。', 1, '2026-07-22 16:00:00', '2026-07-22 16:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10020, 'POST', 9011, 1002, '下次活动什么时候？还想参加！', 0, '2026-07-22 17:00:00', '2026-07-22 17:00:00', 0);

-- post 9012 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10021, 'POST', 9012, 1003, '已报名！环保从身边做起。', 0, '2026-08-12 11:00:00', '2026-08-12 11:00:00', 0);

-- post 9013 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10022, 'POST', 9013, 1004, '好的，准时参加！', 0, '2026-08-10 15:00:00', '2026-08-10 15:00:00', 0);

-- post 9014 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10023, 'POST', 9014, 1005, '500小时！太棒了，我们是最棒的！', 0, '2026-08-01 17:00:00', '2026-08-01 17:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10024, 'POST', 9014, 1006, '继续加油，争取突破1000小时！', 0, '2026-08-01 18:00:00', '2026-08-01 18:00:00', 0);

-- post 9015 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10025, 'POST', 9015, 1008, '我要朗诵李白的《将进酒》！', 0, '2026-08-09 11:00:00', '2026-08-09 11:00:00', 0);

-- post 9016 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10026, 'POST', 9016, 1009, '"君子不器"这个话题讨论得太好了，笔记很详细。', 0, '2026-07-06 11:00:00', '2026-07-06 11:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10027, 'POST', 9016, 1023, '感谢整理！下周读书会讨论"己所不欲勿施于人"，欢迎大家参加。', 0, '2026-07-06 12:00:00', '2026-07-06 12:00:00', 0);

-- post 9017 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10028, 'POST', 9017, 1006, '李泽厚的书确实值得读，推荐搭配《华夏美学》一起看。', 0, '2026-07-15 15:00:00', '2026-07-15 15:00:00', 0);

-- post 9018 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10029, 'POST', 9018, 1010, '超声波传感器方案不错，不过可以试试LiDAR，精度更高。', 1, '2026-08-05 21:00:00', '2026-08-05 21:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10030, 'POST', 9018, 1011, '视觉识别可以用OpenCV，我有经验，可以一起做。', 0, '2026-08-06 09:00:00', '2026-08-06 09:00:00', 0);

-- post 9019 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10031, 'POST', 9019, 1012, 'PETG耗材太好了，比PLA强度高很多。', 0, '2026-08-01 10:00:00', '2026-08-01 10:00:00', 0);

-- post 9020 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10032, 'POST', 9020, 1013, '我想做一个智能垃圾桶，有组队的吗？', 0, '2026-08-10 11:00:00', '2026-08-10 11:00:00', 0);

-- post 9021 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10033, 'POST', 9021, 1001, '3:2的比分太刺激了！明天一定来看。', 1, '2026-08-13 20:00:00', '2026-08-13 20:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10034, 'POST', 9021, 1002, 'B组的配合真的很默契，传球意识太好了。', 0, '2026-08-13 21:00:00', '2026-08-13 21:00:00', 0);

-- post 9022 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10035, 'POST', 9022, 1018, '已报名！好久没踢球了，想重新找回状态。', 0, '2026-08-05 11:00:00', '2026-08-05 11:00:00', 0);

-- post 9023 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10036, 'POST', 9023, 1025, '后卫线确实进步很大，继续保持！', 0, '2026-08-10 21:00:00', '2026-08-10 21:00:00', 0);

-- post 9024 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10037, 'POST', 9024, 1005, 'Breaking Battle太期待了！到时候一定来观战。', 1, '2026-08-12 11:00:00', '2026-08-12 11:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10038, 'POST', 9024, 1006, 'Popping环节有没有？想看！', 0, '2026-08-12 12:00:00', '2026-08-12 12:00:00', 0);

-- post 9025 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10039, 'POST', 9025, 1004, '膝盖青了还在练，太拼了！注意安全啊。', 0, '2026-08-08 22:00:00', '2026-08-08 22:00:00', 0);

-- post 9026 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10040, 'POST', 9026, 1005, '202教室空间确实大很多，跳起来更放得开。', 0, '2026-08-06 20:00:00', '2026-08-06 20:00:00', 0);

-- post 9027 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10041, 'POST', 9027, 1008, '我有一幅楷书作品想投稿，怎么提交？', 0, '2026-08-13 11:00:00', '2026-08-13 11:00:00', 0);

-- post 9028 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10042, 'POST', 9028, 1009, '行书写起来确实比楷书流畅，有行云流水的感觉。', 0, '2026-08-05 18:00:00', '2026-08-05 18:00:00', 0);

-- post 9029 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10043, 'POST', 9029, 1010, '《兰亭序》临摹难度很大，能写成这样已经很不错了！', 0, '2026-08-10 16:00:00', '2026-08-10 16:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10044, 'POST', 9029, 1027, '继续加油，书法就是要持之以恒。下次展览可以投稿！', 0, '2026-08-10 17:00:00', '2026-08-10 17:00:00', 0);

-- post 9030 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10045, 'POST', 9030, 1012, '混双比赛确实精彩，明天单打半决赛我一定来！', 1, '2026-08-14 19:00:00', '2026-08-14 19:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10046, 'POST', 9030, 1013, '观众氛围太好了，乒乓球社加油！', 0, '2026-08-14 20:00:00', '2026-08-14 20:00:00', 0);

-- post 9031 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10047, 'POST', 9031, 1015, '有教练指导太好了，我正想找专业教练学弧圈球。', 0, '2026-08-08 11:00:00', '2026-08-08 11:00:00', 0);

-- post 9032 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10048, 'POST', 9032, 1028, '小明辛苦了，下次争取金牌！🥇', 0, '2026-08-12 17:00:00', '2026-08-12 17:00:00', 0);

-- post 9033 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10049, 'POST', 9033, 1002, '《百年孤独》我也在读，马尔克斯的叙事风格真的很独特。', 0, '2026-08-10 19:00:00', '2026-08-10 19:00:00', 0);

-- post 9034 评论 (1条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10050, 'POST', 9034, 1001, '暑假学车+1，一起加油！科二还没过呢。', 0, '2026-07-05 21:00:00', '2026-07-05 21:00:00', 0);

-- NOTICE 评论 (3条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10051, 'NOTICE', 14001, 1020, '收到，编程协会会按时提交活动计划。', 0, '2026-08-01 10:00:00', '2026-08-01 10:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10052, 'NOTICE', 14001, 1022, '志愿者协会也会按时注册。', 0, '2026-08-01 11:00:00', '2026-08-01 11:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10053, 'NOTICE', 14002, 1024, '新系统试用了一下，确实比旧系统好用多了。', 0, '2026-07-16 09:00:00', '2026-07-16 09:00:00', 0);

-- ACTIVITY 评论 (2条)
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10054, 'ACTIVITY', 6004, 1013, 'Python讲座太棒了，期待更多技术分享活动！', 0, '2026-07-10 18:00:00', '2026-07-10 18:00:00', 0);
INSERT INTO `comment` (id, biz_type, biz_id, user_id, content, like_count, create_time, update_time, deleted)
VALUES (10055, 'ACTIVITY', 6002, 1001, '江边日落太美了，下次还要去！', 0, '2026-07-20 20:00:00', '2026-07-20 20:00:00', 0);

-- ===== 13. user_like（76条） =====
-- POST 点赞 68 条 + COMMENT 点赞 8 条

-- post 9001 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11001, 'POST', 9001, 1002, '1', '2026-07-20 21:00:00', '2026-07-20 21:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11002, 'POST', 9001, 1003, '1', '2026-07-20 21:30:00', '2026-07-20 21:30:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11003, 'POST', 9001, 1004, '1', '2026-07-20 22:00:00', '2026-07-20 22:00:00', 0);

-- post 9002 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11004, 'POST', 9002, 1011, '1', '2026-08-01 11:00:00', '2026-08-01 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11005, 'POST', 9002, 1012, '1', '2026-08-01 12:00:00', '2026-08-01 12:00:00', 0);

-- post 9003 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11006, 'POST', 9003, 1019, '1', '2026-08-05 15:00:00', '2026-08-05 15:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11007, 'POST', 9003, 1003, '1', '2026-08-05 16:00:00', '2026-08-05 16:00:00', 0);

-- post 9004 点赞 (4)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11008, 'POST', 9004, 1008, '1', '2026-07-28 22:30:00', '2026-07-28 22:30:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11009, 'POST', 9004, 1009, '1', '2026-07-29 08:00:00', '2026-07-29 08:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11010, 'POST', 9004, 1010, '1', '2026-07-29 09:00:00', '2026-07-29 09:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11011, 'POST', 9004, 1011, '1', '2026-07-29 10:00:00', '2026-07-29 10:00:00', 0);

-- post 9005 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11012, 'POST', 9005, 1010, '1', '2026-08-02 10:00:00', '2026-08-02 10:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11013, 'POST', 9005, 1012, '1', '2026-08-02 11:00:00', '2026-08-02 11:00:00', 0);

-- post 9006 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11014, 'POST', 9006, 1013, '1', '2026-07-05 16:00:00', '2026-07-05 16:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11015, 'POST', 9006, 1014, '1', '2026-07-05 17:00:00', '2026-07-05 17:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11016, 'POST', 9006, 1015, '1', '2026-07-06 08:00:00', '2026-07-06 08:00:00', 0);

-- post 9007 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11017, 'POST', 9007, 1009, '1', '2026-07-20 19:00:00', '2026-07-20 19:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11018, 'POST', 9007, 1010, '1', '2026-07-20 20:00:00', '2026-07-20 20:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11019, 'POST', 9007, 1011, '1', '2026-07-20 21:00:00', '2026-07-20 21:00:00', 0);

-- post 9008 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11020, 'POST', 9008, 1014, '1', '2026-08-10 11:00:00', '2026-08-10 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11021, 'POST', 9008, 1015, '1', '2026-08-10 12:00:00', '2026-08-10 12:00:00', 0);

-- post 9009 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11022, 'POST', 9009, 1015, '1', '2026-08-13 21:00:00', '2026-08-13 21:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11023, 'POST', 9009, 1016, '1', '2026-08-13 22:00:00', '2026-08-13 22:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11024, 'POST', 9009, 1001, '1', '2026-08-14 08:00:00', '2026-08-14 08:00:00', 0);

-- post 9010 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11025, 'POST', 9010, 1016, '1', '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

-- post 9011 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11026, 'POST', 9011, 1001, '1', '2026-07-22 16:00:00', '2026-07-22 16:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11027, 'POST', 9011, 1002, '1', '2026-07-22 17:00:00', '2026-07-22 17:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11028, 'POST', 9011, 1003, '1', '2026-07-22 18:00:00', '2026-07-22 18:00:00', 0);

-- post 9012 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11029, 'POST', 9012, 1004, '1', '2026-08-12 11:00:00', '2026-08-12 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11030, 'POST', 9012, 1005, '1', '2026-08-12 12:00:00', '2026-08-12 12:00:00', 0);

-- post 9013 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11031, 'POST', 9013, 1017, '1', '2026-08-10 15:00:00', '2026-08-10 15:00:00', 0);

-- post 9014 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11032, 'POST', 9014, 1001, '1', '2026-08-01 17:00:00', '2026-08-01 17:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11033, 'POST', 9014, 1002, '1', '2026-08-01 18:00:00', '2026-08-01 18:00:00', 0);

-- post 9015 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11034, 'POST', 9015, 1006, '1', '2026-08-09 11:00:00', '2026-08-09 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11035, 'POST', 9015, 1008, '1', '2026-08-09 12:00:00', '2026-08-09 12:00:00', 0);

-- post 9016 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11036, 'POST', 9016, 1007, '1', '2026-07-06 11:00:00', '2026-07-06 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11037, 'POST', 9016, 1008, '1', '2026-07-06 12:00:00', '2026-07-06 12:00:00', 0);

-- post 9017 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11038, 'POST', 9017, 1009, '1', '2026-07-15 15:00:00', '2026-07-15 15:00:00', 0);

-- post 9018 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11039, 'POST', 9018, 1010, '1', '2026-08-05 21:00:00', '2026-08-05 21:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11040, 'POST', 9018, 1011, '1', '2026-08-06 08:00:00', '2026-08-06 08:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11041, 'POST', 9018, 1012, '1', '2026-08-06 09:00:00', '2026-08-06 09:00:00', 0);

-- post 9019 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11042, 'POST', 9019, 1012, '1', '2026-08-01 10:00:00', '2026-08-01 10:00:00', 0);

-- post 9020 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11043, 'POST', 9020, 1013, '1', '2026-08-10 11:00:00', '2026-08-10 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11044, 'POST', 9020, 1014, '1', '2026-08-10 12:00:00', '2026-08-10 12:00:00', 0);

-- post 9021 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11045, 'POST', 9021, 1001, '1', '2026-08-13 20:00:00', '2026-08-13 20:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11046, 'POST', 9021, 1002, '1', '2026-08-13 21:00:00', '2026-08-13 21:00:00', 0);

-- post 9022 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11047, 'POST', 9022, 1018, '1', '2026-08-05 11:00:00', '2026-08-05 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11048, 'POST', 9022, 1001, '1', '2026-08-05 12:00:00', '2026-08-05 12:00:00', 0);

-- post 9023 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11049, 'POST', 9023, 1025, '1', '2026-08-10 21:00:00', '2026-08-10 21:00:00', 0);

-- post 9024 点赞 (3)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11050, 'POST', 9024, 1003, '1', '2026-08-12 11:00:00', '2026-08-12 11:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11051, 'POST', 9024, 1004, '1', '2026-08-12 12:00:00', '2026-08-12 12:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11052, 'POST', 9024, 1005, '1', '2026-08-12 13:00:00', '2026-08-12 13:00:00', 0);

-- post 9025 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11053, 'POST', 9025, 1005, '1', '2026-08-08 22:00:00', '2026-08-08 22:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11054, 'POST', 9025, 1026, '1', '2026-08-09 08:00:00', '2026-08-09 08:00:00', 0);

-- post 9026 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11055, 'POST', 9026, 1005, '1', '2026-08-06 20:00:00', '2026-08-06 20:00:00', 0);

-- post 9027 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11056, 'POST', 9027, 1008, '1', '2026-08-13 11:00:00', '2026-08-13 11:00:00', 0);

-- post 9028 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11057, 'POST', 9028, 1006, '1', '2026-08-05 18:00:00', '2026-08-05 18:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11058, 'POST', 9028, 1009, '1', '2026-08-05 19:00:00', '2026-08-05 19:00:00', 0);

-- post 9029 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11059, 'POST', 9029, 1010, '1', '2026-08-10 16:00:00', '2026-08-10 16:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11060, 'POST', 9029, 1027, '1', '2026-08-10 17:00:00', '2026-08-10 17:00:00', 0);

-- post 9030 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11061, 'POST', 9030, 1012, '1', '2026-08-14 19:00:00', '2026-08-14 19:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11062, 'POST', 9030, 1013, '1', '2026-08-14 20:00:00', '2026-08-14 20:00:00', 0);

-- post 9031 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11063, 'POST', 9031, 1015, '1', '2026-08-08 11:00:00', '2026-08-08 11:00:00', 0);

-- post 9032 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11064, 'POST', 9032, 1011, '1', '2026-08-12 17:00:00', '2026-08-12 17:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11065, 'POST', 9032, 1014, '1', '2026-08-12 18:00:00', '2026-08-12 18:00:00', 0);

-- post 9033 点赞 (1)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11066, 'POST', 9033, 1009, '1', '2026-08-10 19:00:00', '2026-08-10 19:00:00', 0);

-- post 9034 点赞 (2)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11067, 'POST', 9034, 1002, '1', '2026-07-05 21:00:00', '2026-07-05 21:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11068, 'POST', 9034, 1003, '1', '2026-07-05 22:00:00', '2026-07-05 22:00:00', 0);

-- COMMENT 点赞 (8条)
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11069, 'COMMENT', 10001, 1002, '1', '2026-07-20 22:00:00', '2026-07-20 22:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11070, 'COMMENT', 10001, 1003, '1', '2026-07-20 22:30:00', '2026-07-20 22:30:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11071, 'COMMENT', 10008, 1009, '1', '2026-07-29 08:00:00', '2026-07-29 08:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11072, 'COMMENT', 10013, 1010, '1', '2026-07-20 20:00:00', '2026-07-20 20:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11073, 'COMMENT', 10019, 1003, '1', '2026-07-22 17:00:00', '2026-07-22 17:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11074, 'COMMENT', 10029, 1011, '1', '2026-08-06 09:00:00', '2026-08-06 09:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11075, 'COMMENT', 10033, 1002, '1', '2026-08-13 21:00:00', '2026-08-13 21:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11076, 'COMMENT', 10037, 1026, '1', '2026-08-12 12:00:00', '2026-08-12 12:00:00', 0);
INSERT INTO user_like (id, biz_type, biz_id, user_id, status, create_time, update_time, deleted)
VALUES (11077, 'COMMENT', 10045, 1028, '1', '2026-08-14 20:00:00', '2026-08-14 20:00:00', 0);

-- ===== 14. fund（27条） =====
-- APPROVED ×23, PENDING ×2, REJECTED ×2

-- 社团 2001 晨光摄影社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12001, 2001, '2026年度社团运营经费', 3000.00, 'INCOME', 'APPROVED', 1019, 1, '2026-05-20 10:00:00', '经费合理，批准拨付', '2026-05-18 10:00:00', '2026-05-20 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12002, 2001, '购买摄影器材三脚架', 1200.00, 'EXPENSE', 'APPROVED', 1019, 1, '2026-06-05 14:00:00', '三脚架为必需器材，批准', '2026-06-01 10:00:00', '2026-06-05 14:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12003, 2001, '外拍交通费报销', 300.00, 'EXPENSE', 'APPROVED', 1001, 1, '2026-07-25 10:00:00', '江边外拍交通费，批准', '2026-07-22 10:00:00', '2026-07-25 10:00:00', 0);

-- 社团 2002 编程爱好者协会
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12004, 2002, '迎新活动经费拨付', 5000.00, 'INCOME', 'APPROVED', 1020, 1, '2026-05-22 09:00:00', '迎新经费，批准', '2026-05-20 10:00:00', '2026-05-22 09:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12005, 2002, '购买开发板Arduino套件', 2000.00, 'EXPENSE', 'APPROVED', 1020, 1, '2026-06-10 14:00:00', '教学用开发板，批准', '2026-06-05 10:00:00', '2026-06-10 14:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12006, 2002, '编程比赛奖品采购', 1500.00, 'EXPENSE', 'APPROVED', 1007, 1, '2026-07-30 10:00:00', 'Hackathon奖品，批准', '2026-07-25 10:00:00', '2026-07-30 10:00:00', 0);

-- 社团 2003 篮球社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12007, 2003, '年度活动经费', 2000.00, 'INCOME', 'APPROVED', 1021, 1, '2026-05-25 10:00:00', '年度经费，批准', '2026-05-22 10:00:00', '2026-05-25 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12008, 2003, '篮球采购10个', 800.00, 'EXPENSE', 'APPROVED', 1021, 1, '2026-06-15 14:00:00', '训练用球，批准', '2026-06-10 10:00:00', '2026-06-15 14:00:00', 0);

-- 社团 2004 青年志愿者协会
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12009, 2004, '志愿服务专项经费', 4000.00, 'INCOME', 'APPROVED', 1022, 1, '2026-05-28 10:00:00', '志愿服务专项，批准', '2026-05-25 10:00:00', '2026-05-28 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12010, 2004, '社区服务物资采购', 1500.00, 'EXPENSE', 'APPROVED', 1017, 1, '2026-06-20 14:00:00', '敬老院服务物资，批准', '2026-06-15 10:00:00', '2026-06-20 14:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12011, 2004, '志愿者培训材料费', 500.00, 'EXPENSE', 'APPROVED', 1022, 1, '2026-08-05 10:00:00', '急救培训材料，批准', '2026-08-01 10:00:00', '2026-08-05 10:00:00', 0);

-- 社团 2005 国学社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12012, 2005, '传统文化活动经费', 2500.00, 'INCOME', 'APPROVED', 1023, 1, '2026-06-01 10:00:00', '传统文化传承经费，批准', '2026-05-28 10:00:00', '2026-06-01 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12013, 2005, '购买国学经典书籍', 800.00, 'EXPENSE', 'APPROVED', 1006, 1, '2026-06-25 14:00:00', '读书会用书，批准', '2026-06-20 10:00:00', '2026-06-25 14:00:00', 0);

-- 社团 2006 机器人创客社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12014, 2006, '科技创新经费', 8000.00, 'INCOME', 'APPROVED', 1024, 1, '2026-06-05 10:00:00', '科技创新专项，批准', '2026-06-01 10:00:00', '2026-06-05 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12015, 2006, '购买机器人套件', 5000.00, 'EXPENSE', 'APPROVED', 1024, 1, '2026-07-05 14:00:00', '机器人比赛套件，批准', '2026-07-01 10:00:00', '2026-07-05 14:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12016, 2006, '3D打印耗材', 1200.00, 'EXPENSE', 'APPROVED', 1010, 1, '2026-08-05 10:00:00', 'PLA和PETG耗材，批准', '2026-08-01 10:00:00', '2026-08-05 10:00:00', 0);

-- 社团 2007 足球社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12017, 2007, '年度体育经费', 3000.00, 'INCOME', 'APPROVED', 1025, 1, '2026-06-08 10:00:00', '年度体育经费，批准', '2026-06-05 10:00:00', '2026-06-08 10:00:00', 0);

-- 社团 2008 街舞社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12018, 2008, '文艺活动经费', 2000.00, 'INCOME', 'APPROVED', 1026, 1, '2026-06-10 10:00:00', '文艺活动经费，批准', '2026-06-08 10:00:00', '2026-06-10 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12019, 2008, '舞蹈室音响设备租赁', 800.00, 'EXPENSE', 'APPROVED', 1003, 1, '2026-08-10 10:00:00', '排练用音响，批准', '2026-08-05 10:00:00', '2026-08-10 10:00:00', 0);

-- 社团 2009 书画社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12020, 2009, '传统文化传承经费', 2000.00, 'INCOME', 'APPROVED', 1027, 1, '2026-06-12 10:00:00', '传统文化传承，批准', '2026-06-08 10:00:00', '2026-06-12 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12021, 2009, '书法用品采购', 600.00, 'EXPENSE', 'APPROVED', 1027, 1, '2026-08-08 10:00:00', '笔墨纸砚，批准', '2026-08-05 10:00:00', '2026-08-08 10:00:00', 0);

-- 社团 2010 乒乓球社
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12022, 2010, '体育器材经费', 1500.00, 'INCOME', 'APPROVED', 1028, 1, '2026-06-15 10:00:00', '体育器材经费，批准', '2026-06-12 10:00:00', '2026-06-15 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12023, 2010, '乒乓球台维护', 500.00, 'EXPENSE', 'APPROVED', 1028, 1, '2026-08-10 10:00:00', '球台维护费，批准', '2026-08-08 10:00:00', '2026-08-10 10:00:00', 0);

-- PENDING 经费
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12024, 2001, '海外摄影展参赛费', 3000.00, 'EXPENSE', 'PENDING', 1019, NULL, NULL, '', '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12025, 2002, '企业赞助编程大赛', 10000.00, 'INCOME', 'PENDING', 1020, NULL, NULL, '', '2026-08-12 10:00:00', '2026-08-12 10:00:00', 0);

-- REJECTED 经费
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12026, 2004, '超出预算的活动', 99999.00, 'EXPENSE', 'REJECTED', 1017, 1, '2026-07-20 10:00:00', '金额远超预算，驳回', '2026-07-15 10:00:00', '2026-07-20 10:00:00', 0);
INSERT INTO fund (id, club_id, title, amount, type, status, apply_user_id, audit_user_id, audit_time, audit_remark, create_time, update_time, deleted)
VALUES (12027, 2006, '比赛报名费', 2000.00, 'EXPENSE', 'REJECTED', 1011, 1, '2026-08-08 10:00:00', '比赛时间与考试冲突，驳回', '2026-08-05 10:00:00', '2026-08-08 10:00:00', 0);

-- ===== 15. fund_record（23条） =====
-- 仅给 APPROVED 的 fund 编流水，balance_after 逐笔累加

-- 社团 2001: 3000 - 1200 - 300 = 1500
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13001, 12001, 2001, 3000.00, 'INCOME', 3000.00, '2026-05-20 10:00:00', '2026-05-20 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13002, 12002, 2001, 1200.00, 'EXPENSE', 1800.00, '2026-06-05 14:00:00', '2026-06-05 14:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13003, 12003, 2001, 300.00, 'EXPENSE', 1500.00, '2026-07-25 10:00:00', '2026-07-25 10:00:00', 0);

-- 社团 2002: 5000 - 2000 - 1500 = 1500
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13004, 12004, 2002, 5000.00, 'INCOME', 5000.00, '2026-05-22 09:00:00', '2026-05-22 09:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13005, 12005, 2002, 2000.00, 'EXPENSE', 3000.00, '2026-06-10 14:00:00', '2026-06-10 14:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13006, 12006, 2002, 1500.00, 'EXPENSE', 1500.00, '2026-07-30 10:00:00', '2026-07-30 10:00:00', 0);

-- 社团 2003: 2000 - 800 = 1200
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13007, 12007, 2003, 2000.00, 'INCOME', 2000.00, '2026-05-25 10:00:00', '2026-05-25 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13008, 12008, 2003, 800.00, 'EXPENSE', 1200.00, '2026-06-15 14:00:00', '2026-06-15 14:00:00', 0);

-- 社团 2004: 4000 - 1500 - 500 = 2000
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13009, 12009, 2004, 4000.00, 'INCOME', 4000.00, '2026-05-28 10:00:00', '2026-05-28 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13010, 12010, 2004, 1500.00, 'EXPENSE', 2500.00, '2026-06-20 14:00:00', '2026-06-20 14:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13011, 12011, 2004, 500.00, 'EXPENSE', 2000.00, '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);

-- 社团 2005: 2500 - 800 = 1700
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13012, 12012, 2005, 2500.00, 'INCOME', 2500.00, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13013, 12013, 2005, 800.00, 'EXPENSE', 1700.00, '2026-06-25 14:00:00', '2026-06-25 14:00:00', 0);

-- 社团 2006: 8000 - 5000 - 1200 = 1800
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13014, 12014, 2006, 8000.00, 'INCOME', 8000.00, '2026-06-05 10:00:00', '2026-06-05 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13015, 12015, 2006, 5000.00, 'EXPENSE', 3000.00, '2026-07-05 14:00:00', '2026-07-05 14:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13016, 12016, 2006, 1200.00, 'EXPENSE', 1800.00, '2026-08-05 10:00:00', '2026-08-05 10:00:00', 0);

-- 社团 2007: 3000
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13017, 12017, 2007, 3000.00, 'INCOME', 3000.00, '2026-06-08 10:00:00', '2026-06-08 10:00:00', 0);

-- 社团 2008: 2000 - 800 = 1200
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13018, 12018, 2008, 2000.00, 'INCOME', 2000.00, '2026-06-10 10:00:00', '2026-06-10 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13019, 12019, 2008, 800.00, 'EXPENSE', 1200.00, '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

-- 社团 2009: 2000 - 600 = 1400
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13020, 12020, 2009, 2000.00, 'INCOME', 2000.00, '2026-06-12 10:00:00', '2026-06-12 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13021, 12021, 2009, 600.00, 'EXPENSE', 1400.00, '2026-08-08 10:00:00', '2026-08-08 10:00:00', 0);

-- 社团 2010: 1500 - 500 = 1000
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13022, 12022, 2010, 1500.00, 'INCOME', 1500.00, '2026-06-15 10:00:00', '2026-06-15 10:00:00', 0);
INSERT INTO fund_record (id, fund_id, club_id, amount, type, balance_after, create_time, update_time, deleted)
VALUES (13023, 12023, 2010, 500.00, 'EXPENSE', 1000.00, '2026-08-10 10:00:00', '2026-08-10 10:00:00', 0);

-- ===== 16. sys_login_log（50条） =====

INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15001, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-14 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15002, 'stu1001', '10.1.2.1', '0', '登录成功', '2026-08-14 08:15:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15003, 'stu1002', '10.1.2.2', '0', '登录成功', '2026-08-14 08:20:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15004, 'pres1001', '10.1.3.1', '0', '登录成功', '2026-08-14 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15005, 'pres1002', '10.1.3.2', '0', '登录成功', '2026-08-14 08:35:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15006, 'stu1003', '10.1.2.3', '0', '登录成功', '2026-08-14 08:45:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15007, 'unknown', '10.1.9.9', '1', '用户名不存在', '2026-08-14 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15008, 'stu1001', '10.1.2.1', '1', '密码错误', '2026-08-14 09:05:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15009, 'stu1004', '10.1.2.4', '0', '登录成功', '2026-08-14 09:10:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15010, 'pres1003', '10.1.3.3', '0', '登录成功', '2026-08-14 09:15:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15011, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-13 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15012, 'stu1005', '10.1.2.5', '0', '登录成功', '2026-08-13 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15013, 'pres1004', '10.1.3.4', '0', '登录成功', '2026-08-13 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15014, 'stu1006', '10.1.2.6', '0', '登录成功', '2026-08-13 09:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15015, 'stu1007', '10.1.2.7', '0', '登录成功', '2026-08-13 10:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15016, 'hacker', '10.1.9.8', '1', '用户名不存在', '2026-08-13 14:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15017, 'pres1005', '10.1.3.5', '0', '登录成功', '2026-08-13 14:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15018, 'stu1008', '10.1.2.8', '0', '登录成功', '2026-08-13 15:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15019, 'pres1006', '10.1.3.6', '0', '登录成功', '2026-08-12 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15020, 'stu1009', '10.1.2.9', '0', '登录成功', '2026-08-12 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15021, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-12 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15022, 'stu1010', '10.1.2.10', '0', '登录成功', '2026-08-12 09:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15023, 'pres1007', '10.1.3.7', '0', '登录成功', '2026-08-12 10:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15024, 'pres1008', '10.1.3.8', '0', '登录成功', '2026-08-11 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15025, 'stu1011', '10.1.2.11', '0', '登录成功', '2026-08-11 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15026, 'pres1009', '10.1.3.9', '0', '登录成功', '2026-08-11 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15027, 'stu1012', '10.1.2.12', '0', '登录成功', '2026-08-11 09:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15028, 'pres1010', '10.1.3.10', '0', '登录成功', '2026-08-11 10:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15029, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-10 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15030, 'stu1013', '10.1.2.13', '0', '登录成功', '2026-08-10 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15031, 'stu1014', '10.1.2.14', '0', '登录成功', '2026-08-10 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15032, 'stu1015', '10.1.2.15', '0', '登录成功', '2026-08-10 09:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15033, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-09 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15034, 'stu1016', '10.1.2.16', '0', '登录成功', '2026-08-09 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15035, 'stu1017', '10.1.2.17', '0', '登录成功', '2026-08-09 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15036, 'stu1018', '10.1.2.18', '0', '登录成功', '2026-08-09 09:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15037, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-08 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15038, 'pres1001', '10.1.3.1', '0', '登录成功', '2026-08-08 08:30:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15039, 'pres1002', '10.1.3.2', '0', '登录成功', '2026-08-08 09:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15040, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-07 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15041, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-06 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15042, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-05 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15043, 'stu1001', '10.1.2.1', '0', '登录成功', '2026-08-05 10:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15044, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-04 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15045, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-03 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15046, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-02 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15047, 'admin', '10.1.1.1', '0', '登录成功', '2026-08-01 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15048, 'stu1001', '192.168.1.100', '1', '密码错误', '2026-08-01 22:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15049, 'admin', '10.1.1.1', '0', '登录成功', '2026-07-31 08:00:00');
INSERT INTO sys_login_log (id, user_name, ipaddr, status, msg, login_time)
VALUES (15050, 'admin', '10.1.1.1', '0', '登录成功', '2026-07-30 08:00:00');

-- ===== 17. sys_oper_log（40条） =====

INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16001, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-14 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16002, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-14 09:05:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16003, '用户管理', 2, 'com.club.service.impl.UserServiceImpl.update', 'PUT', 'admin', '/api/v1/system/user', '10.1.1.1', 0, '2026-08-14 09:10:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16004, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-14 09:15:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16005, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-14 09:20:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16006, '公告管理', 1, 'com.club.service.impl.NoticeServiceImpl.create', 'POST', 'admin', '/api/v1/notice', '10.1.1.1', 0, '2026-08-13 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16007, '公告管理', 2, 'com.club.service.impl.NoticeServiceImpl.update', 'PUT', 'admin', '/api/v1/notice', '10.1.1.1', 0, '2026-08-13 10:05:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16008, '字典管理', 1, 'com.club.service.impl.DictServiceImpl.create', 'POST', 'admin', '/api/v1/system/dict', '10.1.1.1', 0, '2026-08-13 11:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16009, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-12 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16010, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-12 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16011, '用户管理', 1, 'com.club.service.impl.UserServiceImpl.create', 'POST', 'admin', '/api/v1/system/user', '10.1.1.1', 0, '2026-08-12 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16012, '参数设置', 2, 'com.club.service.impl.ConfigServiceImpl.update', 'PUT', 'admin', '/api/v1/system/config', '10.1.1.1', 0, '2026-08-11 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16013, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-11 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16014, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-11 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16015, '活动管理', 1, 'com.club.service.impl.ActivityServiceImpl.create', 'POST', 'pres1001', '/api/v1/activity', '10.1.3.1', 0, '2026-08-10 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16016, '活动管理', 2, 'com.club.service.impl.ActivityServiceImpl.update', 'PUT', 'pres1001', '/api/v1/activity', '10.1.3.1', 0, '2026-08-10 10:05:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16017, '纳新管理', 1, 'com.club.service.impl.RecruitServiceImpl.create', 'POST', 'pres1002', '/api/v1/recruit', '10.1.3.2', 0, '2026-08-10 11:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16018, '成员管理', 2, 'com.club.service.impl.ClubMemberServiceImpl.audit', 'POST', 'pres1003', '/api/v1/club/member/audit', '10.1.3.3', 0, '2026-08-10 14:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16019, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-09 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16020, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-09 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16021, '用户管理', 2, 'com.club.service.impl.UserServiceImpl.update', 'PUT', 'admin', '/api/v1/system/user', '10.1.1.1', 0, '2026-08-09 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16022, '公告管理', 1, 'com.club.service.impl.NoticeServiceImpl.create', 'POST', 'pres1004', '/api/v1/notice', '10.1.3.4', 0, '2026-08-08 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16023, '活动管理', 1, 'com.club.service.impl.ActivityServiceImpl.create', 'POST', 'pres1005', '/api/v1/activity', '10.1.3.5', 0, '2026-08-08 10:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16024, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-07 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16025, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-07 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16026, '成员管理', 2, 'com.club.service.impl.ClubMemberServiceImpl.audit', 'POST', 'pres1006', '/api/v1/club/member/audit', '10.1.3.6', 0, '2026-08-06 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16027, '纳新管理', 1, 'com.club.service.impl.RecruitServiceImpl.create', 'POST', 'pres1007', '/api/v1/recruit', '10.1.3.7', 0, '2026-08-06 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16028, '活动管理', 2, 'com.club.service.impl.ActivityServiceImpl.update', 'PUT', 'pres1008', '/api/v1/activity', '10.1.3.8', 0, '2026-08-05 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16029, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-05 11:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16030, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-05 11:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16031, '用户管理', 1, 'com.club.service.impl.UserServiceImpl.create', 'POST', 'admin', '/api/v1/system/user', '10.1.1.1', 0, '2026-08-04 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16032, '公告管理', 2, 'com.club.service.impl.NoticeServiceImpl.update', 'PUT', 'admin', '/api/v1/notice', '10.1.1.1', 0, '2026-08-04 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16033, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-03 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16034, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-03 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16035, '活动管理', 1, 'com.club.service.impl.ActivityServiceImpl.create', 'POST', 'pres1009', '/api/v1/activity', '10.1.3.9', 0, '2026-08-02 10:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16036, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-08-02 11:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16037, '用户管理', 2, 'com.club.service.impl.UserServiceImpl.update', 'PUT', 'admin', '/api/v1/system/user', '10.1.1.1', 0, '2026-08-01 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16038, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-08-01 09:30:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16039, '社团审批', 2, 'com.club.service.impl.ClubServiceImpl.audit', 'POST', 'admin', '/api/v1/club/audit', '10.1.1.1', 0, '2026-07-31 09:00:00');
INSERT INTO sys_oper_log (id, title, business_type, method, request_method, oper_name, oper_url, oper_ip, status, oper_time)
VALUES (16040, '经费审批', 2, 'com.club.service.impl.FundServiceImpl.audit', 'POST', 'admin', '/api/v1/fund/audit', '10.1.1.1', 0, '2026-07-31 09:30:00');
