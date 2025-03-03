package com.genius.genius.domain.room.domain;

import com.genius.genius.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

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

    private String password;

    @Column(nullable = false, name = "is_started")
    private Boolean isStarted;

    @ManyToMany
    @JoinTable(
            name = "room_users",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @ManyToMany
    @Column(nullable = true, name = "ready_users")
    private List<User> readyUsers;
}
