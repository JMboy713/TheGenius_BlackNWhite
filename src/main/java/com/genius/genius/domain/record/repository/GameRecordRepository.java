package com.genius.genius.domain.record.repository;

import com.genius.genius.domain.record.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    Optional<GameRecord> findByGameId(UUID gameId);

    @Query("SELECT gr FROM GameRecord gr WHERE gr.winner = :userId or gr.loser = :userId")
    ArrayList<GameRecord> findByUserId(long userId);
}
