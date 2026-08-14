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
    void dataHealthTool_seedData_allPass() {
        // V1/V2 种子数据是自洽的：体检应全部通过
        String result = dataHealthTool.dataHealthCheck(toolContext(ADMIN));
        assertTrue(result.contains("全部检查通过"), result);
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
