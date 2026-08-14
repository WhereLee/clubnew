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
 * 数据体检：跑预设对账检查（计数一致性、孤儿引用、社长完整性、经费余额），
 * 与 docs/测试数据规范.md 的验收规则同源——发现数据层 bug 的一线工具。
 */
@Component
public class DataHealthTool extends AbstractAgentTool {

    private final JdbcTemplate jdbcTemplate;

    public DataHealthTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        super(toolLogMapper, objectMapper);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String name() {
        return "data_health_check";
    }

    @Override
    public String description() {
        return "对数据库做一致性体检：社团成员计数 vs 明细、纳新/活动报名计数 vs 明细、经费余额累加校验、孤儿引用、社长缺失或重复。"
                + "适用场景：用户问「数据有没有问题」「帮我体检数据库」「计数对不对」。无参数。";
    }

    @Override
    public ToolAccess access() {
        return ToolAccess.ADMIN;
    }

    @Tool(description = "数据库一致性体检：计数对账/孤儿引用/社长完整性/经费余额")
    public String dataHealthCheck(ToolContext toolContext) {
        return bridge(toolContext, Map.of()).content();
    }

    @Override
    protected AgentToolResult doExecute(Map<String, Object> args, AgentContext ctx) {
        StringBuilder sb = new StringBuilder("【数据体检报告】\n");
        int issues = 0;

        // 1. 社团成员计数（子查询包裹：外层 WHERE 引用别名，H2/MySQL 双兼容）
        List<Map<String, Object>> memberMismatch = jdbcTemplate.queryForList(
                "SELECT * FROM (SELECT c.id, c.name, c.member_count AS declared_cnt, " +
                "(SELECT COUNT(*) FROM club_member cm WHERE cm.club_id = c.id AND cm.status = 'ACTIVE' AND cm.deleted = 0) AS actual_cnt " +
                "FROM club c WHERE c.deleted = 0 AND c.status = 'APPROVED') t WHERE t.declared_cnt != t.actual_cnt");
        issues += appendSection(sb, "成员计数不一致（member_count != ACTIVE 明细数）", memberMismatch);

        // 2. 纳新报名计数
        List<Map<String, Object>> recruitMismatch = jdbcTemplate.queryForList(
                "SELECT * FROM (SELECT r.id, r.title, r.applied_count AS declared_cnt, " +
                "(SELECT COUNT(*) FROM recruit_record rr WHERE rr.recruit_id = r.id " +
                " AND rr.status IN ('PENDING','PASSED') AND rr.deleted = 0) AS actual_cnt " +
                "FROM recruit r WHERE r.deleted = 0) t WHERE t.declared_cnt != t.actual_cnt");
        issues += appendSection(sb, "纳新报名计数不一致（applied_count != 有效报名数）", recruitMismatch);

        // 3. 活动报名计数
        List<Map<String, Object>> activityMismatch = jdbcTemplate.queryForList(
                "SELECT * FROM (SELECT a.id, a.title, a.applied_count AS declared_cnt, " +
                "(SELECT COUNT(*) FROM activity_signup s WHERE s.activity_id = a.id AND s.status = 'SIGNED' AND s.deleted = 0) AS actual_cnt " +
                "FROM activity a WHERE a.deleted = 0) t WHERE t.declared_cnt != t.actual_cnt");
        issues += appendSection(sb, "活动报名计数不一致（applied_count != SIGNED 报名数）", activityMismatch);

        // 4. 社长缺失/重复（APPROVED 社团必须恰好一个 ACTIVE 社长）
        List<Map<String, Object>> presidentIssues = jdbcTemplate.queryForList(
                "SELECT * FROM (SELECT c.id, c.name, " +
                "(SELECT COUNT(*) FROM club_member cm WHERE cm.club_id = c.id AND cm.member_role = 'PRESIDENT' AND cm.status = 'ACTIVE' AND cm.deleted = 0) AS president_cnt " +
                "FROM club c WHERE c.deleted = 0 AND c.status = 'APPROVED') t WHERE t.president_cnt != 1");
        issues += appendSection(sb, "社长异常（应为恰好 1 个 ACTIVE 社长）", presidentIssues);

        // 5. 孤儿引用
        List<Map<String, Object>> orphans = jdbcTemplate.queryForList(
                "SELECT 'club_member→club' AS kind, cm.id AS bad_id FROM club_member cm LEFT JOIN club c ON c.id = cm.club_id WHERE cm.deleted = 0 AND c.id IS NULL " +
                "UNION ALL " +
                "SELECT 'recruit→club', r.id FROM recruit r LEFT JOIN club c ON c.id = r.club_id WHERE r.deleted = 0 AND c.id IS NULL " +
                "UNION ALL " +
                "SELECT 'activity→club', a.id FROM activity a LEFT JOIN club c ON c.id = a.club_id WHERE a.deleted = 0 AND c.id IS NULL " +
                "LIMIT 20");
        issues += appendSection(sb, "孤儿引用（业务记录指向不存在的社团）", orphans);

        if (issues == 0) {
            sb.append("\n全部检查通过，未发现一致性问题。");
        } else {
            sb.append("\n共发现 ").append(issues).append(" 条异常，请向用户如实报告。");
        }
        return AgentToolResult.of(sb.toString());
    }

    private int appendSection(StringBuilder sb, String title, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return 0;
        }
        sb.append("\n- ").append(title).append("（").append(rows.size()).append(" 条）:\n");
        for (Map<String, Object> row : rows) {
            sb.append("  ").append(row).append("\n");
        }
        return rows.size();
    }
}
