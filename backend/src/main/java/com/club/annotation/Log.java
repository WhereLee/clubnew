package com.club.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /** 模块标题 */
    String title() default "";

    /** 业务类型(0其它 1新增 2修改 3删除) */
    int businessType() default 0;
}
