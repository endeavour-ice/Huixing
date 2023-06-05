package com.ice.hxy.aop;

import com.ice.hxy.annotation.RedissonLock;
import com.ice.hxy.common.ErrorCode;
import com.ice.hxy.exception.GlobalException;
import com.ice.hxy.util.SpELUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2023/5/24 14:51
 * @Description: 分布式锁切面
 */
@Slf4j
@Aspect
@Component
@Order(0)//确保比事务注解先执行，分布式锁在事务外
public class RedissonLockAspect {
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.ice.hxy.annotation.RedissonLock)")
    public Object lock(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);
        String prefix = StringUtils.hasText(redissonLock.prefixKey()) ? redissonLock.prefixKey() : SpELUtils.getMethodKey(method);
        String key = SpELUtils.parseSpEl(method, pjp.getArgs(), redissonLock.key());
        key = StringUtils.hasText(key) ? prefix + ":" + key : prefix;
        return Locking(key, redissonLock.waitTime(), redissonLock.unit(), pjp);
    }

    public Object Locking(String key, int waitTime, TimeUnit unit, ProceedingJoinPoint pjp) throws Throwable {
        RLock lock = redissonClient.getLock(key);
        boolean lockSuccess = lock.tryLock(waitTime, unit);
        if (!lockSuccess) {
            log.error("加锁失败 error key:{}",key);
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
        try {
            return pjp.proceed();//执行锁内的代码逻辑
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
