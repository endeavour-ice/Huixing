package com.ice.hxy.aop;

import com.ice.hxy.annotation.CurrentLimiting;
import com.ice.hxy.common.B;
import com.ice.hxy.util.IpUtils;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2022/10/10 18:23
 * @Description: aop
 * @Version 1.0
 */
@Aspect
@Component
@Slf4j
public class CurrentLimitingAspect {
    private static final Map<String, ExpiringMap<String, Integer>> map = new ConcurrentHashMap<>();

    // 限制请求的频率
    @Around(value = "@annotation(currentLimiting)")// 注解标注的
    public Object doAround(ProceedingJoinPoint pjp, CurrentLimiting currentLimiting) throws Throwable {
        // 获得request对象
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        if (sra == null) {
            return B.error("请求过快，请稍后重试");
        }
        HttpServletRequest request = sra.getRequest();
        String ipAddress = IpUtils.getIpAddress(request);
        // 获取Map value对象， 如果没有则返回默认值
        // getOrDefault获取参数，获取不到则给默认值
        ExpiringMap<String, Integer> em = map.getOrDefault(ipAddress,
                ExpiringMap.builder().variableExpiration().build());
        Integer Count = em.getOrDefault(ipAddress, 0);
        if (Count >= currentLimiting.value()) { // 超过次数，不执行目标方法
            return B.error("请求过快，请稍后重试");
        } else if (Count == 0) { // 第一次请求时，设置有效时间
            em.put(ipAddress, Count + 1,
                    ExpirationPolicy.CREATED, currentLimiting.time(), TimeUnit.MILLISECONDS);
        } else { // 未超过次数， 记录加一
            em.put(ipAddress, Count + 1);
        }
        map.put(ipAddress, em);
        // result的值就是被拦截方法的返回值
        return pjp.proceed();
    }




}
