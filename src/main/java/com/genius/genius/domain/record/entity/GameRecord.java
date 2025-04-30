package com.genius.genius.domain.record.entity;

import com.genius.genius.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "record")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gameRecord_id", nullable = false)
    private Long gameRecordId;

    @Column(name = "game_id", nullable = false)
    private UUID gameId;

    @Column(name = "winner_record", nullable = false, length = 255)
    private String winnerRecord;


    @Column(name = "loser_record", nullable = false, length = 255)
    private String loserRecord;

    // 첫 번째 사용자와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winner", referencedColumnName = "id")
    private User winner;

    // 두 번째 사용자와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loser", referencedColumnName = "id")
    private User loser;
}