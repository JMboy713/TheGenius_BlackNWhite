package com.genius.genius.common.config.async.service;

import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.rank.service.RankService;
import com.genius.genius.domain.record.entity.GameRecord;
import com.genius.genius.domain.record.service.GameRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncService {
    private final GameRecordService recordService;
    private final RankService rankService;

    @Async("gameExecutor")
    public void saveRecord(GameRecord record) {
        recordService.save(record);
    }

    @Async("gameExecutor")
    public void updateRank(Rank rank) {
        rankService.save(rank);
    }
}
