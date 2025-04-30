package com.genius.genius.domain.game.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GameInitMessage {
    UUID gameId;
    List<Long> players;
}
