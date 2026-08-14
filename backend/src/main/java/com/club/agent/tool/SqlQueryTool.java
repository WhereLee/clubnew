package com.club.agent.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * 受限 NL2SQL：LLM 生成单表 SELECT → AST 级防护 → 执行 → 结果摘要回传。
 *
 * 安全模型（基于 jsqlparser 真实 SQL 解析，而非正则——正则对 SQL 词法必然存在绕过）：
 * 1. 解析层：解析失败/多语句直接拒绝；注释（--/#）被解析器丢弃，天然无法吞掉注入逻辑；
 * 2. 结构层：仅允许 PlainSelect + 单个 Table（无 JOIN/逗号多表）；WHERE/HAVING/ORDER BY/GROUP BY
 *    全树遍历禁止子查询（布尔盲注侧信道载体）；SELECT 列必须在表白名单内，函数仅放行
 *    COUNT/SUM/AVG/MAX/MIN（同时封死 EXTRACTVALUE 等报错注入函数）；
 * 3. 数据权限层：非管理员在 AST 上构造 AND 注入 club_id 过滤（词法正确，不存在插入位置被字面量欺骗）；
 * 4. 资源层：LIMIT 强制 ≤100（AST 操作）；查询超时 3 秒；执行错误只回传通用文案（细节入日志）。
 */
@Slf4j
@Component
public class SqlQueryTool extends AbstractAgentTool {

    private static final int MAX_ROWS = 100;
    private static final int QUERY_TIMEOUT_SECONDS = 3;

    /** 表白名单 → 允许查询的列 */
    private static final Map<String, Set<String>> TABLE_WHITELIST = new LinkedHashMap<>();

    static {
        TABLE_WHITELIST.put("club", Set.of("id", "name", "code", "category", "status", "member_count",
                "star_level", "create_user_id", "president_id", "apply_time", "audit_time", "create_time"));
        TABLE_WHITELIST.put("club_member", Set.of("id", "club_id", "user_id", "member_role", "status",
                "apply_time", "join_time", "create_time"));
        TABLE_WHITELIST.put("recruit", Set.of("id", "club_id", "title", "quota", "applied_count",
                "start_time", "end_time", "status", "create_time"));
        TABLE_WHITELIST.put("recruit_record", Set.of("id", "recruit_id", "user_id", "status",
                "apply_time", "interview_time", "create_time"));
        TABLE_WHITELIST.put("activity", Set.of("id", "club_id", "title", "quota", "applied_count",
                "start_time", "end_time", "status", "checkin_enabled", "create_time"));
        TABLE_WHITELIST.put("activity_signup", Set.of("id", "activity_id", "user_id", "status",
                "signup_time", "create_time"));
        TABLE_WHITELIST.put("activity_checkin", Set.of("id", "activity_id", "user_id", "status",
                "checkin_time", "create_time"));
        TABLE_WHITELIST.put("notice", Set.of("id", "club_id", "title", "top", "status",
                "publish_time", "create_time"));
        TABLE_WHITELIST.put("post", Set.of("id", "club_id", "author_id", "content", "like_count",
                "comment_count", "create_time"));
        TABLE_WHITELIST.put("comment", Set.of("id", "biz_type", "biz_id", "user_id", "content", "create_time"));
        TABLE_WHITELIST.put("user_like", Set.of("id", "biz_type", "biz_id", "user_id", "status", "create_time"));
        TABLE_WHITELIST.put("fund", Set.of("id", "club_id", "title", "amount", "type", "status",
                "apply_user_id", "create_time"));
        TABLE_WHITELIST.put("fund_record", Set.of("id", "club_id", "fund_id", "amount", "type",
                "balance_after", "create_time"));
        TABLE_WHITELIST.put("sys_user", Set.of("id", "username", "nickname", "user_type", "status", "create_time"));
    }

