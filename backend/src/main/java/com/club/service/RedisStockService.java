package com.club.service;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Redis 库存预扣服务（防超卖增强）。
 * <p>
 * 通过 Lua 脚本保证「检查库存 + 扣减」的原子性，与数据库层的
 * {@code UPDATE ... WHERE applied_count < quota} 原子更新形成双层防超卖。
 * 当 Redis 不可用或库存未初始化时，返回降级标记，由调用方走 DB 兜底。
 * </p>
 */
@Service
public class RedisStockService {

    /**
     * Lua 脚本：库存未初始化返回 -1；扣减成功返回 1；库存不足返回 0（并回滚）。
     * KEYS[1] = 库存 key
     */
    private static final DefaultRedisScript<Long> DEDUCT_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('EXISTS', KEYS[1]) == 0 then return -1 end " +
            "local stock = redis.call('DECR', KEYS[1]) " +
            "if stock < 0 then redis.call('INCR', KEYS[1]) return 0 end " +
            "return 1",
            Long.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 初始化库存（纳新/活动创建时写入 quota） */
    public void initStock(String key, long quota) {
        stringRedisTemplate.opsForValue().set(key, String.valueOf(quota));
    }

    /**
     * 原子预扣库存。
     *
     * @return 1=扣减成功；0=库存不足；-1=Redis 不可用或库存未初始化（调用方降级走 DB 兜底）
     */
    public int tryDeduct(String key) {
        try {
            Long result = stringRedisTemplate.execute(DEDUCT_SCRIPT, Collections.singletonList(key));
            if (result == null) {
                return -1; // mock / 不可用，降级
            }
            return result.intValue();
        } catch (Exception e) {
            return -1; // 异常降级
        }
    }

    /** 回滚预扣（DB 扣减失败时调用） */
    public void rollback(String key) {
        try {
            stringRedisTemplate.opsForValue().increment(key);
        } catch (Exception ignored) {
            // 回滚失败不阻断，最终一致性由 DB 兜底
        }
    }
}
