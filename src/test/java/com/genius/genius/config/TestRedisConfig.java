package com.genius.genius.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
public class TestRedisConfig {

    private static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2.5").withExposedPorts(6379);

    static {
        redis.start();
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                redis.getHost(),
                redis.getMappedPort(6379));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> tpl = new RedisTemplate<>();
        tpl.setConnectionFactory(cf);
        // production 과 동일한 직렬화 설정이 필요하면 여기서 setKeySerializer … 등 지정
        return tpl;
    }
}