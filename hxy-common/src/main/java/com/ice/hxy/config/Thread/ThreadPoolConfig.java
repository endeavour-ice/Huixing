package com.ice.hxy.config.Thread;


import com.ice.hxy.util.Threads;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * @author ice
 * @date 2022/9/11 17:28
 */
@Configuration
@Slf4j
public class ThreadPoolConfig {
    // 核心线程池大小
    private int corePoolSize = Runtime.getRuntime().availableProcessors();

    // 最大可创建的线程数
    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2;

    // 队列最大长度
    private int queueCapacity = 1000;

    // 线程池维护线程所允许的空闲时间
    private int keepAliveTime = 300;

    /**
     * 线程池
     */
    @Bean(name = "executorService")
    public ExecutorService executorService() {
        return new ThreadPoolExecutor(corePoolSize,maxPoolSize
                , keepAliveTime,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(queueCapacity)){
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }
    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        return new ScheduledThreadPoolExecutor(corePoolSize,
                new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
    }

    @Bean("websocketExecutor")
    public ThreadPoolTaskExecutor websocketExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(2000);//支持同时推送1000人
        executor.setThreadNamePrefix("socket-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());//满了直接丢弃
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }
}
