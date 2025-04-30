package com.genius.genius.integration;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.config.SyncAsyncConfig;
import com.genius.genius.config.TestRedisConfig;
import com.genius.genius.domain.game.service.GameService;
import com.genius.genius.domain.game.util.GameKeyUtil;
import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.rank.repository.RankRepository;
import com.genius.genius.domain.rank.service.RankService;
import com.genius.genius.domain.record.entity.GameRecord;
import com.genius.genius.domain.record.service.GameRecordService;
import com.genius.genius.domain.user.domain.Authority;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.repository.UserRepository;
import com.genius.genius.domain.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestRedisConfig.class, SyncAsyncConfig.class})
@Rollback

class GameServiceImplIT {

    @Autowired
    GameService gameService;
    @Autowired
    UserService userService;
    @Autowired
    RankService rankService;
    @Autowired
    GameRecordService recordService;
    @Autowired
    GameKeyUtil keyUtil;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RankRepository rankRepository;

    Long userA, userB;

    @BeforeEach
    void setUp() {
        User a = userRepository.save(User.builder()
                .username("userA")
                .password(passwordEncoder.encode("a123456!"))
                .name("테스트A")
                .authority(Authority.USER)
                .isDeleted(false)
                .build());

        User b = userRepository.save(User.builder()
                .username("userB")
                .password(passwordEncoder.encode("a123456!"))
                .name("테스트B")
                .authority(Authority.USER)
                .isDeleted(false)
                .build());

        // Rank 도 직접 넣어 줘야 함
        rankRepository.save(new Rank(null, 0L, 0L, 0.0, a));
        rankRepository.save(new Rank(null, 0L, 0L, 0.0, b));

        userA = a.getId();
        userB = b.getId();
    }


    @Test
    @DisplayName("게임 진행 — 한명이 먼저 5승")
    void point5Win() {
        UUID gameId = UUID.randomUUID();
        gameService.initGame(gameId, List.of(userA, userB));

        // 라운드 1 : userA(20) vs userB(10) -> userA 승
        gameService.usePoints(gameId, userA, 20);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 2 : userA(30) vs userB(80) -> userB 승
        gameService.usePoints(gameId, userA, 30);
        gameService.usePoints(gameId, userB, 80);

        // 라운드 3 : userA(20) vs userB(1) -> userA 승
        gameService.usePoints(gameId, userA, 20);
        gameService.usePoints(gameId, userB, 1);

        // 라운드 4 : userA(10) vs userB(2) -> userA 승
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 2);

        // 라운드 5 : userA(10) vs userB(3) -> userA 승
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 3);

        // 라운드 6 : userA(9) vs userB( 0) -> userA 승, 5점 선취
        gameService.usePoints(gameId, userA, 9);
        gameService.usePoints(gameId, userB, 0);

        assertThat(gameService.isEndGame(gameId)).isTrue();

        gameService.save(gameId);      // async → 동기로 설정돼 있어 바로 끝남

        // 결과 검증
        Rank userARank = rankService.findByUser(userService.findById(userA).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)));
        Rank userBRank   = rankService.findByUser(userService.findById(userB).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)));

        // 승 : userA, 패 : userB 랭크 집계 확인
        assertThat(userARank.getWinCount()).isEqualTo(1);
        assertThat(userBRank.getLoseCount()).isEqualTo(1);

        GameRecord rec = recordService.findByGameId(gameId);
        assertThat(rec.getWinner().getId()).isEqualTo(userA);
        assertThat(rec.getLoser().getId()).isEqualTo(userB);

        // /result 호출 됐다고 가정
        keyUtil.deleteKeys(gameId);

        // Redis 키가 정리됐는지도 확인
        assertThat(keyUtil.getPlayers(gameId)).isEmpty();
    }

    @Test
    @DisplayName("게임 진행 — 5점 넘어서 승")
    void point7Win() {
        UUID gameId = UUID.randomUUID();
        gameService.initGame(gameId, List.of(userA, userB));

        // 라운드 1 : userA(10) vs userB(10) -> 비김 (1 : 1)
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 2 : userA(10) vs userB(10) -> 비김 (2 : 2)
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 3 : userA(10) vs userB(10) -> 비김 (3 : 3)
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 4 : userA(10) vs userB(10) -> 비김 (4 : 4)
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 5 : userA(10) vs userB(10) -> 비김 (5 : 5)
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 6 : userA(10) vs userB(10) -> 비김 (6 : 6)
        gameService.usePoints(gameId, userA, 10);
        gameService.usePoints(gameId, userB, 10);

        // 라운드 7 : userA(11) vs userB(10) -> userA 승 (7 : 5)
        gameService.usePoints(gameId, userA, 11);
        gameService.usePoints(gameId, userB, 10);

        assertThat(gameService.isEndGame(gameId)).isTrue();

        gameService.save(gameId);      // async → 동기로 설정돼 있어 바로 끝남

        // 결과 검증
        Rank userARank = rankService.findByUser(userService.findById(userA).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)));
        Rank userBRank   = rankService.findByUser(userService.findById(userB).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND)));

        // 승 : userA, 패 : userB 랭크 집계 확인
        assertThat(userARank.getWinCount()).isEqualTo(1);
        assertThat(userBRank.getLoseCount()).isEqualTo(1);

        GameRecord rec = recordService.findByGameId(gameId);
        assertThat(rec.getWinner().getId()).isEqualTo(userA);
        assertThat(rec.getLoser().getId()).isEqualTo(userB);

        // /result 호출 됐다고 가정
        keyUtil.deleteKeys(gameId);

        // Redis 키가 정리됐는지도 확인
        assertThat(keyUtil.getPlayers(gameId)).isEmpty();
    }
}
