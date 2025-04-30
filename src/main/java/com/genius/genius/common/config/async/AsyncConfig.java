package com.genius.genius.common.config.async;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfig {

    @Bean("gameExecutor")
    public ThreadPoolTaskExecutor gameExecutor(
            @Value("${GAME_EXECUTOR_CORE}") int core,
            @Value("${GAME_EXECUTOR_MAX}") int max,
            @Value("${GAME_EXECUTOR_QUEUE}") int queue) {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("game-");
        executor.setCorePoolSize(core); // 기본 스레드
        executor.setMaxPoolSize(max); // 최대 스레드
        executor.setQueueCapacity(queue); // 대기 큐
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
