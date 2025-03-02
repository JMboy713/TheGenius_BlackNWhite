package com.genius.genius.common.config.jwt.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("Token")
public class RedisToken implements Serializable {
    @Id
    private Long userId; // Redis 에 저장될 키(유저 ID)
    @Setter
    private String refreshToken;
    /**
     * Redis 에서 자동 만료될 시간(초 단위)
     */
    @TimeToLive
    private long expiration;

    /**
     * 리프레시 토큰 업데이트 로직
     * @param newRefreshToken 새 리프레시 토큰
     * @param newExpiration 새 만료 시간(초)
     */
    public RedisToken refresh(String newRefreshToken, long newExpiration) {
        this.refreshToken = newRefreshToken;
        this.expiration = newExpiration;
        return this;
    }
}
