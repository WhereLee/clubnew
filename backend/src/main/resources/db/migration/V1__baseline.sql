-- V1 基线：全量建表 + 种子数据（MySQL 8 / H2 MySQL-MODE 双兼容）
-- 说明：不包含 DROP/SET/ENGINE/CHARSET 语句，保证 Flyway 在 H2 测试库同样可执行
-- =============================================
-- 社团全流程管理系统 · 全量建表 + 种子数据
-- 数据库: club_flow (MySQL 8.0)
-- =============================================


-- -------------------------------------------
-- 阶段01: 基础设施表
-- -------------------------------------------

CREATE TABLE sys_config (
    id            BIGINT       NOT NULL COMMENT '主键',
    config_name   VARCHAR(100) DEFAULT '' COMMENT '参数名称',
    config_key    VARCHAR(100) DEFAULT '' COMMENT '参数键名',
    config_value  VARCHAR(500) DEFAULT '' COMMENT '参数值',
    config_type   CHAR(1)      DEFAULT 'N' COMMENT '系统内置(Y/N)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted       INT          DEFAULT 0 COMMENT '逻辑删除(0未删 1已删)',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE dict_type (
    id            BIGINT       NOT NULL COMMENT '主键',
    dict_name     VARCHAR(100) DEFAULT '' COMMENT '字典名称',
    dict_type     VARCHAR(100) DEFAULT '' COMMENT '字典类型',
    status        CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted       INT          DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type)
);

CREATE TABLE dict_data (
    id            BIGINT       NOT NULL COMMENT '主键',
    dict_sort     INT          DEFAULT 0 COMMENT '排序',
    dict_label    VARCHAR(100) DEFAULT '' COMMENT '标签(显示值)',
    dict_value    VARCHAR(100) DEFAULT '' COMMENT '键值(存储值)',
    dict_type     VARCHAR(100) DEFAULT '' COMMENT '所属字典类型',
    is_default    CHAR(1)      DEFAULT 'N' COMMENT '是否默认(Y/N)',
    status        CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted       INT          DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_dict_type (dict_type)
);

CREATE TABLE sys_oper_log (
    id              BIGINT        NOT NULL COMMENT '主键',
    title           VARCHAR(50)   DEFAULT '' COMMENT '模块标题',
    business_type   INT           DEFAULT 0 COMMENT '业务类型(0其它 1新增 2修改 3删除)',
    method          VARCHAR(200)  DEFAULT '' COMMENT '方法名称',
    request_method  VARCHAR(10)   DEFAULT '' COMMENT '请求方式',
    oper_name       VARCHAR(50)   DEFAULT '' COMMENT '操作人员',
    oper_url        VARCHAR(255)  DEFAULT '' COMMENT '请求URL',
    oper_ip         VARCHAR(128)  DEFAULT '' COMMENT '主机地址',
    oper_param      TEXT          COMMENT '请求参数',
    json_result     TEXT          COMMENT '返回参数',
    status          INT           DEFAULT 0 COMMENT '操作状态(0成功 1失败)',
    error_msg       VARCHAR(2000) DEFAULT '' COMMENT '错误消息',
    oper_time       DATETIME      DEFAULT NULL COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_oper_time (oper_time)
);

CREATE TABLE sys_login_log (
    id          BIGINT       NOT NULL COMMENT '主键',
    user_name   VARCHAR(50)  DEFAULT '' COMMENT '用户名',
    ipaddr      VARCHAR(128) DEFAULT '' COMMENT '登录IP',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态(0成功 1失败)',
    msg         VARCHAR(255) DEFAULT '' COMMENT '提示消息',
    login_time  DATETIME     DEFAULT NULL COMMENT '访问时间',
    PRIMARY KEY (id),
    KEY idx_login_time (login_time)
);

-- -------------------------------------------
-- 阶段02: RBAC 权限与认证
-- -------------------------------------------

CREATE TABLE sys_user (
    id          BIGINT       NOT NULL COMMENT '主键',
    username    VARCHAR(50)  DEFAULT '' COMMENT '登录名',
    password    VARCHAR(100) DEFAULT '' COMMENT '密码(BCrypt)',
    nickname    VARCHAR(50)  DEFAULT '' COMMENT '昵称',
    email       VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    phone       VARCHAR(20)  DEFAULT '' COMMENT '手机号',
    avatar      VARCHAR(255) DEFAULT '' COMMENT '头像地址',
    user_type   VARCHAR(20)  DEFAULT 'STUDENT' COMMENT '身份类型',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by   VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
);