    /** 数据范围列：非管理员查询时自动注入过滤（club 表自身即社团实体，范围列是 id） */
    private static final Map<String, String> CLUB_SCOPE_COLUMN = Map.ofEntries(
            Map.entry("club", "id"),
            Map.entry("club_member", "club_id"),
            Map.entry("recruit", "club_id"),
            Map.entry("recruit_record", "club_id"),
            Map.entry("activity", "club_id"),
            Map.entry("activity_signup", "club_id"),
            Map.entry("activity_checkin", "club_id"),
            Map.entry("notice", "club_id"),
            Map.entry("post", "club_id"),
            Map.entry("comment", "club_id"),
            Map.entry("user_like", "club_id"),
            Map.entry("fund", "club_id"),
            Map.entry("fund_record", "club_id"));

    /** 聚合函数白名单（封死 EXTRACTVALUE/CONCAT 等报错注入与信息回显函数） */
    private static final Set<String> FUNCTION_WHITELIST = Set.of("count", "sum", "avg", "max", "min");

    /** 预筛正则：仅拦截 jsqlparser 会静默截断/丢弃的多语句与注释（其余全部交给 AST 结构校验） */
    private static final java.util.regex.Pattern DANGEROUS_PRESCAN = java.util.regex.Pattern.compile(
            "(?is);|--|#|/\\*|\\*/");

    private final JdbcTemplate jdbcTemplate;

    public SqlQueryTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        super(toolLogMapper, objectMapper);
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String name() {
        return "query_business_data";
    }

    @Override
    public String description() {
        return "用 SQL 查询业务数据（受限单表 SELECT，禁止多表 JOIN 与子查询）。"
                + "适用场景：用户问「摄影社有多少成员」「本社团活动报名率」「最近 7 天新增动态数」等运营数据问题。"
                + "规则：1) 只能查一张表；2) 可用表及列见下："
                + "club(id,name,code,category,status,member_count,star_level,president_id,create_time)、"
                + "club_member(id,club_id,user_id,member_role,status,join_time)、"
                + "recruit(id,club_id,title,quota,applied_count,start_time,end_time,status)、"
                + "recruit_record(id,recruit_id,user_id,status,apply_time,interview_time)、"
                + "activity(id,club_id,title,quota,applied_count,start_time,end_time,status,checkin_enabled)、"
                + "activity_signup(id,activity_id,user_id,status,signup_time)、"
                + "activity_checkin(id,activity_id,user_id,checkin_time)、"
                + "notice(id,club_id,title,top,status,publish_time)、"
                + "post(id,club_id,author_id,content,like_count,comment_count,create_time)、"
                + "comment(id,biz_type,biz_id,user_id,content)、"
                + "user_like(id,biz_type,biz_id,user_id)、"
                + "fund(id,club_id,title,amount,type,status)、"
                + "fund_record(id,club_id,fund_id,amount,type,balance_after)、"
                + "sys_user(id,username,nickname,user_type,status)；"
                + "3) 可用聚合函数 COUNT/SUM/AVG/MAX/MIN 与 GROUP BY，系统自动追加 LIMIT 100；"
                + "4) 返回行数上限 100，超出截断。参数 sql：完整 SQL 语句文本。";
    }

    @Override
    public ToolAccess access() {
        return ToolAccess.CLUB_ADMIN;
    }

    @Tool(description = "受限单表 SELECT 查询业务数据（自动应用数据权限与行数上限）")
    public String queryBusinessData(
            @ToolParam(description = "完整的单表 SELECT SQL 语句") String sql,
            ToolContext toolContext) {
        return bridge(toolContext, Map.of("sql", sql)).content();
    }

