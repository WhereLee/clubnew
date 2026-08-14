package com.club.agent.tool;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.AgentToolResult;
import com.club.agent.ToolAccess;
import com.club.mapper.AgentToolLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 今日异常分析：聚合操作日志失败与登录失败 TOP，输出摘要供 LLM 解读。
 * 只喂聚合摘要不喂原始日志（token 成本 + 敏感信息双控）。
 */
@Component
public class TodayErrorTool extends AbstractAgentTool {

    private final JdbcTemplate jdbcTemplate;

    public TodayErrorTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        super(toolLogMapper, objectMapper);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String name() {
        return "analyze_today_errors";
    }

    @Override
    public String description() {
        return "查询今日系统异常摘要：操作日志失败 TOP10 与登录失败 TOP10（按操作人/登录名聚合）。"
                + "适用场景：用户问「今天有什么报错」「系统异常吗」「谁登录失败最多」。参数 type 取值：oper/login/all（默认 all）。";
    }

    @Override
    public ToolAccess access() {
        return ToolAccess.ADMIN;
    }

    @Tool(description = "查询今日系统异常摘要：操作日志失败与登录失败 TOP 聚合")
    public String analyzeTodayErrors(
            @ToolParam(description = "日志类型：oper（操作日志）/login（登录日志）/all（全部，默认）") String type,
            ToolContext toolContext) {
        return bridge(toolContext, Map.of("type", type)).content();
    }

    @Override
    protected AgentToolResult doExecute(Map<String, Object> args, AgentContext ctx) {
        String type = String.valueOf(args.getOrDefault("type", "all"));
        StringBuilder sb = new StringBuilder("【今日异常摘要】\n");
        boolean includeOper = !"login".equals(type);
        boolean includeLogin = !"oper".equals(type);

        if (includeOper) {
            sb.append("\n- 操作失败 TOP10（按操作人聚合）:\n");
            List<Map<String, Object>> oper = jdbcTemplate.queryForList(
                    "SELECT oper_name, COUNT(*) AS cnt FROM sys_oper_log " +
                    "WHERE status = 1 AND DATE(oper_time) = CURDATE() " +
                    "GROUP BY oper_name ORDER BY cnt DESC LIMIT 10");
            if (oper.isEmpty()) {
                sb.append("  （今日无操作失败记录）\n");
            } else {
                for (Map<String, Object> row : oper) {
                    sb.append("  ").append(row.get("oper_name")).append(": ")
                      .append(row.get("cnt")).append(" 次\n");
                }
            }
        }

        if (includeLogin) {
            sb.append("\n- 登录失败 TOP10（按用户名+IP 聚合）:\n");
            List<Map<String, Object>> login = jdbcTemplate.queryForList(
                    "SELECT user_name, ipaddr, COUNT(*) AS cnt FROM sys_login_log " +
                    "WHERE status = '1' AND DATE(login_time) = CURDATE() " +
                    "GROUP BY user_name, ipaddr ORDER BY cnt DESC LIMIT 10");
            if (login.isEmpty()) {
                sb.append("  （今日无登录失败记录）\n");
            } else {
                for (Map<String, Object> row : login) {
                    sb.append("  ").append(row.get("user_name")).append(" @ ")
                      .append(row.get("ipaddr")).append(": ").append(row.get("cnt")).append(" 次\n");
                }
            }
        }
        return AgentToolResult.of(sb.toString());
    }
}
