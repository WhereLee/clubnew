package com.club.service.impl;

import cn.hutool.core.util.IdUtil;
import com.club.common.BusinessException;
import com.club.domain.SysLoginLog;
import com.club.domain.SysUser;
import com.club.security.JwtUtils;
import com.club.security.LoginUser;
import com.club.service.LoginService;
import com.club.service.SysLoginLogService;
import com.club.service.SysMenuService;
import com.club.service.SysUserService;
import com.club.vo.LoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginServiceImpl.class);

    private static final String LOGIN_TOKENS_PREFIX = "login_tokens:";

    @Resource
    private SysUserService userService;

    @Resource
    private SysMenuService menuService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysLoginLogService loginLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LoginVO login(String username, String password) {
        // 查找用户
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            recordLoginLog(username, "1", "用户不存在");
            throw new BusinessException("用户不存在");
        }
        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            recordLoginLog(username, "1", "密码错误");
            throw new BusinessException("密码错误");
        }
        // 校验状态
        if ("1".equals(user.getStatus())) {
            recordLoginLog(username, "1", "账号已停用");
            throw new BusinessException("账号已停用");
        }
        // 加载权限
        Set<String> permissions = menuService.selectPermsByUserId(user.getId());
        // 如果是管理员，赋予全部权限
        if ("ADMIN".equals(user.getUserType())) {
            permissions.add("*:*:*");
        }
        // 构建 LoginUser
        LoginUser loginUser = new LoginUser(
                user.getId(), user.getUsername(), user.getPassword(),
                user.getNickname(), user.getUserType(), permissions
        );
        // 生成 JWT（内嵌身份与权限快照，Redis 故障时可降级认证）
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getUserType(), permissions);
        // 存入 Redis（失败不阻断登录：认证过滤器会在 Redis 不可用时降级为 JWT 内嵌信息）
        String redisKey = LOGIN_TOKENS_PREFIX + token;
        try {
            stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(loginUser),
                    jwtUtils.getExpiration(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warn("Redis 会话写入失败，登录降级为无状态模式: {}", e.getMessage());
        }
        // 记录登录日志
        recordLoginLog(username, "0", "登录成功");

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(jwtUtils.getExpiration() / 1000);
        return vo;
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null) {
            stringRedisTemplate.delete(LOGIN_TOKENS_PREFIX + token);
        }
    }

    private void recordLoginLog(String username, String status, String msg) {
        try {
            SysLoginLog log = new SysLoginLog();
            log.setUserName(username);
            log.setStatus(status);
            log.setMsg(msg);
            log.setLoginTime(LocalDateTime.now());
            loginLogService.save(log);
        } catch (Exception ignored) {}
    }
}
