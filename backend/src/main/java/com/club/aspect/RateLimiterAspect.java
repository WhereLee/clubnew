package com.club.aspect;

import com.club.annotation.RateLimiter;
import com.club.common.BusinessException;
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

    @Before("@annotation(rateLimiter)")
    public void before(JoinPoint point, RateLimiter rateLimiter) {
        Long userId = SecurityUtils.getUserId();
        String userSuffix = userId != null ? ":" + userId : "";
        String key = RATE_LIMIT_PREFIX + rateLimiter.key() + userSuffix;
        try {
            Long count = stringRedisTemplate.execute(
                    LIMIT_SCRIPT, Collections.singletonList(key), String.valueOf(rateLimiter.time()));
            if (count != null && count > rateLimiter.count()) {
                throw new BusinessException("请求过于频繁，请稍后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 不可用：限流降级为放行（fail-open），记录日志便于排查
            log.warn("限流组件异常，本次请求放行: {}", e.getMessage());
        }
    }
}
