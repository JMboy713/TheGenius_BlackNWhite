package com.genius.genius.domain.room.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateRequest {
    private String name;
    private Integer password;
}
