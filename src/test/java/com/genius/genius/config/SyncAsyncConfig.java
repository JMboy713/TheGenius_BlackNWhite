package com.genius.genius.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

@TestConfiguration
public class SyncAsyncConfig {
    //  테스트에서는 스레드풀 대신 동기 실행
    @Bean("gameExecutor")
    public Executor syncExec() {
        return Runnable::run;
    }
}
