package com.club.aspect;

import cn.hutool.crypto.digest.DigestUtil;
import com.club.annotation.RepeatSubmit;
import com.club.common.BusinessException;
import com.club.security.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 防重复提交切面 - userId 从 SecurityUtils 获取真实登录用户
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    private static final Logger log = LoggerFactory.getLogger(RepeatSubmitAspect.class);
    private static final String REPEAT_SUBMIT_PREFIX = "repeat:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Before("@annotation(repeatSubmit)")
    public void before(JoinPoint point, RepeatSubmit repeatSubmit) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        // 从 SecurityUtils 获取真实用户 ID
        Long userId = SecurityUtils.getUserId();
        String userKey = userId != null ? userId.toString() : "anonymous";

        String params;
        try {
            params = cn.hutool.json.JSONUtil.toJsonStr(point.getArgs());
        } catch (Exception e) {
            params = "";
        }
        String paramMd5 = DigestUtil.md5Hex(params);
        String key = REPEAT_SUBMIT_PREFIX + userKey + ":" + url + ":" + paramMd5;
        try {
            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", repeatSubmit.interval(), TimeUnit.MILLISECONDS);
            if (Boolean.FALSE.equals(success)) {
                throw new BusinessException("请勿重复提交");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // Redis 不可用：防重复提交降级为放行（fail-open），DB 唯一约束兜底
            log.warn("防重复提交组件异常，本次请求放行: {}", e.getMessage());
        }
    }
}