CREATE TABLE sys_role (
    id          BIGINT       NOT NULL COMMENT '主键',
    role_name   VARCHAR(50)  DEFAULT '' COMMENT '角色名称',
    role_key    VARCHAR(100) DEFAULT '' COMMENT '角色权限字符串',
    role_sort   INT          DEFAULT 0 COMMENT '排序',
    data_scope  CHAR(1)      DEFAULT '4' COMMENT '数据范围(1全部 2本社团及以下 3本社团 4仅本人 5自定义)',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1停用)',
    create_by   VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE sys_menu (
    id          BIGINT       NOT NULL COMMENT '主键',
    menu_name   VARCHAR(50)  DEFAULT '' COMMENT '菜单名称',
    parent_id   BIGINT       DEFAULT 0 COMMENT '父菜单ID',
    order_num   INT          DEFAULT 0 COMMENT '排序',
    path        VARCHAR(200) DEFAULT '' COMMENT '路由地址',
    component   VARCHAR(255) DEFAULT '' COMMENT '组件路径',
    perms       VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    menu_type   CHAR(1)      DEFAULT '' COMMENT '类型(M目录 C菜单 F按钮)',
    icon        VARCHAR(100) DEFAULT '' COMMENT '图标',
    status      CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1隐藏)',
    create_by   VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
);

-- -------------------------------------------
-- 阶段03: 社团生命周期 + 成员管理
-- -------------------------------------------

CREATE TABLE club (
    id              BIGINT        NOT NULL COMMENT '主键',
    name            VARCHAR(100)  DEFAULT '' COMMENT '社团名称',
    code            VARCHAR(50)   DEFAULT '' COMMENT '社团编号',
    logo            VARCHAR(255)  DEFAULT '' COMMENT '社团logo',
    description     VARCHAR(1000) DEFAULT '' COMMENT '社团简介',
    category        VARCHAR(50)   DEFAULT '' COMMENT '社团类别(字典)',
    advisor_id      BIGINT        DEFAULT NULL COMMENT '指导老师ID',
    president_id    BIGINT        DEFAULT NULL COMMENT '社长ID',
    status          VARCHAR(20)   DEFAULT 'PENDING' COMMENT '状态',
    member_count    INT           DEFAULT 0 COMMENT '成员数',
    star_level      INT           DEFAULT 0 COMMENT '星级(1~5)',
    create_user_id  BIGINT        DEFAULT NULL COMMENT '申请人ID',
    apply_time      DATETIME      DEFAULT NULL COMMENT '申请时间',
    audit_time      DATETIME      DEFAULT NULL COMMENT '审核时间',
    audit_user_id   BIGINT        DEFAULT NULL COMMENT '审核人ID',
    audit_remark    VARCHAR(500)  DEFAULT '' COMMENT '审核意见',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted         INT           DEFAULT 0 COMMENT '逻辑删除',
    remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_club_name (name),
    UNIQUE KEY uk_club_code (code)
);

CREATE TABLE club_member (
    id            BIGINT       NOT NULL COMMENT '主键',
    club_id       BIGINT       NOT NULL COMMENT '社团ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    member_role   VARCHAR(20)  DEFAULT 'MEMBER' COMMENT '成员角色',
    status        VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态',
    apply_time    DATETIME     DEFAULT NULL COMMENT '申请时间',
    join_time     DATETIME     DEFAULT NULL COMMENT '加入时间',
    audit_user_id BIGINT       DEFAULT NULL COMMENT '审批人ID',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted       INT          DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_club_user (club_id, user_id)
);

-- -------------------------------------------
-- 阶段04: 纳新 + 活动
-- -------------------------------------------

