package com.club.agent.tool;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.AgentToolResult;
import com.club.agent.ToolAccess;
import com.club.mapper.AgentToolLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 审批辅助：拉取待审批清单摘要（只读建议，不代替人做决定）。
 * - 管理员：待审批社团申请 + 全平台入社申请
 * - 社长：本社团入社申请（含申请人基础画像）
 */
@Component
public class ApprovalAssistTool extends AbstractAgentTool {

    private final JdbcTemplate jdbcTemplate;

    public ApprovalAssistTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        super(toolLogMapper, objectMapper);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String name() {
        return "pending_approval_summary";
    }

    @Override
    public String description() {
        return "查询待审批事项摘要：待审批的社团创建申请与入社申请（含申请人昵称、申请时间、申请理由/描述）。"
                + "适用场景：用户问「有什么待审批的」「帮我看看入社申请」「今天要处理哪些审批」。无参数。"
                + "注意：本工具只提供信息摘要，审批决定与执行由用户本人完成。";
    }

    @Override
    public ToolAccess access() {
        return ToolAccess.CLUB_ADMIN;
    }

    @Tool(description = "待审批事项摘要（社团申请 + 入社申请）")
    public String pendingApprovalSummary(ToolContext toolContext) {
        return bridge(toolContext, Map.of()).content();
    }

    @Override
    protected AgentToolResult doExecute(Map<String, Object> args, AgentContext ctx) {
        StringBuilder sb = new StringBuilder("【待审批摘要】\n");

        // 1. 社团创建申请（仅管理员可见）
        if (ctx.isAdmin()) {
            List<Map<String, Object>> clubs = jdbcTemplate.queryForList(
                    "SELECT c.id, c.name, c.category, u.nickname AS applicant, c.apply_time " +
                    "FROM club c LEFT JOIN sys_user u ON u.id = c.create_user_id " +
                    "WHERE c.status = 'PENDING' AND c.deleted = 0 ORDER BY c.apply_time ASC LIMIT 20");
            sb.append("\n- 待审批社团创建申请（").append(clubs.size()).append(" 个）:\n");
            if (clubs.isEmpty()) {
                sb.append("  （无）\n");
            } else {
                for (Map<String, Object> row : clubs) {
                    sb.append("  ").append(row).append("\n");
                }
            }
        }

        // 2. 入社申请（管理员看全平台，社长看本社团）
        List<Map<String, Object>> members;
        if (ctx.isAdmin()) {
            members = jdbcTemplate.queryForList(
                    "SELECT cm.id, cm.club_id, c.name AS club_name, u.nickname AS applicant, cm.apply_time " +
                    "FROM club_member cm " +
                    "LEFT JOIN club c ON c.id = cm.club_id " +
                    "LEFT JOIN sys_user u ON u.id = cm.user_id " +
                    "WHERE cm.status = 'PENDING' AND cm.deleted = 0 " +
                    "ORDER BY cm.apply_time ASC LIMIT 20");
        } else if (ctx.clubId() != null) {
            members = jdbcTemplate.queryForList(
                    "SELECT cm.id, u.nickname AS applicant, u.user_type, cm.apply_time " +
                    "FROM club_member cm LEFT JOIN sys_user u ON u.id = cm.user_id " +
                    "WHERE cm.club_id = ? AND cm.status = 'PENDING' AND cm.deleted = 0 " +
                    "ORDER BY cm.apply_time ASC LIMIT 20",
                    ctx.clubId());
        } else {
            members = List.of();
        }
        sb.append("\n- 待审批入社申请（").append(members.size()).append(" 个）:\n");
        if (members.isEmpty()) {
            sb.append("  （无）\n");
        } else {
            for (Map<String, Object> row : members) {
                sb.append("  ").append(row).append("\n");
            }
        }

        sb.append("\n请基于以上信息向用户给出客观摘要；审批动作请提醒用户自行在审批页面执行。");
        return AgentToolResult.of(sb.toString());
    }
}
