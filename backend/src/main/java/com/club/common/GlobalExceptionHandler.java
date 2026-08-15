package com.club.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务异常 */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        // 业务失败属预期内流程，warn 级别即可，error 留给系统异常
        log.warn("业务异常: {} -> {}", request.getRequestURI(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /** 参数校验异常 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        return R.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 绑定异常 */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("参数绑定失败");
        return R.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 无权限（@PreAuthorize 方法级拒绝：与 SecurityExceptionHandler 的 HTTP 403 语义一致） */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        return R.fail(ResultCode.FORBIDDEN, "没有权限");
    }

    /** 404 */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return R.fail(ResultCode.NOT_FOUND, "资源不存在");
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ResultCode.BAD_REQUEST, "不支持'" + e.getMethod() + "'请求");
    }

    /** 请求体 JSON 格式错误 / 类型不匹配（客户端问题，返回 400 而非 500） */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST, "请求参数格式错误");
    }

    /** 方法级参数校验失败（@Validated / @RequestParam 约束） */
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleConstraintViolation(jakarta.validation.ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("参数校验失败");
        return R.fail(ResultCode.BAD_REQUEST, message);
    }

    /** 唯一约束冲突兜底（并发重复提交在 Service 层已友好处理，此处防其余场景裸 500） */
    @ExceptionHandler(org.springframework.dao.DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleDuplicateKey(org.springframework.dao.DuplicateKeyException e) {
        log.warn("唯一约束冲突: {}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST, "数据已存在，请勿重复操作");
    }

    /** 系统异常 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {}", request.getRequestURI(), e);
        return R.fail(ResultCode.FAIL, "系统异常");
    }
}