CREATE TABLE recruit (
    id             BIGINT        NOT NULL COMMENT '主键',
    club_id        BIGINT        NOT NULL COMMENT '社团ID',
    title          VARCHAR(100)  DEFAULT '' COMMENT '纳新标题',
    description    VARCHAR(1000) DEFAULT '' COMMENT '纳新说明',
    quota          INT           DEFAULT 0 COMMENT '名额上限',
    applied_count  INT           DEFAULT 0 COMMENT '已报名数',
    start_time     DATETIME      DEFAULT NULL COMMENT '开始时间',
    end_time       DATETIME      DEFAULT NULL COMMENT '结束时间',
    status         VARCHAR(20)   DEFAULT 'NOT_STARTED' COMMENT '状态',
    requirements   VARCHAR(500)  DEFAULT '' COMMENT '报名要求',
    version        INT           DEFAULT 0 COMMENT '乐观锁版本号',
    create_by      VARCHAR(50)   DEFAULT '' COMMENT '创建人',
    create_time    DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time    DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted        INT           DEFAULT 0 COMMENT '逻辑删除',
    remark         VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE recruit_record (
    id               BIGINT       NOT NULL COMMENT '主键',
    recruit_id       BIGINT       NOT NULL COMMENT '纳新ID',
    user_id          BIGINT       NOT NULL COMMENT '用户ID',
    status           VARCHAR(20)  DEFAULT 'PENDING' COMMENT '状态',
    apply_time       DATETIME     DEFAULT NULL COMMENT '报名时间',
    interview_time   DATETIME     DEFAULT NULL COMMENT '面试时间',
    interview_result VARCHAR(500) DEFAULT '' COMMENT '面试结果',
    version          INT          DEFAULT 0 COMMENT '乐观锁',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time      DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time      DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted          INT          DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recruit_user (recruit_id, user_id)
);

CREATE TABLE activity (
    id              BIGINT        NOT NULL COMMENT '主键',
    club_id         BIGINT        NOT NULL COMMENT '社团ID',
    title           VARCHAR(100)  DEFAULT '' COMMENT '活动标题',
    content         TEXT          COMMENT '活动内容',
    start_time      DATETIME      DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME      DEFAULT NULL COMMENT '结束时间',
    quota           INT           DEFAULT 0 COMMENT '名额',
    applied_count   INT           DEFAULT 0 COMMENT '已报名数',
    status          VARCHAR(20)   DEFAULT 'DRAFT' COMMENT '状态',
    checkin_enabled CHAR(1)       DEFAULT 'N' COMMENT '是否开启签到(Y/N)',
    version         INT           DEFAULT 0 COMMENT '乐观锁',
    create_by       VARCHAR(50)   DEFAULT '' COMMENT '创建人',
    create_time     DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted         INT           DEFAULT 0 COMMENT '逻辑删除',
    remark          VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE activity_signup (
    id          BIGINT       NOT NULL COMMENT '主键',
    activity_id BIGINT       NOT NULL COMMENT '活动ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    status      VARCHAR(20)  DEFAULT 'SIGNED' COMMENT '状态(SIGNED/CANCELLED)',
    signup_time DATETIME     DEFAULT NULL COMMENT '报名时间',
    version     INT          DEFAULT 0 COMMENT '乐观锁',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_user (activity_id, user_id)
);

CREATE TABLE activity_checkin (
    id           BIGINT   NOT NULL COMMENT '主键',
    activity_id  BIGINT   NOT NULL COMMENT '活动ID',
    user_id      BIGINT   NOT NULL COMMENT '用户ID',
    checkin_time DATETIME DEFAULT NULL COMMENT '签到时间',
    status       CHAR(1)  DEFAULT '1' COMMENT '状态(1已签到)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time  DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time  DATETIME DEFAULT NULL COMMENT '更新时间',
    deleted      INT      DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_activity_checkin_user (activity_id, user_id)
);

-- -------------------------------------------
-- 阶段05: 内容互动 + 经费 + 排行榜
-- -------------------------------------------

CREATE TABLE notice (
    id              BIGINT       NOT NULL COMMENT '主键',
    club_id         BIGINT       DEFAULT 0 COMMENT '社团ID(0=平台级)',
    title           VARCHAR(100) DEFAULT '' COMMENT '标题',
    content         TEXT         COMMENT '内容',
    publish_user_id BIGINT       DEFAULT NULL COMMENT '发布人',
    publish_time    DATETIME     DEFAULT NULL COMMENT '发布时间',
    status          CHAR(1)      DEFAULT '0' COMMENT '状态(0正常 1下架)',
    top             CHAR(1)      DEFAULT 'N' COMMENT '是否置顶(Y/N)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time     DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time     DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted         INT          DEFAULT 0 COMMENT '逻辑删除',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE post (
    id            BIGINT  NOT NULL COMMENT '主键',
    club_id       BIGINT  DEFAULT NULL COMMENT '社团ID',
    author_id     BIGINT  DEFAULT NULL COMMENT '作者',
    content       TEXT    COMMENT '内容',
    like_count    INT     DEFAULT 0 COMMENT '点赞数',
    comment_count INT     DEFAULT 0 COMMENT '评论数',
    status        CHAR(1) DEFAULT '0' COMMENT '状态(0正常 1删除)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME DEFAULT NULL COMMENT '更新时间',
    deleted       INT     DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE comment (
    id          BIGINT       NOT NULL COMMENT '主键',
    biz_type    VARCHAR(20)  DEFAULT '' COMMENT '业务类型(POST/NOTICE/ACTIVITY)',
    biz_id      BIGINT       DEFAULT NULL COMMENT '业务ID',
    user_id     BIGINT       DEFAULT NULL COMMENT '评论人',
    content     VARCHAR(500) DEFAULT '' COMMENT '内容',
    like_count  INT          DEFAULT 0 COMMENT '点赞数',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time DATETIME     DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME     DEFAULT NULL COMMENT '更新时间',
    deleted     INT          DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_biz (biz_type, biz_id)
);

CREATE TABLE user_like (
    id          BIGINT      NOT NULL COMMENT '主键',
    biz_type    VARCHAR(20) DEFAULT '' COMMENT '业务类型',
    biz_id      BIGINT      DEFAULT NULL COMMENT '业务ID',
    user_id     BIGINT      DEFAULT NULL COMMENT '点赞人',
    status      CHAR(1)     DEFAULT '1' COMMENT '状态(1已赞 0取消)',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time DATETIME    DEFAULT NULL COMMENT '创建时间',
    update_time DATETIME    DEFAULT NULL COMMENT '更新时间',
    deleted     INT         DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_like (biz_type, biz_id, user_id)
);

CREATE TABLE fund (
    id            BIGINT        NOT NULL COMMENT '主键',
    club_id       BIGINT        NOT NULL COMMENT '社团ID',
    title         VARCHAR(100)  DEFAULT '' COMMENT '申请标题',
    amount        DECIMAL(10,2) DEFAULT 0 COMMENT '金额',
    type          VARCHAR(20)   DEFAULT '' COMMENT '类型(INCOME/EXPENSE)',
    status        VARCHAR(20)   DEFAULT 'PENDING' COMMENT '状态',
    apply_user_id BIGINT        DEFAULT NULL COMMENT '申请人',
    audit_user_id BIGINT        DEFAULT NULL COMMENT '审核人',
    audit_time    DATETIME      DEFAULT NULL COMMENT '审核时间',
    audit_remark  VARCHAR(500)  DEFAULT '' COMMENT '审核意见',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted       INT           DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

CREATE TABLE fund_record (
    id            BIGINT        NOT NULL COMMENT '主键',
    fund_id       BIGINT        DEFAULT NULL COMMENT '关联经费申请',
    club_id       BIGINT        NOT NULL COMMENT '社团ID',
    amount        DECIMAL(10,2) DEFAULT 0 COMMENT '金额',
    type          VARCHAR(20)   DEFAULT '' COMMENT '收入/支出',
    balance_after DECIMAL(12,2) DEFAULT 0 COMMENT '变动后余额',
    create_by     VARCHAR(50)  DEFAULT '' COMMENT '创建人',
    create_time   DATETIME      DEFAULT NULL COMMENT '创建时间',
    update_time   DATETIME      DEFAULT NULL COMMENT '更新时间',
    deleted       INT           DEFAULT 0 COMMENT '逻辑删除',
    remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
);

-- -------------------------------------------
-- 种子数据
-- -------------------------------------------

-- 内置管理员 (admin / admin123)
INSERT INTO sys_user (id, username, password, nickname, user_type, status, create_time, update_time, deleted)
VALUES (1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', 'ADMIN', '0', NOW(), NOW(), 0);

-- 内置角色
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_time, update_time, deleted)
VALUES (1, '管理员', 'admin', 1, '1', '0', NOW(), NOW(), 0);
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_time, update_time, deleted)
VALUES (2, '社长', 'president', 2, '3', '0', NOW(), NOW(), 0);
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_time, update_time, deleted)
VALUES (3, '学生', 'student', 3, '4', '0', NOW(), NOW(), 0);

-- 管理员角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 菜单权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, icon, status, create_time, update_time, deleted)
VALUES
(1,  '系统管理', 0, 1, 'system', NULL, NULL, 'M', 'system', '0', NOW(), NOW(), 0),
(2,  '用户管理', 1, 1, 'user', 'system/user/index', 'system:user:list', 'C', 'user', '0', NOW(), NOW(), 0),
(3,  '角色管理', 1, 2, 'role', 'system/role/index', 'system:role:list', 'C', 'peoples', '0', NOW(), NOW(), 0),
(4,  '菜单管理', 1, 3, 'menu', 'system/menu/index', 'system:menu:list', 'C', 'tree-table', '0', NOW(), NOW(), 0),
(5,  '字典管理', 1, 4, 'dict', 'system/dict/index', 'system:dict:list', 'C', 'dict', '0', NOW(), NOW(), 0),
(6,  '参数设置', 1, 5, 'config', 'system/config/index', 'system:config:list', 'C', 'edit', '0', NOW(), NOW(), 0),
(7,  '社团管理', 0, 2, 'club', NULL, NULL, 'M', 'guide', '0', NOW(), NOW(), 0),
(8,  '社团列表', 7, 1, 'list', 'club/list/index', 'club:*', 'C', 'list', '0', NOW(), NOW(), 0),
(9,  '社团审批', 7, 2, 'audit', 'club/audit/index', 'club:audit', 'C', 'form', '0', NOW(), NOW(), 0),
(10, '纳新管理', 7, 3, 'recruit', 'club/recruit/index', 'recruit:manage', 'C', 'job', '0', NOW(), NOW(), 0),
(11, '活动管理', 7, 4, 'activity', 'club/activity/index', 'activity:manage', 'C', 'international', '0', NOW(), NOW(), 0),
(12, '经费管理', 7, 5, 'fund', 'club/fund/index', 'fund:manage', 'C', 'money', '0', NOW(), NOW(), 0),
(13, '排行榜', 0, 3, 'rank', 'rank/index', NULL, 'M', 'chart', '0', NOW(), NOW(), 0),
(14, '日志管理', 0, 4, 'log', NULL, NULL, 'M', 'log', '0', NOW(), NOW(), 0),
(15, '操作日志', 14, 1, 'oper', 'log/oper/index', 'system:operlog:list', 'C', 'form', '0', NOW(), NOW(), 0),
(16, '登录日志', 14, 2, 'login', 'log/login/index', 'system:loginlog:list', 'C', 'logininfor', '0', NOW(), NOW(), 0);

-- 管理员角色-全部菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 学生角色-有限菜单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (3, 7), (3, 8), (3, 10), (3, 11), (3, 13);

-- 字典类型
INSERT INTO dict_type (id, dict_name, dict_type, status, create_time, update_time, deleted)
VALUES
(1, '社团状态', 'club_status', '0', NOW(), NOW(), 0),
(2, '社团类别', 'club_category', '0', NOW(), NOW(), 0),
(3, '成员角色', 'member_role', '0', NOW(), NOW(), 0),
(4, '用户类型', 'user_type', '0', NOW(), NOW(), 0);

-- 字典数据
INSERT INTO dict_data (id, dict_sort, dict_label, dict_value, dict_type, is_default, status, create_time, update_time, deleted)
VALUES
(1,  1, '待审批', 'PENDING', 'club_status', 'N', '0', NOW(), NOW(), 0),
(2,  2, '正常运营', 'APPROVED', 'club_status', 'N', '0', NOW(), NOW(), 0),
(3,  3, '已暂停', 'SUSPENDED', 'club_status', 'N', '0', NOW(), NOW(), 0),
(4,  4, '已注销', 'DISSOLVED', 'club_status', 'N', '0', NOW(), NOW(), 0),
(5,  5, '已驳回', 'REJECTED', 'club_status', 'N', '0', NOW(), NOW(), 0),
(6,  1, '学术科技', 'ACADEMIC', 'club_category', 'N', '0', NOW(), NOW(), 0),
(7,  2, '文化艺术', 'CULTURE', 'club_category', 'N', '0', NOW(), NOW(), 0),
(8,  3, '体育竞技', 'SPORTS', 'club_category', 'N', '0', NOW(), NOW(), 0),
(9,  4, '志愿服务', 'VOLUNTEER', 'club_category', 'N', '0', NOW(), NOW(), 0),
(10, 1, '社长', 'PRESIDENT', 'member_role', 'N', '0', NOW(), NOW(), 0),
(11, 2, '副社长', 'VICE', 'member_role', 'N', '0', NOW(), NOW(), 0),
(12, 3, '普通成员', 'MEMBER', 'member_role', 'N', '0', NOW(), NOW(), 0),
(13, 1, '学生', 'STUDENT', 'user_type', 'N', '0', NOW(), NOW(), 0),
(14, 2, '社长', 'PRESIDENT', 'user_type', 'N', '0', NOW(), NOW(), 0),
(15, 3, '指导老师', 'ADVISOR', 'user_type', 'N', '0', NOW(), NOW(), 0),
(16, 4, '管理员', 'ADMIN', 'user_type', 'N', '0', NOW(), NOW(), 0);

-- 参数配置
INSERT INTO sys_config (id, config_name, config_key, config_value, config_type, create_time, update_time, deleted)
VALUES
(1, '首页公告', 'sys.index.notice', '欢迎使用社团全流程管理系统', 'Y', NOW(), NOW(), 0),
(2, '用户初始密码', 'sys.user.initPassword', '123456', 'Y', NOW(), NOW(), 0),
(3, '社团编号前缀', 'sys.club.codePrefix', 'CLUB', 'Y', NOW(), NOW(), 0);

