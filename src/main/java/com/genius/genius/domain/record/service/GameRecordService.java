package com.genius.genius.domain.record.service;

import com.genius.genius.domain.record.dto.DetailRecord;
import com.genius.genius.domain.record.entity.GameRecord;

import java.util.ArrayList;
import java.util.UUID;

public interface GameRecordService {
    void save(GameRecord record);

    GameRecord findByGameId(UUID gameId);

    ArrayList<DetailRecord> detailRecord(long userId); // 유저의 각 게임과 라운드별 기록을 보여줌
}
