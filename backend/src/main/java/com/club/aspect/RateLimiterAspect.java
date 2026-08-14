package com.club.aspect;

import com.club.annotation.RateLimiter;
import com.club.common.BusinessException;
import com.club.metrics.ClubMetrics;
import com.club.security.SecurityUtils;
import jakarta.annotation.Resource;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 接口限流切面 - key 包含用户ID，per-user 限流。
 * <p>
 * 计数与过期时间通过 <b>Lua 脚本原子执行</b>（INCR + EXPIRE 一次完成），
 * 避免「INCR 成功后进程崩溃 → EXPIRE 未执行 → key 永不过期 → 用户被永久限流」的经典故障。
 * Redis 不可用时放行（fail-open）：限流是柔性保护，业务侧另有防超卖等硬防线兜底。
 */
@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /** Lua：计数 +1，首次计数时设置过期（原子） */
    private static final DefaultRedisScript<Long> LIMIT_SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('INCR', KEYS[1]) " +
            "if c == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
            "return c",
            Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ClubMetrics metrics;

    @Before("@annotation(rateLimiter)")
    public void before(JoinPoint point, RateLimiter rateLimiter) {
        Long userId = SecurityUtils.getUserId();
        // 未认证场景（如 /auth/refresh）按客户端 IP 分片，避免单个匿名请求刷掉全员额度；
        // 反代（nginx）场景优先取 X-Forwarded-For 首值
        String userSuffix;
        if (userId != null) {
            userSuffix = ":" + userId;
        } else {
            userSuffix = ":ip:" + resolveClientIp();
        }
        String key = RATE_LIMIT_PREFIX + rateLimiter.key() + userSuffix;
        try {
            Long count = stringRedisTemplate.execute(
                    LIMIT_SCRIPT, Collections.singletonList(key), String.valueOf(rateLimiter.time()));
            if (count != null && count > rateLimiter.count()) {
                metrics.incrRateLimitRejections();
                throw new BusinessException("请求过于频繁，请稍后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 不可用：限流降级为放行（fail-open），记录日志便于排查
            log.warn("限流组件异常，本次请求放行: {}", e.getMessage());
        }
    }

    /** 解析客户端 IP：优先取 nginx 覆盖写入的 X-Real-IP（不可伪造），
     *  否则取 X-Forwarded-For 首值（仅限直连/信任反代场景），并截断防超长。 */
    private String resolveClientIp() {
        try {
            var attrs = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sa) {
                String realIp = sa.getRequest().getHeader("X-Real-IP");
                if (realIp != null && !realIp.isBlank()) {
                    return truncate(realIp, 45);
                }
                String xff = sa.getRequest().getHeader("X-Forwarded-For");
                if (xff != null && !xff.isBlank()) {
                    return truncate(xff.split(",")[0].trim(), 45);
                }
                return truncate(sa.getRequest().getRemoteAddr(), 45);
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
