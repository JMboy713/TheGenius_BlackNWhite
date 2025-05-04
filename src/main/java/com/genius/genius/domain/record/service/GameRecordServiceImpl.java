package com.genius.genius.domain.record.service;

import com.genius.genius.domain.record.dto.DetailRecord;
import com.genius.genius.domain.record.entity.GameRecord;
import com.genius.genius.domain.record.repository.GameRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameRecordServiceImpl implements GameRecordService {
    private final GameRecordRepository gameRecordRepository;
    @Override
    public void save(GameRecord gameRecord) {
        gameRecordRepository.save(gameRecord);
    }

    // 이거 바꿔야댐 gameId로 찾도록
    @Override
    public GameRecord findByGameId(UUID gameId) {
        return gameRecordRepository.findByGameId(gameId).orElse(null);
    }

    @Override
    public ArrayList<DetailRecord> detailRecord(long userId) {
        ArrayList<DetailRecord> detailRecords = new ArrayList<>();
        ArrayList<GameRecord> gameRecordOptional = gameRecordRepository.findByUserId(userId);
        if (gameRecordOptional.size() > 0) {
            for (GameRecord gameRecord : gameRecordOptional) {

                ArrayList<Integer> record1 = Arrays.stream(gameRecord.getWinnerRecord().split(","))
                        .map(Integer::parseInt)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                ArrayList<Integer> record2 = Arrays.stream(gameRecord.getLoserRecord().split(","))
                        .map(Integer::parseInt)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

                ArrayList<Long> record3 = new ArrayList<>();

                for (int i = 0; i < record1.size(); i++) {
                    if (record1.get(i) > record2.get(i)) {
                        record3.add(gameRecord.getWinner().getId());
                    } else if (record1.get(i) < record2.get(i)) {
                        record3.add(gameRecord.getLoser().getId());
                    } else
                        record3.add(0L);
                }
                DetailRecord detailRecord = null;
                if (gameRecord.getWinner().getId() == userId) {
                     detailRecord = DetailRecord.builder()
                            .myId(userId)
                            .opponentId(gameRecord.getLoser().getId())
                            .myRecord(record1)
                            .opponentRecord(record2)
                            .winnerId(record3)
                            .build();
                } else if (gameRecord.getLoser().getId() == userId) {
                    detailRecord = DetailRecord.builder()
                            .myId(userId)
                            .opponentId(gameRecord.getWinner().getId())
                            .myRecord(record2)
                            .opponentRecord(record1)
                            .winnerId(record3)
                            .build();
                }
                detailRecords.add(detailRecord);
            }
        }
        return detailRecords;
    }

    /**
     * TODO 게임 마무리 후 결과 기록해야됨
     */
}
