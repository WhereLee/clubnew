package com.club.agent;

/**
 * 当前请求的 Agent 上下文（ThreadLocal）。
 * 由 AgentController 在进入时设置、finally 清理；工具方法内取用。
 */
public final class AgentContextHolder {

    private static final ThreadLocal<AgentContext> HOLDER = new ThreadLocal<>();

    private AgentContextHolder() {
    }

    public static void set(AgentContext ctx) {
        HOLDER.set(ctx);
    }

    /** 取当前上下文；未设置时返回空上下文（userId=null），工具据此拒绝服务 */
    public static AgentContext get() {
        AgentContext ctx = HOLDER.get();
        return ctx != null ? ctx : new AgentContext(null, null, null, null, null, null);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
