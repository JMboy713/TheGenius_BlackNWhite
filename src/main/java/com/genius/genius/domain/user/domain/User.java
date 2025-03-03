package com.genius.genius.domain.user.domain;

import com.genius.genius.common.entity.BaseEntity;
import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.record.entity.GameRecord;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, name = "is_deleted")
    private boolean isDeleted;

    @Column(nullable = false, name = "authority")
    @Enumerated(EnumType.STRING)
    private Authority authority;

    @OneToOne(mappedBy = "user")
    private Rank rank;

    // User가 첫 번째 사용자로 참여한 Record 들
    @OneToMany(mappedBy = "winner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameRecord> recordsAsUser1 = new ArrayList<>();

    // User가 두 번째 사용자로 참여한 Record 들
    @OneToMany(mappedBy = "loser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameRecord> recordsAsUser2 = new ArrayList<>();
}