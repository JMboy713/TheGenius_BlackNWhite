package com.genius.genius.domain.game.service;

import java.util.List;
import java.util.UUID;

public interface GameService {
    void save(UUID gameId);
    void initGame(UUID gameId, List<Long> players);
    Long usePoints(UUID gameId, Long userId, int used);
    boolean isEndGame(UUID gameId);
    int getPointRange(UUID gameId, Long userId);
}
