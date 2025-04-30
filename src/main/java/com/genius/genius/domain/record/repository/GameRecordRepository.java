package com.genius.genius.domain.record.repository;

import com.genius.genius.domain.record.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {
    Optional<GameRecord> findByGameId(UUID gameId);
}