    @Override
    protected AgentToolResult doExecute(Map<String, Object> args, AgentContext ctx) {
        String rawSql = String.valueOf(args.getOrDefault("sql", "")).trim();
        if (rawSql.isBlank()) {
            return AgentToolResult.of("SQL 被安全策略拒绝：语句为空。");
        }
        // 0. 预筛（多语句/注释/危险模式；jsqlparser 对分号后内容静默截断，必须在此拦截）
        if (DANGEROUS_PRESCAN.matcher(rawSql).find()) {
            return AgentToolResult.of("SQL 被安全策略拒绝：包含多语句/注释/写操作/危险函数。");
        }

        // ---- 1. 解析层（jsqlparser：注释被丢弃、多语句/语法错误直接拒绝） ----
        PlainSelect ps;
        try {
            Statement stmt = CCJSqlParserUtil.parse(rawSql);
            if (!(stmt instanceof Select select) || !(select.getSelectBody() instanceof PlainSelect)) {
                return AgentToolResult.of("SQL 被安全策略拒绝：仅支持单条 SELECT 查询。");
            }
            ps = (PlainSelect) select.getSelectBody();
        } catch (Exception e) {
            return AgentToolResult.of("SQL 被安全策略拒绝：语句解析失败或包含多语句/注释。");
        }

        // ---- 2. 结构层 ----
        String reject = validateStructure(ps);
        if (reject != null) {
            return AgentToolResult.of(reject);
        }

        // ---- 3. 数据权限层 ----
        String tableName = ((Table) ps.getFromItem()).getName().toLowerCase();
        String scopeColumn = CLUB_SCOPE_COLUMN.get(tableName);
        if (scopeColumn != null && !ctx.isAdmin()) {
            if (ctx.clubId() == null) {
                return AgentToolResult.of("权限不足：当前用户无社团身份，不能查询社团运营数据。");
            }
            String scopeText = tableName + "." + scopeColumn + " = " + ctx.clubId();
            if (ps.getWhere() != null) {
                // 受控字符串重建 + 重建后语义级自校验：
                // 1) ps.toString() 是规范化输出；2) 重建解析后校验顶层 AND 结构与 scope 谓词逐字段一致；
                // 3) 重跑 validateStructure——三重校验封死「规范化文本构造攻击」（如 SELECT 列表字面量伪造 WHERE 前缀）
                String canonical = ps.toString();
                String whereText = ps.getWhere().toString();
                String marker = "WHERE " + whereText;
                int idx = canonical.indexOf(marker);
                if (idx < 0) {
                    return AgentToolResult.of("SQL 被安全策略拒绝：无法安全注入数据权限过滤。");
                }
                String scoped = canonical.substring(0, idx)
                        + "WHERE (" + whereText + ") AND " + scopeText
                        + canonical.substring(idx + marker.length());
                PlainSelect scopedPs;
                try {
                    scopedPs = (PlainSelect) ((Select) CCJSqlParserUtil.parse(scoped)).getSelectBody();
                } catch (Exception e) {
                    return AgentToolResult.of("SQL 被安全策略拒绝：数据权限注入后语句无效。");
                }
                String scopeCheck = verifyScopeInjection(scopedPs, tableName, scopeColumn, ctx.clubId(), ps.getWhere());
                if (scopeCheck != null) {
                    return AgentToolResult.of(scopeCheck);
                }
                String recheck = validateStructure(scopedPs);
                if (recheck != null) {
                    return AgentToolResult.of(recheck);
                }
                ps = scopedPs;
            } else {
                ps.setWhere(new EqualsTo(
                        new Column(new Table(tableName), scopeColumn), new LongValue(ctx.clubId())));
            }
        }

        // ---- 4. 资源层：LIMIT 强制（AST 操作，无字符串位置问题） ----
        enforceLimit(ps);

        String finalSql = ps.toString();
        try {
            long start = System.currentTimeMillis();
            final List<Map<String, Object>> rows = new java.util.ArrayList<>();
            jdbcTemplate.query(con -> {
                java.sql.PreparedStatement prepared = con.prepareStatement(finalSql);
                prepared.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                return prepared;
            }, rs -> {
                java.sql.ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            });
            long elapsed = System.currentTimeMillis() - start;

            StringBuilder sb = new StringBuilder("【查询结果】\n");
            sb.append("返回 ").append(rows.size()).append(" 行（上限 ").append(MAX_ROWS)
              .append("），耗时 ").append(elapsed).append("ms\n");
            if (rows.isEmpty()) {
                sb.append("（无匹配数据）\n");
            } else {
                for (Map<String, Object> row : rows) {
                    sb.append(row).append("\n");
                }
                if (rows.size() == MAX_ROWS) {
                    sb.append("（已到上限，结果可能截断，建议加过滤条件）\n");
                }
            }
            return AgentToolResult.of(sb.toString());
        } catch (Exception e) {
            // 不回显内部细节（防信息泄露），细节只进日志
            log.warn("agent NL2SQL 执行失败: sql={}, err={}", finalSql, e.getMessage());
            return AgentToolResult.of("SQL 执行失败，请修正语句后重试（仅支持单表 SELECT）。");
        }
    }

