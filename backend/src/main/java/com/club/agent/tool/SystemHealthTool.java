package com.club.agent.tool;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.club.agent.AbstractAgentTool;
import com.club.agent.AgentContext;
import com.club.agent.AgentToolResult;
import com.club.agent.ToolAccess;
import com.club.mapper.AgentToolLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 系统健康：Redis 连通性、今日登录/注册趋势、限流拦截统计。
 * 输出结构化摘要，供 LLM 汇总回答「系统怎么样」类问题。
 */
@Component
public class SystemHealthTool extends AbstractAgentTool {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;

    public SystemHealthTool(AgentToolLogMapper toolLogMapper, ObjectMapper objectMapper,
                            JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate) {
        super(toolLogMapper, objectMapper);
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String name() {
        return "system_health";
    }

    @Override
    public String description() {
        return "查看系统运行状态：Redis 是否可用、今日登录人数/注册人数、今日限流拦截次数。"
                + "适用场景：用户问「系统状态如何」「Redis 正常吗」「今天多少人登录」。无参数。";
    }

    @Override
    public ToolAccess access() {
        return ToolAccess.ADMIN;
    }

    @Tool(description = "系统健康状态：Redis 连通性、今日登录/注册趋势、限流拦截统计")
    public String systemHealth(ToolContext toolContext) {
        return bridge(toolContext, Map.of()).content();
    }

    @Override
    protected AgentToolResult doExecute(Map<String, Object> args, AgentContext ctx) {
        StringBuilder sb = new StringBuilder("【系统健康摘要】\n");

        // 1. Redis 连通性
        String redisStatus;
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            redisStatus = "PONG".equalsIgnoreCase(pong) ? "正常" : "异常(" + pong + ")";
        } catch (Exception e) {
            redisStatus = "不可用: " + e.getMessage();
        }
        sb.append("\n- Redis: ").append(redisStatus);

        // 2. 今日登录 / 注册
        Long todayLogins = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_login_log WHERE status = '0' AND DATE(login_time) = CURDATE()",
                Long.class);
        Long todayRegisters = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE DATE(create_time) = CURDATE()",
                Long.class);
        sb.append("\n- 今日登录成功: ").append(todayLogins).append(" 次，今日注册: ").append(todayRegisters).append(" 人");

        // 3. 限流拦截统计（scan 而非 KEYS：生产大键空间安全）
        long limited = 0;
        long limitedKeys = 0;
        try {
            var conn = redisTemplate.getConnectionFactory().getConnection();
            try (var cursor = conn.scan(org.springframework.data.redis.core.ScanOptions
                    .scanOptions().match(RATE_LIMIT_PREFIX + "*").count(200).build())) {
                while (cursor.hasNext()) {
                    byte[] key = cursor.next();
                    limitedKeys++;
                    String v = redisTemplate.opsForValue().get(new String(key, StandardCharsets.UTF_8));
                    if (v != null) {
                        try {
                            limited += Math.max(0, Long.parseLong(v) - 1); // INCR 首值为 1（放行），超出部分为拦截
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            conn.close();
        } catch (Exception ignored) {
            // Redis 不可用时限流统计跳过（前面已报告不可用）
        }
        sb.append("\n- 限流键数量: ").append(limitedKeys).append("，估算今日限流拦截: ").append(limited).append(" 次");

        // 4. 数据总量（供 LLM 感知系统规模）
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE deleted = 0", Long.class);
        Long clubCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM club WHERE deleted = 0", Long.class);
        sb.append("\n- 用户总数: ").append(userCount).append("，社团总数: ").append(clubCount);

        return AgentToolResult.of(sb.toString());
    }
}
