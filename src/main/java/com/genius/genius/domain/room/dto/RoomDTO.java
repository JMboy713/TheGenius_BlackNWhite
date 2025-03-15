package com.genius.genius.domain.room.dto;

import com.genius.genius.domain.room.domain.Room;
import lombok.Getter;

import java.util.Set;

@Getter
public class RoomDTO {
    private long id;

    private String name;

    private String password;

    private Boolean isStarted;

    private Set<UserDTO> users;

    private Set<UserDTO> readyUsers;



    public RoomDTO(Room room, Set<UserDTO> users, Set<UserDTO> readyUsers) {
        this.id   = room.getId();
        this.name = room.getName();
        this.password  = room.getPassword();
        this.isStarted = room.getIsStarted();
        this.users = users;
        this.readyUsers = readyUsers;
    }
}