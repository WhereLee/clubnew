package com.club.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JWT 工具类（jjwt 0.13 API）。
 * <p>
 * 设计说明：
 * <ul>
 *   <li>access token 短时有效（默认 30 分钟），配合 Redis 会话存储实现主动登出/踢人；</li>
 *   <li>Redis 不可用时 Filter 会降级为纯 JWT 签名校验，保证可用性（见 {@code JwtAuthenticationFilter}）；</li>
 *   <li>数字 claim 通过 {@code Number} 读取，避免 jjwt 0.12+ Jackson 反序列化时 Integer/Long 类型不一致。</li>
 * </ul>
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token（内嵌用户身份与权限快照，供 Redis 故障时降级认证使用）
     */
    public String generateToken(Long userId, String username, String userType, Set<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);
        claims.put("perms", permissions != null ? permissions.toArray(new String[0]) : new String[0]);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 token
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 token 获取 userId
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        Number userId = claims.get("userId", Number.class);
        return userId != null ? userId.longValue() : null;
    }

    /**
     * 从 token 获取 username
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 验证 token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiration() {
        return expiration;
    }
}
