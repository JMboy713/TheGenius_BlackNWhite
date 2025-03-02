package com.genius.genius.common.config.jwt.repository;

import com.genius.genius.common.config.jwt.entity.RedisToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Redis를 사용하는 토큰 리포지토리
 */
@Repository
public interface RedisTokenRepository extends CrudRepository<RedisToken, Long> {
    Optional<RedisToken> findByUserId(Long userId);
    Optional<RedisToken> findByRefreshToken(String refreshToken);
}
