package com.genius.genius.domain.game.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UsePointsMessage {
    private UUID gameId;
    private Long userId;
    private int points;
}

