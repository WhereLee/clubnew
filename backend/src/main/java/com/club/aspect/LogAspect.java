package com.club.aspect;

import cn.hutool.json.JSONUtil;
import com.club.annotation.Log;
import com.club.domain.SysOperLog;
import com.club.service.SysOperLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.concurrent.Executor;

/**
 * 操作日志切面
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Resource
    private SysOperLogService operLogService;

    @Resource(name = "clubAsyncExecutor")
    private Executor executor;

    @Around("@annotation(logAnnotation)")
    public Object around(ProceedingJoinPoint point, Log logAnnotation) throws Throwable {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle(logAnnotation.title());
        operLog.setBusinessType(logAnnotation.businessType());
        operLog.setMethod(point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName());

        // 获取请求信息和操作人
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operLog.setRequestMethod(request.getMethod());
                operLog.setOperUrl(request.getRequestURI());
                operLog.setOperIp(request.getRemoteAddr());
            }
            // 记录当前登录用户名
            String username = com.club.security.SecurityUtils.getUsername();
            operLog.setOperName(username != null ? username : "anonymous");
        } catch (Exception e) {
            log.warn("获取请求信息失败", e);
        }

        // 请求参数
        try {
            operLog.setOperParam(JSONUtil.toJsonStr(point.getArgs()));
        } catch (Exception e) {
            operLog.setOperParam("参数序列化失败");
        }

        Object result = null;
        try {
            result = point.proceed();
            operLog.setStatus(0); // 成功
            try {
                operLog.setJsonResult(JSONUtil.toJsonStr(result));
            } catch (Exception ignored) {}
        } catch (Throwable e) {
            operLog.setStatus(1); // 失败
            operLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            operLog.setOperTime(LocalDateTime.now());
            // 异步写入
            executor.execute(() -> {
                try {
                    operLogService.save(operLog);
                } catch (Exception e) {
                    log.error("保存操作日志失败", e);
                }
            });
        }
        return result;
    }
}
