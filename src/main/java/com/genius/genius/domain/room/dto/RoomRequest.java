package com.genius.genius.domain.room.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomRequest {
    private String name;
    private String password;
    private Long user1Id;
    private Long user2Id;
}
