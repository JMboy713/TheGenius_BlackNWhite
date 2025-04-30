package com.genius.genius.domain.game.util;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameKeys {
    // 현재 차례
    public String turnUserKey(UUID gameId) {
        return String.format("game:%s:turnUser",gameId);
    }

    // 현재 라운드
    public String currentRoundKey(UUID gameId) {
        return String.format("game:%s:currentRound",gameId);
    }

    // 0번째는 남은 점수, 1번째부터 라운드 기록
    public String remainKey(UUID gameId, Long userId) {
        return String.format("game:%s:user:%d", gameId, userId);
    }

    // 스코어
    public String scoreKey(UUID gameId, Long userId) {
        return String.format("game:%s:score:%d", gameId, userId);
    }

    // 참가 유저
    public String playersKey(UUID gameId) {
        return String.format("game:%s:players",gameId);
    }
}
