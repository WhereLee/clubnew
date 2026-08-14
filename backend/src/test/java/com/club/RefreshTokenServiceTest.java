package com.club;

import com.club.common.BusinessException;
import com.club.domain.SysUser;
import com.club.security.JwtUtils;
import com.club.service.RefreshTokenService;
import com.club.service.SysMenuService;
import com.club.service.SysUserService;
import com.club.service.impl.RefreshTokenServiceImpl;
import com.club.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Refresh Token 轮换安全模型测试：
 * 1. 正常轮换：旧 refresh 作废，签发新 access + 新 refresh；
 * 2. 复用检测：已轮换的旧 token 再次出现 → 吊销全部会话 + 拉黑 userId；
 * 3. 未知 token：直接拒绝。
 */
class RefreshTokenServiceTest {

    private StringRedisTemplate redis;
    private ValueOperations<String, String> valueOps;
    private SetOperations<String, String> setOps;
    private SysUserService userService;
    private SysMenuService menuService;
    private RefreshTokenServiceImpl service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redis = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        setOps = mock(SetOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(redis.opsForSet()).thenReturn(setOps);

        userService = mock(SysUserService.class);
        menuService = mock(SysMenuService.class);

        JwtUtils jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "clubflow2024secretkey1234567890abcdef");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 1800000L);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpiration", 604800000L);

        service = new RefreshTokenServiceImpl();
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redis);
        ReflectionTestUtils.setField(service, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "menuService", menuService);

        // 默认用户数据
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("pwd");
        user.setNickname("管理员");
        user.setUserType("ADMIN");
        user.setStatus("0");
        when(userService.getById(1L)).thenReturn(user);
        when(menuService.selectPermsByUserId(1L)).thenReturn(new java.util.HashSet<>(Set.of("*:*:*")));
    }

    @Test
    void refresh_validToken_rotatesAndReturnsNewPair() {
        String oldRt = "old-refresh-token";
        // Lua 原子 GET+DEL 返回 userId
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class), anyList()))
                .thenReturn("1");

        LoginVO vo = service.refresh(oldRt);

        assertNotNull(vo.getToken());
        assertNotNull(vo.getRefreshToken());
        assertNotEquals(oldRt, vo.getRefreshToken(), "轮换后必须签发新的 refresh token");
        // 旧 token 由 Lua 原子删除（不再有单独 delete 调用）
        // 旧 token 标记为已使用（值含时间戳，供复用检测宽限期判定）
        verify(valueOps).set(eq("refresh:used:" + oldRt), startsWith("1:"), anyLong(), any());
        // 从用户集合移除旧 token
        verify(setOps).remove("refresh:user:1", oldRt);
    }

    @Test
    void refresh_reusedToken_revokesAllAndBlacklists() {
        String usedRt = "already-rotated-token";
        // Lua 返回 null（token 不存在），used 标记存在且已过宽限期 → 真实复用检测
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class), anyList()))
                .thenReturn(null);
        when(valueOps.get("refresh:used:" + usedRt)).thenReturn("1:" + (System.currentTimeMillis() - 60_000));
        when(setOps.members("refresh:user:1")).thenReturn(Set.of("other-rt"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.refresh(usedRt));
        assertTrue(ex.getMessage().contains("异常登录"), "复用检测应提示异常登录");

        // 吊销全部 refresh 会话 + 一次性消费 used 标记
        verify(redis).delete("refresh:token:other-rt");
        verify(redis).delete("refresh:user:1");
        verify(redis).delete("refresh:used:" + usedRt);
        // 拉黑 userId（TTL = access 有效期）
        verify(valueOps).set(eq("user_blacklist:1"), eq("1"), anyLong(), any());
        // 不签发新 token
        verify(valueOps, never()).set(startsWith("login_tokens:"), anyString(), anyLong(), any());
    }

    @Test
    void refresh_replayedWithinGracePeriod_onlyRejectedNoRevoke() {
        String rt = "grace-period-token";
        // 宽限期内（5 秒内）的重放：仅拒绝，不吊销不拉黑（多标签页并发续期误伤防护）
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class), anyList()))
                .thenReturn(null);
        when(valueOps.get("refresh:used:" + rt)).thenReturn("1:" + (System.currentTimeMillis() - 1000));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.refresh(rt));
        assertTrue(ex.getMessage().contains("已过期"), "宽限期内应仅提示过期");

        verify(redis, never()).delete("user_blacklist:1");
        verify(valueOps, never()).set(eq("user_blacklist:1"), anyString(), anyLong(), any());
        verify(setOps, never()).members(anyString());
    }

    @Test
    void refresh_unknownToken_rejected() {
        String unknown = "unknown-token";
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class), anyList()))
                .thenReturn(null);
        when(valueOps.get("refresh:used:" + unknown)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.refresh(unknown));
        assertTrue(ex.getMessage().contains("已过期") || ex.getMessage().contains("重新登录"));
    }

    @Test
    void refresh_redisDown_failSecure() {
        when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class), anyList()))
                .thenThrow(new RuntimeException("connection refused"));

        assertThrows(BusinessException.class, () -> service.refresh("any"));
        // fail-secure：Redis 故障时不签发任何新会话
        verify(valueOps, never()).set(startsWith("login_tokens:"), anyString(), anyLong(), any());
    }

    @Test
    void revoke_removesTokenAndUserSet() {
        when(valueOps.get("refresh:token:rt1")).thenReturn("1");
        service.revoke("rt1");

        verify(redis).delete("refresh:token:rt1");
        verify(setOps).remove("refresh:user:1", "rt1");
    }

    @Test
    void isBlacklisted_checksRedis() {
        when(valueOps.get("user_blacklist:1")).thenReturn("1");
        assertTrue(service.isBlacklisted(1L));

        when(valueOps.get("user_blacklist:2")).thenReturn(null);
        assertFalse(service.isBlacklisted(2L));
        assertFalse(service.isBlacklisted(null));
    }
}
