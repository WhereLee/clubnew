package com.club;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.tool.DataHealthTool;
import com.club.agent.tool.SystemHealthTool;
import com.club.agent.tool.TodayErrorTool;

/**
 * Phase B 技术端工具测试（真实 Spring 上下文 + H2 种子数据）。
 * 覆盖：正常执行、权限拒绝、Redis 不可用降级。
 */
@SpringBootTest
@ActiveProfiles("test")
class AgentToolTest {

    @Autowired
    private TodayErrorTool todayErrorTool;

    @Autowired
    private DataHealthTool dataHealthTool;

    @Autowired
    private SystemHealthTool systemHealthTool;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final AgentContext ADMIN = new AgentContext(1L, "admin", "管理员", "ADMIN", 1, null);
    private static final AgentContext STUDENT = new AgentContext(1001L, "stu1001", "学生", "STUDENT", 4, null);

    private ToolContext toolContext(AgentContext ctx) {
        return new ToolContext(Map.of(AbstractAgentTool.CTX_KEY, ctx, AbstractAgentTool.SESSION_KEY, 1L));
    }

    @Test
    void todayErrorTool_admin_returnsSummary() {
        String result = todayErrorTool.analyzeTodayErrors("all", toolContext(ADMIN));
        assert result.contains("今日异常摘要");
    }

    @Test
    void dataHealthTool_detectsInconsistency_andPassesWhenClean() {
        // 检测能力验证（与全局库状态解耦——不依赖其他测试类的执行顺序）：
        // 1) 定向插入无社长脏社团 → 体检必须发现「社长异常」
        jdbcTemplate.update("INSERT INTO club (id, name, code, status, member_count, star_level, create_time, update_time, deleted) " +
                "VALUES (9901, '体检无社长社团', 'C9901', 'APPROVED', 0, 0, NOW(), NOW(), 0)");
        String dirty = dataHealthTool.dataHealthCheck(toolContext(ADMIN));
        assertTrue(dirty.contains("社长异常"), "无社长脏数据应被发现 -> " + dirty);

        // 2) 定向插入计数不一致社团（member_count=5 但只有 1 个 ACTIVE 社长）→ 体检必须发现「成员计数不一致」
        jdbcTemplate.update("UPDATE club SET member_count = 5 WHERE id = 9901");
        String mismatch = dataHealthTool.dataHealthCheck(toolContext(ADMIN));
        assertTrue(mismatch.contains("成员计数不一致"), "计数不一致应被发现 -> " + mismatch);

        // 3) 清理定向脏数据 → 体检全部通过（其余共享数据均已自洽：种子 + ConcurrentDuplicateTest 修复后）
        jdbcTemplate.update("DELETE FROM club WHERE id = 9901");
        String clean = dataHealthTool.dataHealthCheck(toolContext(ADMIN));
        assertTrue(clean.contains("全部检查通过"), "清理后应全部通过 -> " + clean);
    }

    @Test
    void dataHealthTool_reportHasAllSections() {
        String result = dataHealthTool.dataHealthCheck(toolContext(ADMIN));
        // 报告结构完整：五组检查节标题齐全
        assertTrue(result.contains("数据体检报告"), result);
        assertTrue(result.contains("成员计数") || result.contains("全部检查通过"), result);
        assertTrue(result.contains("纳新报名计数") || result.contains("全部检查通过"), result);
        assertTrue(result.contains("活动报名计数") || result.contains("全部检查通过"), result);
        assertTrue(result.contains("社长异常") || result.contains("全部检查通过"), result);
        assertTrue(result.contains("孤儿引用") || result.contains("全部检查通过"), result);
    }

    @Test
    void systemHealthTool_redisUnavailable_degradesGracefully() {
        // 测试环境无 Redis：应返回不可用而非抛异常
        String result = systemHealthTool.systemHealth(toolContext(ADMIN));
        assert result.contains("系统健康摘要");
        assert result.contains("Redis");
    }

    @Test
    void tools_denyNonAdmin() {
        // 学生调用技术端工具：权限拒绝（不抛异常，返回友好文本）
        String errResult = todayErrorTool.analyzeTodayErrors("all", toolContext(STUDENT));
        assert errResult.contains("权限不足");

        String healthResult = dataHealthTool.dataHealthCheck(toolContext(STUDENT));
        assert healthResult.contains("权限不足");
    }
}
