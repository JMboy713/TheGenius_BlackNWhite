package com.genius.genius.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GameStateResponse {
    private UUID gameId;
    private Long userId;
    private int remain;
    private Long winner;
    private boolean isEnd;
}

