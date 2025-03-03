package com.genius.genius.domain.room.domain;

import com.genius.genius.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "is_started")
    private Boolean isStarted;

    @Column(nullable = false)
    private User user1;

    @Column(nullable = false)
    private User user2;


}
