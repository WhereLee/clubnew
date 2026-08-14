package com.club;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.tool.ApprovalAssistTool;
import com.club.agent.tool.SqlQueryTool;

/**
 * Phase C 业务端工具测试——NL2SQL 安全防线是重点。
 */
@SpringBootTest
@ActiveProfiles("test")
class AgentSqlToolTest {

    @Autowired
    private SqlQueryTool sqlQueryTool;

    @Autowired
    private ApprovalAssistTool approvalAssistTool;

    private static final AgentContext ADMIN = new AgentContext(1L, "admin", "管理员", "ADMIN", 1, null);
    /** 社长（数据范围=本社团，clubId=2001） */
    private static final AgentContext PRESIDENT = new AgentContext(1016L, "pres1001", "陈晨", "STUDENT", 3, 2001L);
    /** 学生（无社团身份） */
    private static final AgentContext STUDENT = new AgentContext(1001L, "stu1001", "林晓雨", "STUDENT", 4, null);

    private ToolContext toolContext(AgentContext ctx) {
        return new ToolContext(Map.of(AbstractAgentTool.CTX_KEY, ctx));
    }

    // ============ NL2SQL 安全防线 ============

    @Test
    void sqlTool_adminSimpleQuery_ok() {
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name, member_count FROM club WHERE status = 'APPROVED'", toolContext(ADMIN));
        assertTrue(result.contains("【查询结果】"), result);
        assertTrue(result.contains("返回"), result);
    }

    @Test
    void sqlTool_joinQuery_rejected() {
        String result = sqlQueryTool.queryBusinessData(
                "SELECT c.name FROM club c JOIN club_member m ON m.club_id = c.id", toolContext(ADMIN));
        assert result.contains("拒绝");
    }

    @Test
    void sqlTool_dangerousStatement_rejected() {
        // 多语句注入
        assertTrue(sqlQueryTool.queryBusinessData("SELECT name FROM club; DROP TABLE club", toolContext(ADMIN))
                .contains("拒绝"), "多语句注入应被拒绝");
        // 注释注入
        assertTrue(sqlQueryTool.queryBusinessData("SELECT name FROM club -- 注释", toolContext(ADMIN))
                .contains("拒绝"), "注释注入应被拒绝");
        // 危险函数
        assertTrue(sqlQueryTool.queryBusinessData("SELECT name FROM club WHERE sleep(5)", toolContext(ADMIN))
                .contains("拒绝"), "sleep 函数应被拒绝");
        // 非 SELECT
        assertTrue(sqlQueryTool.queryBusinessData("DELETE FROM club WHERE id = 1", toolContext(ADMIN))
                .contains("拒绝"), "DELETE 应被拒绝");
    }

    @Test
    void sqlTool_sensitiveColumn_rejected() {
        // sys_user.password 不在白名单
        String result = sqlQueryTool.queryBusinessData(
                "SELECT username, password FROM sys_user", toolContext(ADMIN));
        assert result.contains("拒绝");
    }

    @Test
    void sqlTool_nonWhitelistTable_rejected() {
        assert sqlQueryTool.queryBusinessData("SELECT * FROM sys_role", toolContext(ADMIN)).contains("拒绝");
    }

    @Test
    void sqlTool_president_autoClubScopeInjected() {
        // 社长查 club 表：AST 层注入 club.id = 2001 过滤——测试库种子社团 id≠2001，返回 0 行即证明过滤生效
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name, member_count FROM club WHERE status = 'APPROVED'", toolContext(PRESIDENT));
        assertTrue(result.contains("【查询结果】"), result);
        assertTrue(result.contains("返回 0 行"), "过滤应生效：非本社团数据不可见 -> " + result);
    }

    @Test
    void sqlTool_studentNoClub_rejected() {
        // 学生无社团身份（且非管理员）在权限过滤中被拦截——直接调工具验证第二道防线
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name FROM club WHERE status = 'APPROVED'", toolContext(STUDENT));
        assert result.contains("权限不足");
    }

    @Test
    void sqlTool_aggregateQuery_ok() {
        String result = sqlQueryTool.queryBusinessData(
                "SELECT status, COUNT(*) FROM recruit GROUP BY status", toolContext(ADMIN));
        assert result.contains("查询结果");
    }

    // ============ 复审发现的 HIGH 绕过路径回归 ============

    @Test
    void sqlTool_hashComment_rejected() {
        // # 注释可吞掉注入的过滤与 LIMIT
        assertTrue(sqlQueryTool.queryBusinessData("SELECT name FROM club WHERE status='APPROVED' #", toolContext(PRESIDENT))
                .contains("拒绝"), "# 注释应被拒绝");
    }

    @Test
    void sqlTool_whereSubquery_rejected() {
        // WHERE 子查询布尔盲注侧信道（可读 sys_user.password）
        assertTrue(sqlQueryTool.queryBusinessData(
                "SELECT name FROM club WHERE status='APPROVED' OR (SELECT password FROM sys_user LIMIT 1) LIKE 'a%'",
                toolContext(ADMIN)).contains("拒绝"), "WHERE 子查询应被拒绝");
    }

    @Test
    void sqlTool_presidentFilter_insertedBeforeOrderBy() {
        // 过滤在 AST 层构造（词法正确，不存在插入位置问题）；测试库 club 无 id=2001 → 0 行即过滤生效
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name, member_count FROM club WHERE status = 'APPROVED' ORDER BY name", toolContext(PRESIDENT));
        assertTrue(result.contains("【查询结果】"), result);
        assertTrue(result.contains("返回 0 行"), "过滤应生效 -> " + result);
    }