    /** 注入后语义自校验：顶层 WHERE 必须是 AndExpression，右支严格等于 scope 谓词，左支等于原 WHERE。
     *  任何结构不符（含 indexOf 被字面量欺骗导致的错误注入位置）→ 拒绝。 */
    private String verifyScopeInjection(PlainSelect scopedPs, String tableName, String scopeColumn,
                                        Long clubId, Expression originalWhere) {
        Expression w = scopedPs.getWhere();
        if (!(w instanceof AndExpression and)) {
            return "SQL 被安全策略拒绝：数据权限注入结构校验失败（顶层非 AND）。";
        }
        Expression right = and.getRightExpression();
        if (!(right instanceof EqualsTo eq)) {
            return "SQL 被安全策略拒绝：数据权限注入结构校验失败（右支非等值）。";
        }
        if (!(eq.getLeftExpression() instanceof Column col)) {
            return "SQL 被安全策略拒绝：数据权限注入结构校验失败（左支非列）。";
        }
        if (col.getTable() == null
                || !tableName.equalsIgnoreCase(col.getTable().getName())
                || !scopeColumn.equalsIgnoreCase(col.getColumnName())) {
            return "SQL 被安全策略拒绝：数据权限注入结构校验失败（列不符）。";
        }
        if (!(eq.getRightExpression() instanceof LongValue lv) || lv.getValue() != clubId) {
            return "SQL 被安全策略拒绝：数据权限注入结构校验失败（值不符）。";
        }
        // 左支必须等于原 WHERE（5.1 将 "(expr)" 解析为 ParenthesedExpressionList，剥离后按规范化文本比较）
        Expression left = and.getLeftExpression();
        if (left instanceof net.sf.jsqlparser.expression.Parenthesis paren) {
            left = paren.getExpression();
        } else if (left instanceof net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList<?> pel
                && !pel.isEmpty()) {
            left = pel.get(0);
        }
        if (!left.toString().equals(originalWhere.toString())) {
            return "SQL 被安全策略拒绝：数据权限注入结构校验失败（原 WHERE 被篡改：expected=["
                    + originalWhere + "] actual=[" + left + "] type=" + left.getClass().getSimpleName() + "）。";
        }
        return null;
    }

