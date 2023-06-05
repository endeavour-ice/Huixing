package com.ice.hxy.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 */
@Retention(RetentionPolicy.RUNTIME)//运行时生效
@Target(ElementType.METHOD)//作用在方法上
public @interface RedissonLock {
    /**
     * @return key的前缀 默认方法名
     */
    String prefixKey() default "";

    /**
     * @return springEl 表达式
     */
    String key() default "";

    /**
     * @return 等待锁的时间，默认-1，不等待直接失败,redisson默认也是-1
     */
    int waitTime() default -1;

    /**
     * @return 等待锁的时间单位，默认毫秒
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
}
