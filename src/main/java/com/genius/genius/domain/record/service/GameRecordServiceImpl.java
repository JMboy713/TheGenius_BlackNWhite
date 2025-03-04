package com.genius.genius.domain.record.service;

import com.genius.genius.domain.record.entity.GameRecord;
import com.genius.genius.domain.record.repository.GameRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameRecordServiceImpl implements GameRecordService {
    private final GameRecordRepository gameRecordRepository;
    @Override
    public void save(GameRecord gameRecord) {
        gameRecordRepository.save(gameRecord);
    }

    /**
     * TODO 게임 마무리 후 결과 기록해야됨
     */
}
