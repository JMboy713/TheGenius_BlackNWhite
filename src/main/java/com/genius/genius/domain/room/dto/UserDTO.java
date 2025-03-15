package com.genius.genius.domain.room.dto;

import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.user.domain.User;
import lombok.Getter;

@Getter
public class UserDTO {
    private Long id;
    private String name;
    private String userName;
    private Rank Rank;

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.userName = user.getUsername();
        this.Rank = user.getRank();
    }

}
