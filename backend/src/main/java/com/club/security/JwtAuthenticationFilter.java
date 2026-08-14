package com.club.security;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JWT 认证过滤器。
 * <p>
 * 认证策略（有状态 JWT + Redis 降级）：
 * <ol>
 *   <li>JWT 签名校验失败 → 不认证（401）；</li>
 *   <li>签名通过后查询 Redis 会话（{@code login_tokens:{token}}）：
 *       <ul>
 *         <li>命中 → 反序列化完整 LoginUser（登录时刻的权限快照）；</li>
 *         <li>key 不存在 → 会话已注销/过期，拒绝认证；</li>
 *         <li><b>Redis 不可用（异常）→ 降级</b>：用 token 内嵌 claim（userId/username/userType/perms）
 *             构建最小 LoginUser，保证服务可用；此时丧失「主动踢人」能力，Redis 恢复后自动回到强一致路径。</li>
 *       </ul>
 *   </li>
 * </ol>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String LOGIN_TOKENS_PREFIX = "login_tokens:";

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            String token = authHeader.substring(TOKEN_PREFIX.length());
            if (jwtUtils.validateToken(token)) {
                authenticate(token);
            }
        }
        filterChain.doFilter(request, response);
    }

    /** 先查 Redis 会话，Redis 故障时降级为 token 内嵌信息 */
    private void authenticate(String token) {
        try {
            String redisKey = LOGIN_TOKENS_PREFIX + token;
            String cached = stringRedisTemplate.opsForValue().get(redisKey);
            if (cached != null) {
                LoginUser loginUser = objectMapper.readValue(cached, LoginUser.class);
                setAuthentication(loginUser);
            }
            // cached == null：会话已注销/过期，拒绝（区别于 Redis 故障的降级路径）
        } catch (Exception e) {
            log.warn("Redis 会话查询失败，降级为 JWT 内嵌信息认证: {}", e.getMessage());
            setAuthentication(buildFallbackLoginUser(token));
        }
    }

    /** Redis 不可用时从 token claim 构建最小登录信息（登录时刻的权限快照） */
    private LoginUser buildFallbackLoginUser(String token) {
        Claims claims = jwtUtils.parseToken(token);
        LoginUser user = new LoginUser();
        Number userId = claims.get("userId", Number.class);
        user.setUserId(userId != null ? userId.longValue() : null);
        user.setUsername(claims.get("username", String.class));
        user.setUserType(claims.get("userType", String.class));
        List<?> perms = claims.get("perms", List.class);
        Set<String> permissions = new HashSet<>();
        if (perms != null) {
            for (Object p : perms) {
                if (p != null) permissions.add(p.toString());
            }
        }
        user.setPermissions(permissions);
        return user;
    }

    private void setAuthentication(LoginUser loginUser) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
