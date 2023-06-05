package com.ice.hxy.config.Thread;


import lombok.AllArgsConstructor;

import java.util.concurrent.ThreadFactory;

/**
 * @Author ice
 * @Date 2023/6/1 19:42
 * @Description: 异常线程工厂
 */
@AllArgsConstructor
public class MyThreadFactory implements ThreadFactory {

    private ThreadFactory factory;
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = factory.newThread(r);
        thread.setUncaughtExceptionHandler(new ThreadPoolExceptionHandler());
        thread.setPriority(5);
        return thread;
    }
}
