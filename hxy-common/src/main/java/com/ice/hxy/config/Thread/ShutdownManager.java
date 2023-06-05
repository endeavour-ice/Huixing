package com.ice.hxy.config.Thread;

import com.ice.hxy.util.Threads;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class ShutdownManager {
    @Resource
    private ExecutorService executorService;
    @Autowired
    @Qualifier("websocketExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @PreDestroy
    public void destroy() {
        shutdownAsyncManager();
    }

    /**
     * 停止异步执行任务
     */
    private void shutdownAsyncManager() {
        try {
            log.info("====关闭后台任务任务线程池====");
            threadPoolTaskExecutor.shutdown();
            Threads.shutdownAndAwaitTermination(executorService);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}