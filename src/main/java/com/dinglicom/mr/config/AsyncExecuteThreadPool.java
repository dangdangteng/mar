package com.dinglicom.mr.config;

import lombok.extern.java.Log;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Log
@Configuration
public class AsyncExecuteThreadPool implements AsyncConfigurer {
    @Value("${async.pool.corePoolSize}")
    private int corePoolSize;

    @Value("${async.pool.maxPoolSize}")
    private int maxPoolSize;

    @Value("${async.pool.keepAliveSeconds}")
    private int keepAliveSeconds;

    @Value("${async.pool.queueCapacity}")
    private int queueCapacity;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("LovinMe Thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {
            @Override
            public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
                log.info("=========================="+throwable.getMessage()+"=======================");
                log.info("exception method:"+method.getName());
            }
        };
    }
}