    @Test
    void sqlTool_studentWithScope4_notClubAdmin() {
        // 学生 dataScope=4（仅本人）不得误判为社团管理者：无 clubId 时应被拒绝
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name FROM club WHERE status = 'APPROVED'", toolContext(STUDENT));
        assertTrue(result.contains("权限不足"), result);
    }

    // ============ 终审（第三轮）发现的 CRITICAL 回归 ============

    @Test
    void sqlTool_functionArgColumnEscape_rejected() {
        // 函数参数列逃逸：MAX(password) 直接回显密码哈希
        assertTrue(sqlQueryTool.queryBusinessData("SELECT MAX(password) FROM sys_user", toolContext(ADMIN))
                .contains("拒绝"), "函数参数列逃逸应被拒绝");
        // GROUP_CONCAT 一次性导出（函数名不在白名单，双保险）
        assertTrue(sqlQueryTool.queryBusinessData("SELECT GROUP_CONCAT(password) FROM sys_user", toolContext(ADMIN))
                .contains("拒绝"), "GROUP_CONCAT 应被拒绝");
    }

    @Test
    void sqlTool_caseExpression_rejected() {
        // CASE WHEN 布尔 oracle / 直接回显：默认拒绝模型
        assertTrue(sqlQueryTool.queryBusinessData(
                "SELECT CASE WHEN 1=1 THEN name END FROM club", toolContext(ADMIN))
                .contains("拒绝"), "CASE 表达式应被拒绝");
    }

    @Test
    void sqlTool_presidentOrTautology_scopedByParenthesis() {
        // 优先级翻转回归：用户 WHERE "id=1 OR 1=1" 被 Parenthesis 包裹后与 scope 过滤 AND 相连，
        // 必须只返回本社团数据（测试库无 club id=2001 → 0 行）
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name FROM club WHERE id = 1 OR 1 = 1", toolContext(PRESIDENT));
        assertTrue(result.contains("返回 0 行"), "Parenthesis 包裹应保证过滤生效 -> " + result);
    }

    @Test
    void sqlTool_createTimeColumn_notFalselyRejected() {
        // 预筛精简回归：create_time 是白名单列，不应被 create 子串误伤
        String result = sqlQueryTool.queryBusinessData(
                "SELECT name, create_time FROM club WHERE status = 'APPROVED'", toolContext(ADMIN));
        assertTrue(result.contains("【查询结果】"), result);
    }

    @Test
    void sqlTool_whereColumnNotInWhitelist_rejected() {
        // WHERE 段列引用同样受白名单约束（返回行数布尔 oracle 封堵）
        assertTrue(sqlQueryTool.queryBusinessData(
                "SELECT name FROM sys_user WHERE password LIKE 'a%'", toolContext(ADMIN))
                .contains("拒绝"), "WHERE 引用白名单外列应被拒绝");
    }

    @Test
    void sqlTool_stringLiteralForge_rejectedOrScoped() {
        // 终审发现：SELECT 列表字符串字面量伪造 "WHERE 1=1" 前缀欺骗 indexOf 注入定位——
        // 注入后语义自校验必须兜底（要么拒绝，要么返回 0 行——绝不越权返回全表）
        String result = sqlQueryTool.queryBusinessData(
                "SELECT 'WHERE 1=1' AS x, name FROM club WHERE 1=1", toolContext(PRESIDENT));
        boolean rejected = result.contains("拒绝");
        boolean scopedCorrectly = result.contains("返回 0 行");
        assertTrue(rejected || scopedCorrectly,
                "字面量欺骗载荷必须被拒绝或过滤生效，实际: " + result);
    }

    // ============ 第五轮复审 CRITICAL 回归 ============

    @Test
    void sqlTool_signedFunctionBypass_rejected() {
        // SignedExpression 不递归曾导致 -SLEEP(2) 绕过函数白名单（时间盲注侧信道）
        assertTrue(sqlQueryTool.queryBusinessData(
                "SELECT name FROM club WHERE id = -SLEEP(2)", toolContext(ADMIN))
                .contains("拒绝"), "-SLEEP 函数绕过应被拒绝");
        assertTrue(sqlQueryTool.queryBusinessData(
                "SELECT -EXTRACTVALUE(1, name) FROM club", toolContext(ADMIN))
                .contains("拒绝"), "-EXTRACTVALUE 应被拒绝");
    }

    @Test
    void sqlTool_cte_rejected() {
        assertTrue(sqlQueryTool.queryBusinessData(
                "WITH c AS (SELECT * FROM sys_user) SELECT name FROM club", toolContext(ADMIN))
                .contains("拒绝"), "CTE 应被拒绝");
    }

    @Test
    void sqlTool_forUpdate_rejected() {
        assertTrue(sqlQueryTool.queryBusinessData(
                "SELECT name FROM club FOR UPDATE", toolContext(ADMIN))
                .contains("拒绝"), "FOR UPDATE 应被拒绝");
    }

    // ============ 审批辅助 ============

    @Test
    void approvalTool_admin_returnsSummary() {
        String result = approvalAssistTool.pendingApprovalSummary(toolContext(ADMIN));
        assert result.contains("待审批摘要");
        assert result.contains("入社申请");
    }

    @Test
    void approvalTool_president_ownClubOnly() {
        String result = approvalAssistTool.pendingApprovalSummary(toolContext(PRESIDENT));
        assert result.contains("待审批摘要");
        // 社长看不到全平台社团申请段
        assert !result.contains("待审批社团创建申请");
    }
}
