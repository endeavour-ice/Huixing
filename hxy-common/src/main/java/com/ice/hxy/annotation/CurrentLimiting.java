package com.ice.hxy.annotation;

import java.lang.annotation.*;

/**
 * 限流注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentLimiting {
    long time() default 2000; // 限制时间 单位：毫秒
     int value() default 5; // 允许请求的次数
}