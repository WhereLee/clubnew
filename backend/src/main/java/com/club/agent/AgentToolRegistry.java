package com.club.agent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * 工具注册表：Spring 容器启动时自动收集所有 AgentTool 实现。
 * 各端（用户端/业务端/技术端）的工具类只要声明为 @Component 即自动注册。
 */
@Component
public class AgentToolRegistry {

    private final Map<String, AgentTool> tools = new LinkedHashMap<>();

    public AgentToolRegistry(List<AgentTool> toolList) {
        for (AgentTool tool : toolList) {
            if (tools.putIfAbsent(tool.name(), tool) != null) {
                throw new IllegalStateException("Agent 工具重名: " + tool.name());
            }
        }
    }

    /** 全部工具（注册顺序稳定） */
    public List<AgentTool> allTools() {
        return List.copyOf(tools.values());
    }

    /** 按当前用户权限过滤后的可用工具列表 */
    public List<AgentTool> toolsFor(AgentContext ctx) {
        return tools.values().stream()
                .filter(t -> allowed(t.access(), ctx))
                .collect(Collectors.toList());
    }

    /** 按名取工具（不存在返回 null） */
    public AgentTool get(String name) {
        return tools.get(name);
    }

    /** 供 system prompt 使用的工具清单（name + description） */
    public String describeFor(AgentContext ctx) {
        return toolsFor(ctx).stream()
                .map(t -> "- " + t.name() + ": " + t.description())
                .collect(Collectors.joining("\n"));
    }

    private boolean allowed(ToolAccess access, AgentContext ctx) {
        return switch (access) {
            case ALL -> true;
            case CLUB_ADMIN -> ctx.isAdmin() || ctx.isClubAdmin();
            case ADMIN -> ctx.isAdmin();
        };
    }
}
