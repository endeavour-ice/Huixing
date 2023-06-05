package com.ice.hxy.config.Thread;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author ice
 * @Date 2023/5/5 19:45
 * @Description: 线程异常处理器
 */
@Slf4j
public class ThreadPoolExceptionHandler implements Thread.UncaughtExceptionHandler{
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("name: {} 线程池异常 error:{} ",t.getName(),e.getMessage());
    }
}
