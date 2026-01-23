package com.xenia.config;

import com.xenia.core.Config;
import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.*;

public class PrimaryThreadPool {

    private static PrimaryThreadPool INSTANCE;

    private Config.ThreadPoolConfig threadPoolConfig;

    @Getter
    private ExecutorService executor;

    @Getter
    private ScheduledExecutorService scheduledExecutor;

    private PrimaryThreadPool() {
    }

    public static PrimaryThreadPool instance(Config.ThreadPoolConfig threadPoolConfig) {
        if (Objects.isNull(PrimaryThreadPool.INSTANCE)) {
            synchronized (PrimaryThreadPool.class) {
                if (Objects.isNull(PrimaryThreadPool.INSTANCE)) {
                    PrimaryThreadPool.INSTANCE = new PrimaryThreadPool();
                    PrimaryThreadPool.INSTANCE.threadPoolConfig = threadPoolConfig;
                    PrimaryThreadPool.INSTANCE.initExecutor();
                }
            }
        }
        return INSTANCE;
    }

    private void initExecutor() {
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName(threadPoolConfig.getThreadNamePrefix() + thread.getName());
            return thread;
        };
        this.executor = new ThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadPoolConfig.getMaxPoolSize(),
                threadPoolConfig.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(threadPoolConfig.getQueueCapacity()),
                threadFactory,
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
        this.scheduledExecutor = new ScheduledThreadPoolExecutor(
                threadPoolConfig.getCorePoolSize(),
                threadFactory,
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

}
