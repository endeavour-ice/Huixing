package com.ice.hxy.aop;

import com.ice.hxy.designPatten.factory.AsyncFactory;
import com.ice.hxy.util.GsonUtils;
import com.ice.hxy.mode.entity.OpLog;
import com.ice.hxy.mode.entity.User;
import com.ice.hxy.util.IpUtils;
import com.ice.hxy.util.Threads;
import com.ice.hxy.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @Author ice
 * @Date 2023/5/24 15:26
 * @Description: 日志切面
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    @Around("execution(* com.ice.hxy.controller..*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 计时
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        OpLog opLog = new OpLog();
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        String ipAddress = IpUtils.getIp(httpServletRequest);
        opLog.setOpIp(ipAddress);
        // 设置请求方式
        opLog.setMethod(httpServletRequest.getMethod());
        // 生成请求唯一 id
        String requestId = UUID.randomUUID().toString();
        String sessionId = httpServletRequest.getSession().getId();
        String url = httpServletRequest.getRequestURI();
        opLog.setOpUrl(url);
        // 获取方法名
        String methodName = point.getSignature().getName();
        opLog.setMethodName(methodName);
        // 获取请求参数
        Object[] args = point.getArgs();

        String reqParam = "[" + StringUtils.join(args, ", ") + "]";
        opLog.setParamData(reqParam);
        // 输出请求日志
        log.info("request start，id: {}, path: {}, ip: {}, params: {}, sessionId: {}", requestId, url,
                ipAddress, reqParam, sessionId);
        // 执行原方法
        Object result;
        try {
            result = point.proceed();
            // 输出响应日志
            String data = GsonUtils.getGson().toJson(result);
            data = StringUtils.substring(data, 0, 2000);
            opLog.setResultData(data);
            opLog.setStatus(0);

        } catch (Throwable e) {
            opLog.setStatus(1);
            String message = e.getMessage();
            message = StringUtils.substring(message, 0, 2000);
            opLog.setErrorMsg(message);
            throw e;
        } finally {
            stopWatch.stop();
            opLog.setOpTime(LocalDateTime.now());
            long totalTimeMillis = stopWatch.getTotalTimeMillis();
            opLog.setExTime(totalTimeMillis);
            log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);

            try {
                User loginUser = UserUtils.getLoginUser();
                String userAccount = loginUser.getUserAccount();
                opLog.setOpName(userAccount);
            } catch (Exception ignored) {

            }
            Threads.time().execute(AsyncFactory.recordOp(opLog));
        }
        return result;
    }
}
