package com.genius.genius.domain.game.service;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.rank.service.RankService;
import com.genius.genius.domain.record.entity.GameRecord;
import com.genius.genius.domain.record.service.GameRecordService;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRecordService gameRecordService;
    private final RankService rankService;
    private final UserService userService;

    // 스레드 풀 생성 (필요에 따라 스레드 수 조절)
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    @Override
    public void save(String winner, String loser) {
        String[] win = winner.split(":");
        String[] lose = loser.split(":");

        User loseUser = userService.findById(Long.parseLong(lose[1]))
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        User winUser = userService.findById(Long.parseLong(win[1]))
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));



        // 게임 기록 저장
        executorService.submit(() -> {
            GameRecord gameRecord = GameRecord.builder()
                    .gameId(Long.parseLong(win[0]))
                    .loser(loseUser)
                    .winner(winUser)
                    .loserRecord(lose[2])
                    .winnerRecord(win[2])
                    .build();
            gameRecordService.save(gameRecord);
        });

        // 패자 승패 승률 저장
        executorService.submit(() -> {
            Rank loseRank = rankService.findByUser(loseUser);
            long loserWinCount = loseRank.getWinCount();
            long loserLoseCount = loseRank.getLoseCount() + 1;
            loseRank.setLoseCount(loserLoseCount);
            loseRank.setWinRate((loserWinCount + loserLoseCount) > 0
                    ? (double) loserWinCount / (loserWinCount + loserLoseCount) * 100 : 0.0);
            rankService.save(loseRank);
        });

        // 승자 승패 승률 저장
        executorService.submit(() -> {
            Rank winRank = rankService.findByUser(winUser);
            long winnerWinCount = winRank.getWinCount() + 1;
            long winnerLoseCount = winRank.getLoseCount();
            winRank.setWinCount(winnerWinCount);
            winRank.setWinRate((winnerWinCount + winnerLoseCount) > 0
                    ? (double) winnerWinCount / (winnerWinCount + winnerLoseCount) * 100 : 0.0);
            rankService.save(winRank);
        });
    }
}

