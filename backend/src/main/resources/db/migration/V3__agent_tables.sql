-- V3: AI Agent 底座表（会话 / 消息 / 工具调用审计）
-- 设计说明：
--   agent_session  会话（多轮上下文归属）
--   agent_message  消息（user/assistant，tool_calls 存 JSON 便于复现编排过程）
--   agent_tool_log 工具调用轨迹（可解释性 + 审计：谁、何时、调了什么、耗时多少）

CREATE TABLE agent_session (
    id          BIGINT       NOT NULL COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    title       VARCHAR(128) NOT NULL DEFAULT '新对话' COMMENT '会话标题（取首条用户消息前 30 字）',
    model       VARCHAR(64)  NOT NULL DEFAULT 'mimo-v2.5' COMMENT '使用的模型',
    create_time DATETIME     NOT NULL,
    update_time DATETIME     NOT NULL,
    create_by   BIGINT       DEFAULT NULL,
    update_by   BIGINT       DEFAULT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_agent_session_user (user_id, create_time)
);

CREATE TABLE agent_message (
    id          BIGINT   NOT NULL COMMENT '主键',
    session_id  BIGINT   NOT NULL COMMENT '会话 ID',
    role        VARCHAR(16) NOT NULL COMMENT 'user / assistant',
    content     TEXT COMMENT '消息内容（assistant 为完整回复，流式结束后落库）',
    tool_calls  TEXT COMMENT '工具调用 JSON（assistant 消息的编排轨迹，可空）',
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_by   BIGINT   DEFAULT NULL,
    update_by   BIGINT   DEFAULT NULL,
    deleted     TINYINT  NOT NULL DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_agent_message_session (session_id, id)
);

CREATE TABLE agent_tool_log (
    id             BIGINT       NOT NULL COMMENT '主键',
    session_id     BIGINT       NOT NULL COMMENT '会话 ID',
    message_id     BIGINT       DEFAULT NULL COMMENT '触发工具调用的 assistant 消息 ID',
    user_id        BIGINT       NOT NULL COMMENT '调用用户',
    tool_name      VARCHAR(64)  NOT NULL COMMENT '工具名',
    arguments      TEXT COMMENT '入参 JSON',
    result_summary TEXT COMMENT '结果摘要（截断，防超长）',
    duration_ms    BIGINT       NOT NULL DEFAULT 0 COMMENT '执行耗时（毫秒）',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1 成功 / 0 失败',
    create_time    DATETIME     NOT NULL,
    update_time    DATETIME     NOT NULL,
    create_by      BIGINT       DEFAULT NULL,
    update_by      BIGINT       DEFAULT NULL,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    remark         VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_agent_tool_log_session (session_id),
    KEY idx_agent_tool_log_user (user_id, create_time)
);
