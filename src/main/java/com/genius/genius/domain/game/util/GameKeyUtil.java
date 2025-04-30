package com.genius.genius.domain.game.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameKeyUtil {

    private final GameKeys gameKeys;
    private final RedisTemplate<String, String> redisTemplate;

    // =============    TurnUser    =============
    public String getTurnUser(UUID gameId) {
        String key = gameKeys.turnUserKey(gameId);
        return redisTemplate.opsForValue().get(key);
    }

    public void setTurnUser(UUID gameId, String user) {
        String key = gameKeys.turnUserKey(gameId);
        redisTemplate.opsForValue().set(key, user);
    }

    // =============    CurrentRound    =============
    public int getCurrentRound(UUID gameId) {
        String key = gameKeys.currentRoundKey(gameId);
        String val = redisTemplate.opsForValue().get(key);
        return (val != null) ? Integer.parseInt(val) : 1;
    }

    public void setCurrentRound(UUID gameId, int round) {
        String key = gameKeys.currentRoundKey(gameId);
        redisTemplate.opsForValue().set(key, String.valueOf(round));
    }

    // =============    Remain  =============
    /**
     * remainKey: "game:{gameId}:user:{userId}"
     * List 구조:
     *   index=0 -> 남은 포인트 (String, e.g. "99")
     *   index>=1 -> 포인트 사용 기록
     */
    public int getRemainPoints(UUID gameId, Long userId) {
        String key = gameKeys.remainKey(gameId, userId);
        // index=0을 가져옴
        String remainPoint = redisTemplate.opsForList().index(key, 0);
        return Integer.parseInt(remainPoint);
    }

    public void setRemainPoint(UUID gameId, Long userId, String remain) {
        String key = gameKeys.remainKey(gameId, userId);
        redisTemplate.opsForList().set(key,0, remain);
    }

    public void addHistory(UUID gameId, Long userId, int usedPoints) {
        String key = gameKeys.remainKey(gameId, userId);
        // index 1 이후가 포인트 사용 기록
        redisTemplate.opsForList().rightPush(key, String.valueOf(usedPoints));
    }

    public List<String> getUsageHistory(UUID gameId, Long userId) {
        String key = gameKeys.remainKey(gameId, userId);
        // index 1부터 마지막까지 포인트 사용 기록만 가져옴 (index 0 은 잔여 포인트)
        return redisTemplate.opsForList().range(key, 1, -1);
    }

    // =============    Score   =============
    public int getScore(UUID gameId, Long userId) {
        String key = gameKeys.scoreKey(gameId, userId);
        String currentUserScore = redisTemplate.opsForValue().get(key);
        return (currentUserScore != null) ? Integer.parseInt(currentUserScore) : 0;
    }

    public void incrementScore(UUID gameId, Long userId) {
        String key = gameKeys.scoreKey(gameId, userId);
        redisTemplate.opsForValue().increment(key, 1);
    }

    // =============    Players =============
    public void addPlayer(UUID gameId, Long userId) {
        String key = gameKeys.playersKey(gameId);
        redisTemplate.opsForList().rightPush(key, String.valueOf(userId));
    }

    public List<String> getPlayers(UUID gameId) {
        String key = gameKeys.playersKey(gameId);
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 게임이 끝난 후 키 삭제 메서드
     *  - 해당 게임에 대한 모든 키 삭제
     */
    public void deleteKeys(UUID gameId){
        List<String> players = getPlayers(gameId);
        redisTemplate.delete(gameKeys.turnUserKey(gameId));
        for (String userId : players) {
            redisTemplate.delete(gameKeys.scoreKey(gameId, Long.valueOf(userId)));
            redisTemplate.delete(gameKeys.remainKey(gameId, Long.valueOf(userId)));
        }
        redisTemplate.delete(gameKeys.currentRoundKey(gameId));
        redisTemplate.delete(gameKeys.playersKey(gameId));
    }
}
