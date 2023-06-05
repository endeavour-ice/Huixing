package com.ice.hxy.util;

import lombok.extern.slf4j.Slf4j;

import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * 线程相关工具类.
 *
 */
@Slf4j
public class Threads {


    /**
     * sleep等待,单位为秒
     */
    public static void sleep(long m) {
        try {
            Thread.sleep((m*1000));
        } catch (InterruptedException ignored) {
        }
    }
    /**
     * 操作延迟10毫秒
     */
    private final int OPERATE_DELAY_TIME = 10;

    /**
     * 异步操作任务调度线程池
     */
    private final ScheduledExecutorService executor = SpringUtil.getBean("scheduledExecutorService");

    /**
     * 单例模式
     */
    private Threads() {
    }

    private static final Threads time = new Threads();

    public static Threads time() {
        return time;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(TimerTask task) {
        executor.schedule(task, OPERATE_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行任务
     * @param task 任务
     * @param SECONDS 秒
     */
    public void execute(TimerTask task,int SECONDS) {
        executor.schedule(task, SECONDS, TimeUnit.SECONDS);
    }
    /**
     * 停止任务线程池
     */
    public void shutdown() {
        Threads.shutdownAndAwaitTermination(executor);
    }
    /**
     * 停止线程池
     * 先使用shutdown, 停止接收新任务并尝试完成所有已存在任务.
     * 如果超时, 则调用shutdownNow, 取消在workQueue中Pending的任务,并中断所有阻塞函数.
     * 如果仍然超時，則強制退出.
     * 另对在shutdown时线程本身被调用中断做了处理.
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        log.info("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    /**
     * 打印线程异常信息
     */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            log.error(t.getMessage(), t);
        }
    }
}