    /** 结构校验：单表、无 JOIN/子查询/CTE、列白名单、函数白名单。返回 null=通过 */
    private String validateStructure(PlainSelect ps) {
        // CTE（WITH 子句）与行锁/INTO：一律拒绝
        if (ps.getWithItemsList() != null && !ps.getWithItemsList().isEmpty()) {
            return "SQL 被安全策略拒绝：不支持 WITH（CTE）子句。";
        }
        if (ps.getForUpdateTable() != null || ps.getForMode() != null) {
            return "SQL 被安全策略拒绝：不支持 FOR UPDATE / LOCK IN SHARE MODE。";
        }
        if (ps.getIntoTables() != null && !ps.getIntoTables().isEmpty()) {
            return "SQL 被安全策略拒绝：不支持 SELECT INTO。";
        }
        // 单表：无 JOIN、无逗号多表
        if (ps.getJoins() != null && !ps.getJoins().isEmpty()) {
            return "SQL 被安全策略拒绝：不允许 JOIN/多表查询。";
        }
        FromItem fromItem = ps.getFromItem();
        if (!(fromItem instanceof Table table) || table.getAlias() != null) {
            return "SQL 被安全策略拒绝：仅支持无别名单表查询。";
        }
        if (table.getSchemaName() != null && !table.getSchemaName().isBlank()) {
            return "SQL 被安全策略拒绝：不支持跨库表引用（schemaName）。";
        }
        String tableName = table.getName().toLowerCase();
        if (!TABLE_WHITELIST.containsKey(tableName)) {
            return "SQL 被安全策略拒绝：表 " + tableName + " 不在白名单。";
        }

        // 全树子查询禁令（WHERE/HAVING/ORDER BY/GROUP BY/SELECT 项）
        SubQueryDetector detector = new SubQueryDetector();
        acceptAll(ps, detector);
        if (detector.found) {
            return "SQL 被安全策略拒绝：不允许子查询（防布尔盲注侧信道）。";
        }

        // 表达式递归白名单：SELECT 项 + WHERE/HAVING/ORDER BY/GROUP BY 全子句统一校验
        // （默认拒绝模型：只允许 白名单列/字面量/白名单函数/算术比较逻辑操作符，其余一律拒绝）
        Set<String> allowedCols = TABLE_WHITELIST.get(tableName);
        for (SelectItem<?> item : ps.getSelectItems()) {
            if (item.getExpression() instanceof AllColumns) {
                continue; // SELECT * 放行（白名单表结构即白名单列集）
            }
            String colReject = checkExpression(item.getExpression(), allowedCols, tableName);
            if (colReject != null) {
                return colReject;
            }
        }
        if (ps.getWhere() != null) {
            String r = checkExpression(ps.getWhere(), allowedCols, tableName);
            if (r != null) {
                return r;
            }
        }
        if (ps.getHaving() != null) {
            String r = checkExpression(ps.getHaving(), allowedCols, tableName);
            if (r != null) {
                return r;
            }
        }
        if (ps.getOrderByElements() != null) {
            for (var ob : ps.getOrderByElements()) {
                String r = checkExpression(ob.getExpression(), allowedCols, tableName);
                if (r != null) {
                    return r;
                }
            }
        }
        if (ps.getGroupBy() != null) {
            for (var gb : ps.getGroupBy().getGroupByExpressionList()) {
                String r = checkExpression((Expression) gb, allowedCols, tableName);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /** 递归表达式白名单校验（默认拒绝）：返回 null=通过，否则拒绝原因 */
    private String checkExpression(Expression expr, Set<String> allowedCols, String tableName) {
        if (expr == null) {
            return null;
        }
        // SELECT * 与 COUNT(*) 的星号参数
        if (expr instanceof AllColumns) {
            return null;
        }
        // 白名单列
        if (expr instanceof Column col) {
            String colName = col.getColumnName().toLowerCase();
            return allowedCols.contains(colName)
                    ? null : "SQL 被安全策略拒绝：列 " + colName + " 不在 " + tableName + " 表白名单内。";
        }
        // 字面量
        if (expr instanceof net.sf.jsqlparser.expression.StringValue
                || expr instanceof LongValue
                || expr instanceof net.sf.jsqlparser.expression.DoubleValue
                || expr instanceof net.sf.jsqlparser.expression.NullValue) {
            return null;
        }
        // 正负号表达式：递归校验内部（-SLEEP(2) 型函数白名单绕过封堵）
        if (expr instanceof net.sf.jsqlparser.expression.SignedExpression signed) {
            return checkExpression(signed.getExpression(), allowedCols, tableName);
        }
        // 函数：白名单 + 参数递归（防止 MAX(password) 型逃逸）
        if (expr instanceof Function fn) {
            if (!FUNCTION_WHITELIST.contains(fn.getName().toLowerCase())) {
                return "SQL 被安全策略拒绝：函数 " + fn.getName() + " 不在白名单（仅 COUNT/SUM/AVG/MAX/MIN）。";
            }
            if (fn.getParameters() != null) {
                for (Expression arg : fn.getParameters()) {
                    String r = checkExpression(arg, allowedCols, tableName);
                    if (r != null) {
                        return r;
                    }
                }
            }
            return null;
        }
        // 算术/比较/逻辑二元操作符：递归两侧
        if (expr instanceof net.sf.jsqlparser.expression.BinaryExpression bin) {
            String l = checkExpression(bin.getLeftExpression(), allowedCols, tableName);
            return l != null ? l : checkExpression(bin.getRightExpression(), allowedCols, tableName);
        }
        // 一元/常用操作符：递归子表达式
        if (expr instanceof net.sf.jsqlparser.expression.NotExpression not) {
            return checkExpression(not.getExpression(), allowedCols, tableName);
        }
        if (expr instanceof net.sf.jsqlparser.expression.Parenthesis paren) {
            return checkExpression(paren.getExpression(), allowedCols, tableName);
        }
        // 5.1 将 "(expr)" 解析为 ParenthesedExpressionList：递归校验每个元素
        if (expr instanceof net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList<?> pel) {
            for (Expression e : pel) {
                String r = checkExpression(e, allowedCols, tableName);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }
        if (expr instanceof net.sf.jsqlparser.expression.operators.relational.IsNullExpression isNull) {
            return checkExpression(isNull.getLeftExpression(), allowedCols, tableName);
        }
        if (expr instanceof net.sf.jsqlparser.expression.operators.relational.InExpression inExpr) {
            String l = checkExpression(inExpr.getLeftExpression(), allowedCols, tableName);
            if (l != null) {
                return l;
            }
            // 右支必须是表达式列表（子查询已被子查询检测拦截；此处显式拒绝其他形态）
            if (!(inExpr.getRightExpression() instanceof net.sf.jsqlparser.expression.operators.relational.ExpressionList<?> list)) {
                return "SQL 被安全策略拒绝：IN 右支仅支持字面量列表。";
            }
            for (Expression item : list) {
                String r = checkExpression(item, allowedCols, tableName);
                if (r != null) {
                    return r;
                }
            }
            return null;
        }
        if (expr instanceof net.sf.jsqlparser.expression.operators.relational.Between between) {
            String l = checkExpression(between.getLeftExpression(), allowedCols, tableName);
            if (l != null) {
                return l;
            }
            String s = checkExpression(between.getBetweenExpressionStart(), allowedCols, tableName);
            return s != null ? s : checkExpression(between.getBetweenExpressionEnd(), allowedCols, tableName);
        }
        // 默认拒绝（CaseExpression/WhenClause/子查询等一切未列举类型）
        return "SQL 被安全策略拒绝：不支持的表达式类型 " + expr.getClass().getSimpleName() + "。";
    }

    /** 全树遍历子查询检测（5.1：子查询表达式 ParenthesedSelect 的 accept 分发到 visit(Select)——拦截 Select 即拦截一切子查询） */
    private static class SubQueryDetector extends ExpressionVisitorAdapter<Object> {
        boolean found = false;

        @Override
        public Object visit(Select select, Object context) {
            found = true;
            return null;
        }

        @Override
        public Object visit(ParenthesedSelect select, Object context) {
            found = true;
            return null;
        }
    }

    private void acceptAll(PlainSelect ps, SubQueryDetector detector) {
        try {
            if (ps.getWhere() != null) {
                ps.getWhere().accept(detector);
            }
            if (ps.getHaving() != null) {
                ps.getHaving().accept(detector);
            }
            if (ps.getOrderByElements() != null) {
                for (var ob : ps.getOrderByElements()) {
                    ob.getExpression().accept(detector);
                }
            }
            if (ps.getGroupBy() != null) {
                for (var gb : ps.getGroupBy().getGroupByExpressionList()) {
                    ((Expression) gb).accept(detector);
                }
            }
            for (SelectItem<?> item : ps.getSelectItems()) {
                item.getExpression().accept(detector);
            }
        } catch (Exception ignored) {
            // 遍历异常：后续 checkExpression 的默认拒绝分支兜底（fail-closed），此处容错不会放行任何危险结构
        }
    }

    /** LIMIT 强制：无 → 加 100；超过 → 替换为 100 */
    private void enforceLimit(PlainSelect ps) {
        Limit limit = ps.getLimit();
        if (limit == null || limit.getRowCount() == null) {
            ps.setLimit(new Limit().withRowCount(new LongValue(MAX_ROWS)));
            return;
        }
        Expression rowCount = limit.getRowCount();
        long value = -1;
        if (rowCount instanceof LongValue lv) {
            value = lv.getValue();
        } else {
            try {
                value = Long.parseLong(rowCount.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        if (value < 0 || value > MAX_ROWS) {
            ps.setLimit(new Limit().withRowCount(new LongValue(MAX_ROWS)));
        }
    }
}
