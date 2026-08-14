package com.club.agent;

/**
 * Agent 工具权限档位。
 * 每个工具声明自己的最低权限，AgentService 在暴露工具给 LLM 前先按当前用户过滤，
 * 工具执行时二次校验（防止 LLM 构造出越权调用）。
 */
public enum ToolAccess {
    /** 任意登录用户可用（查自己的数据、平台公开信息） */
    ALL,
    /** 社团管理者（社长/副社长）可用（查本社团运营数据） */
    CLUB_ADMIN,
    /** 系统管理员可用（日志/慢查询/系统健康） */
    ADMIN
}
