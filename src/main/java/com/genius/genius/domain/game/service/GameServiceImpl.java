package com.genius.genius.domain.game.service;

import com.genius.genius.common.config.async.service.AsyncService;
import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.game.util.GameKeyUtil;
import com.genius.genius.domain.game.util.GameKeys;
import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.rank.service.RankService;
import com.genius.genius.domain.record.entity.GameRecord;
import com.genius.genius.domain.record.service.GameRecordService;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRecordService gameRecordService;
    private final RankService rankService;
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;
    private final GameKeys gameKeys;
    private final GameKeyUtil gameKeyUtil;
    private final AsyncService asyncService;

    @Override
    @Transactional
    public void save(UUID gameId) {
        Queue<String[]> rankQueue = gameResult(gameId);

        User winUser = userService.findById(Long.parseLong(rankQueue.poll()[1]))
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));
        User loseUser = userService.findById(Long.parseLong(rankQueue.poll()[1]))
                .orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        if (gameRecordService.findByGameId(gameId) == null) {
            // 게임 기록 저장
            GameRecord gameRecord = GameRecord.builder()
                    .gameId(gameId)
                    .loser(loseUser)
                    .winner(winUser)
                    .loserRecord(gameKeyUtil.getUsageHistory(gameId, loseUser.getId()).toString())
                    .winnerRecord(gameKeyUtil.getUsageHistory(gameId, winUser.getId()).toString())
                    .build();
            asyncService.saveRecord(gameRecord);


            // 패자 승패 승률 저장
            Rank loseRank = rankService.findByUser(loseUser);
            long loserWinCount = loseRank.getWinCount();
            long loserLoseCount = loseRank.getLoseCount() + 1;
            loseRank.setLoseCount(loserLoseCount);
            loseRank.setWinRate((loserWinCount + loserLoseCount) > 0
                    ? (double) loserWinCount / (loserWinCount + loserLoseCount) * 100 : 0.0);
            asyncService.updateRank(loseRank);


            // 승자 승패 승률 저장
            Rank winRank = rankService.findByUser(winUser);
            long winnerWinCount = winRank.getWinCount() + 1;
            long winnerLoseCount = winRank.getLoseCount();
            winRank.setWinCount(winnerWinCount);
            winRank.setWinRate((winnerWinCount + winnerLoseCount) > 0
                    ? (double) winnerWinCount / (winnerWinCount + winnerLoseCount) * 100 : 0.0);
            asyncService.updateRank(winRank);

        }
    }

    /**
     * 게임시작 전 설정:
     *  1) 라운드 1
     *  2) 게임 참여자 설정
     *  3) 각 유저 잔여포인트 99, 승점 0점
     *  4) 순서 설정 (players 는 순서대로 [선공,후공])
     */
    @Override
    public void initGame(UUID gameId, List<Long> players) {
        // 라운드 1로 설정
        redisTemplate.opsForValue().set(gameKeys.currentRoundKey(gameId), "1");

        // 각 유저들에게
        for (Long userId : players) {
            // 잔여 포인트 99 설정
            redisTemplate.opsForList().rightPush(gameKeys.remainKey(gameId, userId), "99");

            // 게임 참여자 설정
            redisTemplate.opsForList().rightPush(gameKeys.playersKey(gameId), userId.toString());

            // 승점 설정
            redisTemplate.opsForValue().set(gameKeys.scoreKey(gameId, userId), "0");
        }

        // 순서 설정
        redisTemplate.opsForValue().set(gameKeys.turnUserKey(gameId), players.get(0).toString());
    }

    /**
     * 포인트를 사용:
     * 1) index=0 에서 '남은 포인트'를 가져와서 차감
     * 2) LSET( index=0 ) 로 남은 포인트 갱신
     * 3) RPUSH 로 '사용 포인트' 기록을 추가 (인덱스 1,2,...)
     *
     * @return -1L 이면 라운드가 끝나지 않음, 0이면 비김, 이외는 이긴 userId
     */
    @Override
    public Long usePoints(UUID gameId, Long userId, int used) {
        synchronized (("lock:" + gameId).intern()) {
            if (Long.parseLong(gameKeyUtil.getTurnUser(gameId)) != userId) {
                throw new ApiException(ExceptionEnum.NOT_YOUR_TURN);
            }

            // 1) 현재 남은 포인트 가져오기
            int remain = gameKeyUtil.getRemainPoints(gameId, userId);
            int updated = remain - used;
            if (updated < 0) {
                // 예외 처리 (포인트 부족)
                throw new ApiException(ExceptionEnum.NOT_ENOUGH_POINTS);
            }

            // 2) 갱신된 남은 포인트 반영
            gameKeyUtil.setRemainPoint(gameId, userId, String.valueOf(updated));

            // 3) 사용 기록을 리스트 끝에 추가 (오른쪽)
            gameKeyUtil.addHistory(gameId, userId, used);

            String turnUser = gameKeyUtil.getTurnUser(gameId);
            List<String> players = gameKeyUtil.getPlayers(gameId);
            int index = players.indexOf(turnUser);

            // 결과
            Long result = -1L;
            // 순서가 마지막 유저가 플레이 하면
            if (index == 1) result = roundResult(gameId);

            // 순서 넘김
            gameKeyUtil.setTurnUser(gameId, players.get((index + 1) % players.size()));

            return result;
        }
    }

    // 남은 포인트별 단계 표시
    @Override
    public int getPointRange(UUID gameId, Long userId) {
        int remainPoint = gameKeyUtil.getRemainPoints(gameId, userId);
        if (remainPoint >= 81) return 5;
        if (remainPoint >= 61) return 4;
        if (remainPoint >= 41) return 3;
        if (remainPoint >= 21) return 2;
        return 1;
    }

    /**
     * 게임이 끝났는지 체크
     * 1) 먼저 5점 획득한 플레이어가 존재
     * 2) 둘다 5점 이상일 때 승부가 났을 경우
     * @param gameId 현재 게임Id
     * @return 게임이 끝났으면 true 아니면 false
     */
    @Override
    public boolean isEndGame(UUID gameId) {
        List<String> players = gameKeyUtil.getPlayers(gameId);
        if (players.size() != 2) return false;

        long player1 = Long.parseLong(players.get(0));
        long player2 = Long.parseLong(players.get(1));

        int score1 = gameKeyUtil.getScore(gameId, player1);
        int score2 = gameKeyUtil.getScore(gameId, player2);

        int max = Math.max(score1, score2);
        int min = Math.min(score1, score2);

        // 1) 한쪽이 5점 찍고 상대는 아직 5점 못 찍음 -> 즉시 종료
        if (max >= 5 && min < 5) {
            return true;
        }

        // 2) 둘 다 5점 이상인데 더 이상 동점이 아님 -> 차이 나는 순간 종료
        if (score1 >= 5 && score2 >= 5 && score1 != score2) {
            return true;
        }

        // 나머지는 계속 진행
        return false;
    }

    /**
     * 라운드 결과
     *  1) 현재 라운드, 참가 유저, 이번 라운드 포인트 내역을 가져옴
     *  2) 이긴 플레이어 점수 + 1, 비겼을 경우 모두 + 1
     *  3) 라운드 증가
     *
     *  @return  비기면 0, 아니면 이긴 userId
     */
    private Long roundResult(UUID gameId) {
        String playersKey = gameKeys.playersKey(gameId);
        String currentRoundKey = gameKeys.currentRoundKey(gameId);
        String currentRound = redisTemplate.opsForValue().get(currentRoundKey);
        List<String> players = redisTemplate.opsForList().range(playersKey, 0, -1);
        List<String> scores = new ArrayList<>();
        for (String userId : players) {
            String remainKey = gameKeys.remainKey(gameId,Long.parseLong(userId));
            scores.add(redisTemplate.opsForList().index(remainKey, Long.parseLong(currentRound)));
        }

        int firstPlayerScore = Integer.parseInt(scores.get(0));
        Long firstPlayer = Long.valueOf(players.get(0));
        int secondPlayerScore = Integer.parseInt(scores.get(1));
        Long secondPlayer = Long.valueOf(players.get(1));

        Long winner = 0L;
        // 선공이 이겼을 경우
        if (firstPlayerScore > secondPlayerScore) {
            gameKeyUtil.incrementScore(gameId,firstPlayer); // 선공 플레이어 점수 + 1
            winner = firstPlayer;
        } else if (firstPlayerScore < secondPlayerScore) { // 후공이 이겼을경우
            gameKeyUtil.incrementScore(gameId,secondPlayer); // 후공 플레이어 점수 + 1
            winner = secondPlayer;
        } else { // 비겼을 경우 모두 +1
            gameKeyUtil.incrementScore(gameId,firstPlayer);
            gameKeyUtil.incrementScore(gameId,secondPlayer);
        }

        // 라운드 증가
        gameKeyUtil.setCurrentRound(gameId, Integer.parseInt(currentRound) + 1);

        return winner;
    }

    private Queue<String[]> gameResult(UUID gameId) {
        List<String> players = gameKeyUtil.getPlayers(gameId);
        Queue<String[]> winnerQueue = new PriorityQueue<>(
                Comparator.<String[]>comparingInt(o -> Integer.parseInt(o[0])).reversed()
        );
        for (String userId : players) {
            winnerQueue.add(new String[] {String.valueOf(gameKeyUtil.getScore(gameId, Long.valueOf(userId))),userId});
        }
        return winnerQueue;
    }
}