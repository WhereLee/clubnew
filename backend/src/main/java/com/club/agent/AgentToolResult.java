package com.club.agent;

/**
 * Agent 工具执行结果：回传给 LLM 的文本内容。
 *
 * @param content    结果文本（LLM 基于它生成最终回答）
 * @param displayName 展示名（前端工具调用轨迹里显示，可为 null 用 name）
 */
public record AgentToolResult(String content, String displayName) {

    public static AgentToolResult of(String content) {
        return new AgentToolResult(content, null);
    }

    public static AgentToolResult of(String content, String displayName) {
        return new AgentToolResult(content, displayName);
    }
}
