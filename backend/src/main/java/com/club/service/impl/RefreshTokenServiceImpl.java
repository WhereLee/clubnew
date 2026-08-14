package com.club.service.impl;

import com.club.common.BusinessException;
import com.club.common.ResultCode;
import com.club.domain.SysUser;
import com.club.security.JwtUtils;
import com.club.security.LoginUser;
import com.club.service.RefreshTokenService;
import com.club.service.SysMenuService;
import com.club.service.SysUserService;
import com.club.vo.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token 实现。Redis 键设计：
 * <pre>
 * refresh:token:{rt}   → userId（有效 refresh，TTL=7d）
 * refresh:used:{rt}    → userId（已轮换标记，TTL=7d，用于复用检测）
 * refresh:user:{uid}   → Set&lt;rt&gt;（用户全部有效 refresh，用于整体吊销）
 * user_blacklist:{uid} → "1"（复用检测触发，TTL=access 有效期）
 * </pre>
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    private static final String TOKEN_PREFIX = "refresh:token:";
    private static final String USED_PREFIX = "refresh:used:";
    private static final String USER_SET_PREFIX = "refresh:user:";
    private static final String BLACKLIST_PREFIX = "user_blacklist:";
    private static final String LOGIN_TOKENS_PREFIX = "login_tokens:";

    /**
     * Lua：原子「读取并删除」refresh token，返回删除前的原值（userId）。
     * 返回 null=不存在（并发下另一个请求已抢先轮换，或已过期）。
     * 消除 GET+DELETE 两步之间的 TOCTOU 窗口：同一 refresh token 的并发请求只能有一个成功。
     */
    private static final DefaultRedisScript<String> TAKE_TOKEN_SCRIPT = new DefaultRedisScript<>(
            "local v = redis.call('GET', KEYS[1]) " +
            "if v then redis.call('DEL', KEYS[1]) return v else return nil end",
            String.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private SysUserService userService;

    @Resource
    private SysMenuService menuService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String issueRefreshToken(Long userId) {
        String refreshToken = jwtUtils.generateRefreshToken();
        long ttl = jwtUtils.getRefreshExpiration();
        try {
            stringRedisTemplate.opsForValue().set(TOKEN_PREFIX + refreshToken, String.valueOf(userId), ttl, TimeUnit.MILLISECONDS);
            stringRedisTemplate.opsForSet().add(USER_SET_PREFIX + userId, refreshToken);
            stringRedisTemplate.expire(USER_SET_PREFIX + userId, ttl, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // 登录时 Redis 不可用：refresh 能力降级关闭（access 的 JWT 降级仍可用）
            log.warn("refresh token 签发失败，本次登录仅返回 access token: {}", e.getMessage());
            return null;
        }
        return refreshToken;
    }

    /** 复用检测宽限期：多标签页并发续期（各持同一 rt）会天然触发「伪复用」，
     * 5 秒内的重放只拒绝不吊销，超时才算真实泄露（业界 grace period 做法）。 */
    private static final long REUSE_GRACE_MS = 5000;

    @Override
    public LoginVO refresh(String refreshToken) {
        String tokenKey = TOKEN_PREFIX + refreshToken;
        String usedKey = USED_PREFIX + refreshToken;
        try {
            // 1. 原子「读取并删除」：并发下同一 refresh token 只有一个请求能拿到 userId，
            //    其余请求拿 null 走复用检测（已轮换）或过期分支，彻底消除 TOCTOU 竞态
            String userIdStr = stringRedisTemplate.execute(
                    TAKE_TOKEN_SCRIPT, Collections.singletonList(tokenKey));
            if (userIdStr == null) {
                // 2. 已轮换过？读 used 标记（值格式 userId:timestamp）
                String usedValue = stringRedisTemplate.opsForValue().get(usedKey);
                if (usedValue != null) {
                    return handleReusedToken(usedValue, usedKey);
                }
                throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已过期，请重新登录");
            }
            Long userId = Long.parseLong(userIdStr);

            // 3. 轮换：标记 used（带时间戳供宽限期判定）→ 签发新对（token 已被 Lua 删除）
            stringRedisTemplate.opsForSet().remove(USER_SET_PREFIX + userId, refreshToken);
            stringRedisTemplate.opsForValue().set(usedKey, userIdStr + ":" + System.currentTimeMillis(),
                    jwtUtils.getRefreshExpiration(), TimeUnit.MILLISECONDS);

            return buildLoginVO(userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 不可用：refresh 是安全敏感的低频操作，fail-secure 直接拒绝
            log.error("refresh 处理失败: {}", e.getMessage());
            throw new BusinessException(ResultCode.UNAUTHORIZED, "服务繁忙，请重新登录");
        }
    }

    /** used 标记处理：宽限期内仅拒绝（多标签页误伤），超时按真实泄露处理 */
    private LoginVO handleReusedToken(String usedValue, String usedKey) {
        Long usedBy;
        long usedAt;
        try {
            int idx = usedValue.indexOf(':');
            usedBy = Long.parseLong(usedValue.substring(0, idx));
            usedAt = Long.parseLong(usedValue.substring(idx + 1));
        } catch (Exception e) {
            // 旧格式（无时间戳）一律按真实泄露处理
            usedBy = Long.parseLong(usedValue);
            usedAt = 0;
        }
        if (System.currentTimeMillis() - usedAt < REUSE_GRACE_MS) {
            log.warn("宽限期内的 refresh 重放（疑似多标签页并发续期），仅拒绝: userId={}", usedBy);
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已过期，请重新登录");
        }
        return handleReuse(usedBy, usedKey);
    }

    /** 复用检测命中：疑似 refresh 泄露 → 吊销全部 refresh 会话 + 拉黑 userId + 消费 used 标记（检测只生效一次，避免已泄露的旧 token 无限重放造成持续 DoS） */
    private LoginVO handleReuse(Long userId, String usedKey) {
        log.warn("检测到 refresh token 复用（疑似泄露），吊销用户 {} 全部会话", userId);
        revokeAll(userId);
        try {
            // 一次性消费：删除 used 标记，后续重放按「已过期」处理
            stringRedisTemplate.delete(usedKey);
            stringRedisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + userId, "1", jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            // 拉黑失败不阻断（refresh 会话已吊销）
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED, "检测到账号异常登录，已强制下线，请重新登录");
    }

    @Override
    public void revoke(String refreshToken) {
        try {
            String userIdStr = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + refreshToken);
            if (userIdStr != null) {
                stringRedisTemplate.delete(TOKEN_PREFIX + refreshToken);
                stringRedisTemplate.opsForSet().remove(USER_SET_PREFIX + userIdStr, refreshToken);
            }
        } catch (Exception e) {
            log.warn("refresh token 注销失败: {}", e.getMessage());
        }
    }

    @Override
    public void revokeAll(Long userId) {
        try {
            Set<String> tokens = stringRedisTemplate.opsForSet().members(USER_SET_PREFIX + userId);
            if (tokens != null) {
                for (String t : tokens) {
                    stringRedisTemplate.delete(TOKEN_PREFIX + t);
                }
            }
            stringRedisTemplate.delete(USER_SET_PREFIX + userId);
        } catch (Exception e) {
            log.warn("refresh 会话整体吊销失败: {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(Long userId) {
        if (userId == null) return false;
        try {
            return stringRedisTemplate.opsForValue().get(BLACKLIST_PREFIX + userId) != null;
        } catch (Exception e) {
            // Redis 不可用时 fail-open（access 本身短时有效，风险窗口有限）
            return false;
        }
    }

    @Override
    public void clearBlacklist(Long userId) {
        if (userId == null) return;
        try {
            stringRedisTemplate.delete(BLACKLIST_PREFIX + userId);
        } catch (Exception e) {
            log.warn("解除拉黑失败: {}", e.getMessage());
        }
    }

    /** 构建 LoginVO：签发新 access + 新 refresh（与登录流程同源） */
    private LoginVO buildLoginVO(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null || "1".equals(user.getStatus())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "账号不存在或已停用");
        }
        Set<String> permissions = new java.util.HashSet<>(menuService.selectPermsByUserId(userId));
        if ("ADMIN".equals(user.getUserType())) {
            permissions.add("*:*:*");
        }
        LoginUser loginUser = new LoginUser(
                user.getId(), user.getUsername(), user.getPassword(),
                user.getNickname(), user.getUserType(), permissions);

        String accessToken = jwtUtils.generateToken(
                user.getId(), user.getUsername(), user.getUserType(), permissions);
        // 新 refresh 由 issue 登记会话；失败则仅返回 access（降级）
        String newRefreshToken = issueRefreshToken(userId);

        // 将新 access 会话写入 Redis（与登录一致；失败走 JWT 内嵌降级）
        try {
            stringRedisTemplate.opsForValue().set(
                    LOGIN_TOKENS_PREFIX + accessToken, objectMapper.writeValueAsString(loginUser),
                    jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Redis 会话写入失败，access 降级为无状态模式: {}", e.getMessage());
        }

        LoginVO vo = new LoginVO();
        vo.setToken(accessToken);
        vo.setExpiresIn(jwtUtils.getExpiration() / 1000);
        vo.setRefreshToken(newRefreshToken);
        vo.setRefreshExpiresIn(jwtUtils.getRefreshExpiration() / 1000);
        return vo;
    }
}
