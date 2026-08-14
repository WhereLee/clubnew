package com.club.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    /** 限流key */
    String key() default "rate_limit";

    /** 限流次数 */
    int count() default 100;

    /** 限流时间窗口(秒) */
    int time() default 60;
}
