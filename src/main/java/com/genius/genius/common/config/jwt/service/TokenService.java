package com.genius.genius.common.config.jwt.service;

import com.genius.genius.common.config.jwt.entity.RedisToken;
import com.genius.genius.common.config.jwt.repository.RedisTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RedisTokenRepository tokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${LOC_JWT_REFRESH_EXPIRE}")
    private long refreshTokenExpire;

    /**
     * userId에 해당하는 리프레시 토큰이 이미 있으면 갱신, 없으면 새로 저장
     * @param token Token 데이터
     */
    @Transactional
    public void saveOrRefresh(RedisToken token) {
        Optional<RedisToken> oldToken = tokenRepository.findByUserId(token.getUserId());
        if (oldToken.isPresent()) {
            // 기존 토큰 업데이트
            // refreshTokenExpire = 밀리초, toSeconds()로 변환
            oldToken.get().refresh(token.getRefreshToken(), calTimeout());
            tokenRepository.save(oldToken.get());
        } else {
            // 새 토큰 저장
            tokenRepository.save(
                    RedisToken.builder()
                            .userId(token.getUserId())
                            .refreshToken(token.getRefreshToken())
                            .expiration(calTimeout())
                            .build()
            );
        }
    }

    /**
     * 유닉스 시간(초 단위)로 변환
     */
    public long calTimeout() {
        return refreshTokenExpire / 1000;
    }

    /**
     * 리프레시 토큰을 무효화합니다.
     *
     * @param userId 사용자 Id
     */
    @Transactional
    public void invalidateRefreshToken(Long userId) {
        Optional<RedisToken> tokenOptional = tokenRepository.findByUserId(userId);
        tokenOptional.ifPresent(token -> {
            tokenRepository.delete(token);
            // Redis에서 키 삭제 (저장 시 사용하는 키와 일치하는지 확인 필요)
            redisTemplate.delete("Token:" + token.getUserId());
        });
    }
}
