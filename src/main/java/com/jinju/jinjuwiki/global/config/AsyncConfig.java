package com.jinju.jinjuwiki.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);   // 기본 스레드 수
        executor.setMaxPoolSize(10);   // 최대 스레드 수
        executor.setQueueCapacity(200); // 대기 큐
        executor.setThreadNamePrefix("MailAsync-");
        executor.initialize();
        return executor;
    }

}
