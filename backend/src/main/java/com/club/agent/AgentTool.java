package com.club.agent;

import java.util.Map;

/**
 * Agent 工具：暴露给 LLM 的能力单元。
 *
 * 实现规范：
 * 1. 用 Spring @Component 注册，由 AgentToolRegistry 自动收集；
 * 2. name() 全局唯一，是 LLM function name；
 * 3. description() 写给 LLM 看——写清「什么时候用、参数含义、返回什么」，
 *    描述质量直接决定工具调用准确率；
 * 4. execute() 内部必须先做 access() 权限校验（LLM 可能构造越权调用，不能信任编排层）；
 * 5. 一切写操作只返回建议文本，绝不直接落库（人审闭环）。
 */
public interface AgentTool {

    /** 工具名（LLM function name，唯一） */
    String name();

    /** 工具描述（LLM 决策依据） */
    String description();

    /** 所需最低权限 */
    ToolAccess access();

    /** 执行工具 */
    AgentToolResult execute(Map<String, Object> arguments, AgentContext context);
}
