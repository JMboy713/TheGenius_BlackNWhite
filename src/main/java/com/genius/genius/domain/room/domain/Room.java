package com.genius.genius.domain.room.domain;

import com.genius.genius.domain.room.dto.UserDTO;
import com.genius.genius.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.stream.Collectors;

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
    private Set<User> readyUsers;

    // User -> UserDTO 변환 메서드 추가
    public Set<UserDTO> getUserDTOs(Set<User> users) {
        return this.users.stream().map(UserDTO::new).collect(Collectors.toSet());
    }

    public Set<UserDTO> getReadyUserDTOs(Set<User> readyUsers) {
        if (readyUsers == null) return Set.of() ;
        return this.readyUsers.stream().map(UserDTO::new).collect(Collectors.toSet());
    }

}
