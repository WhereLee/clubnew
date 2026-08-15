package com.club.agent;

import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import com.club.common.BusinessException;
import com.club.domain.AgentToolLog;
import com.club.mapper.AgentToolLogMapper;
import com.club.metrics.ClubMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * Agent 工具基类：提供 @Tool 桥接的公共逻辑。
 *
 * 每个具体工具继承本类并实现 AgentTool 三元组（name/description/access）+ doExecute；
 * 再暴露一个 @Tool 注解方法作为 ChatClient.tools() 的入口（Spring AI 反射调用）。
 *
 * 职责：
 * 1. 权限校验（LLM 可能构造越权调用，不能信任编排层）；
 * 2. 工具调用轨迹落库（agent_tool_log：谁、何时、调了什么、耗时、成败）；
 * 3. 异常兜底（工具失败返回友好文本而非打断对话）。
 */
@Slf4j
public abstract class AbstractAgentTool implements AgentTool {

    /** ToolContext 中 AgentContext 的键（AgentServiceImpl 注入） */
    public static final String CTX_KEY = "agentContext";
    /** ToolContext 中会话 ID 的键 */
    public static final String SESSION_KEY = "sessionId";

    protected final AgentToolLogMapper toolLogMapper;
    protected final ObjectMapper objectMapper;

    /** 工具调用结果计数器（AI 健康度：成功/失败/权限拒绝） */
    @Resource
    protected ClubMetrics clubMetrics;

    protected AbstractAgentTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper) {
        this.toolLogMapper = toolLogMapper;
        this.objectMapper = objectMapper;
    }

    /** AgentTool 接口入口（显式传上下文，供非 @Tool 调用路径使用） */
    @Override
    public AgentToolResult execute(Map<String, Object> arguments, AgentContext ctx) {
        return executeInternal(arguments, ctx, null);
    }

    /** @Tool 桥接入口（具体工具的子类方法委托到这里） */
    protected AgentToolResult bridge(ToolContext toolContext, Map<String, Object> arguments) {
        return executeInternal(arguments, getContext(toolContext), getSessionId(toolContext));
    }

    /** 公共执行链：权限校验 → 业务执行 → 轨迹落库 → 异常兜底 */
    private AgentToolResult executeInternal(Map<String, Object> arguments, AgentContext ctx, Long sessionId) {
        long start = System.currentTimeMillis();

        // 1. 权限校验（最小权限原则：编排层过滤之外的第二道防线）
        String denyReason = checkAccess(ctx);
        if (denyReason != null) {
            incrToolCounter("denied");
            return AgentToolResult.of("权限不足：该工具需要「" + access() + "」权限，当前用户不具备。" + denyReason);
        }

        // 2. 执行 + 轨迹落库
        try {
            AgentToolResult result = doExecute(arguments, ctx);
            saveLog(sessionId, ctx, arguments, result, System.currentTimeMillis() - start, 1);
            incrToolCounter("success");
            return result;
        } catch (Exception e) {
            log.warn("agent 工具 {} 执行异常", name(), e);
            incrToolCounter("failure");
            AgentToolResult fail = AgentToolResult.of("工具执行失败：" + e.getMessage() + "。请向用户说明本次查询未能完成。");
            saveLog(sessionId, ctx, arguments, fail, System.currentTimeMillis() - start, 0);
            return fail;
        }
    }

    /** 工具调用结果计数（监控辅助，计数器组件不可用时静默跳过——如单元测试手动 new 场景） */
    private void incrToolCounter(String kind) {
        if (clubMetrics == null) {
            return;
        }
        switch (kind) {
            case "success" -> clubMetrics.incrAgentToolSuccess();
            case "failure" -> clubMetrics.incrAgentToolFailure();
            case "denied" -> clubMetrics.incrAgentToolDenied();
            default -> { }
        }
    }

    /** 具体工具的执行逻辑（不处理权限与轨迹，聚焦业务） */
    protected abstract AgentToolResult doExecute(Map<String, Object> arguments, AgentContext ctx);

    /** 权限校验：返回 null 表示通过，否则返回拒绝原因 */
    private String checkAccess(AgentContext ctx) {
        if (ctx == null || ctx.userId() == null) {
            return "（未登录）";
        }
        boolean allowed = switch (access()) {
            case ALL -> true;
            case CLUB_ADMIN -> ctx.isAdmin() || ctx.isClubAdmin();
            case ADMIN -> ctx.isAdmin();
        };
        return allowed ? null : "（userId=" + ctx.userId() + "）";
    }

    private AgentContext getContext(ToolContext toolContext) {
        Object raw = toolContext.getContext().get(CTX_KEY);
        return raw instanceof AgentContext ctx ? ctx : null;
    }

    private Long getSessionId(ToolContext toolContext) {
        Object raw = toolContext.getContext().get(SESSION_KEY);
        return raw instanceof Long id ? id : null;
    }

    private void saveLog(Long sessionId, AgentContext ctx, Map<String, Object> arguments,
                         AgentToolResult result, long durationMs, int status) {
        try {
            AgentToolLog log = new AgentToolLog();
            log.setSessionId(sessionId);
            log.setUserId(ctx != null ? ctx.userId() : null);
            log.setToolName(name());
            log.setArguments(objectMapper.writeValueAsString(arguments));
            String summary = result.content();
            log.setResultSummary(summary.length() > 500 ? summary.substring(0, 500) : summary);
            log.setDurationMs(durationMs);
            log.setStatus(status);
            toolLogMapper.insert(log);
        } catch (Exception e) {
            // 轨迹落库失败不影响对话主流程
        }
    }
}
