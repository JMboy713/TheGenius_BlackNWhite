package com.genius.genius.domain.rank.entity;

import com.genius.genius.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_rank")
public class Rank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "win_count", nullable = false)
    private Long winCount = 0L;

    @Column(name = "lose_count", nullable = false)
    private Long loseCount = 0L;

    @Column(name = "win_rate", nullable = false)
    private Double winRate = (double) 0;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
