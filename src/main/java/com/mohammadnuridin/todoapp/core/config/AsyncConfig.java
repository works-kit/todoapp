package com.mohammadnuridin.todoapp.core.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        var factory = Thread.ofVirtual()
                .name("async-vt-", 0)
                .factory();
        return Executors.newThreadPerTaskExecutor(factory);
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        // Custom handler — pakai Slf4j supaya masuk ke Logback / log file
        return (ex, method, params) -> log.error("Uncaught exception in @Async [{}.{}]: {}",
                method.getDeclaringClass().getSimpleName(),
                method.getName(),
                ex.getMessage(),
                ex);
    }
}