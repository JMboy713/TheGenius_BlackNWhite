package com.genius.genius.unit;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.game.service.GameServiceImpl;
import com.genius.genius.domain.game.util.GameKeyUtil;
import com.genius.genius.domain.game.util.GameKeys;
import com.genius.genius.domain.rank.service.RankService;
import com.genius.genius.domain.record.service.GameRecordService;
import com.genius.genius.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 단위 테스트 : GameServiceImpl
 * - RedisTemplate 과 외부 서비스는 Mockito 로 Mock 처리
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GameServiceImplTest {

    @Mock GameRecordService gameRecordService;
    @Mock RankService rankService;
    @Mock UserService userService;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock GameKeys gameKeys;
    @Mock GameKeyUtil gameKeyUtil;

    @Mock ValueOperations<String, String> valueOps;
    @Mock ListOperations<String, String> listOps;

    @InjectMocks
    GameServiceImpl sut;

    private final UUID gameId = UUID.randomUUID();
    private final Long userA = 1L;
    private final Long userB = 2L;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForList()).thenReturn(listOps);

        // 기본 키 포맷 Stubs
        when(gameKeys.currentRoundKey(gameId)).thenReturn("game:"+gameId+":round");
        when(gameKeys.remainKey(gameId,userA)).thenReturn("game:"+gameId+":user:"+userA);
        when(gameKeys.remainKey(gameId,userB)).thenReturn("game:"+gameId+":user:"+userB);
        when(gameKeys.playersKey(gameId)).thenReturn("game:"+gameId+":players");
        when(gameKeys.scoreKey(gameId,userA)).thenReturn("game:"+gameId+":score:"+userA);
        when(gameKeys.scoreKey(gameId,userB)).thenReturn("game:"+gameId+":score:"+userB);
        when(gameKeys.turnUserKey(gameId)).thenReturn("game:"+gameId+":turn");
    }

    @Test
    @DisplayName("initGame(): Redis 초기값이 정확히 저장된다")
    void initGame() {
        sut.initGame(gameId, List.of(userA, userB));

        // round=1
        verify(valueOps).set("game:"+gameId+":round", "1");
        // players push
        verify(listOps).rightPush("game:"+gameId+":players", String.valueOf(userA));
        verify(listOps).rightPush("game:"+gameId+":players", String.valueOf(userB));
        // remain 99 push
        verify(listOps).rightPush("game:"+gameId+":user:"+userA, "99");
        verify(listOps).rightPush("game:"+gameId+":user:"+userB, "99");
        // score 0
        verify(valueOps).set("game:"+gameId+":score:"+userA, "0");
        verify(valueOps).set("game:"+gameId+":score:"+userB, "0");
        // turn user
        verify(valueOps).set("game:"+gameId+":turn", String.valueOf(userA));
    }

    @Nested
    class UsePoints {
        @Test
        @DisplayName("차례가 아닌 사용자가 호출하면 예외")
        void notYourTurn() {
            when(gameKeyUtil.getTurnUser(gameId)).thenReturn(String.valueOf(userB));
            assertThatThrownBy(() -> sut.usePoints(gameId, userA, 10))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining(ExceptionEnum.NOT_YOUR_TURN.getMessage());
        }

        @Test
        @DisplayName("포인트 부족 예외")
        void notEnoughPoints() {
            when(gameKeyUtil.getTurnUser(gameId)).thenReturn(String.valueOf(userA));
            when(gameKeyUtil.getRemainPoints(gameId, userA)).thenReturn(5);
            assertThatThrownBy(() -> sut.usePoints(gameId, userA, 10))
                    .isInstanceOf(ApiException.class)
                    .hasMessageContaining(ExceptionEnum.NOT_ENOUGH_POINTS.getMessage());
        }

        @Test
        @DisplayName("정상 차감 및 기록")
        void happyPath() {
            when(gameKeyUtil.getTurnUser(gameId)).thenReturn(String.valueOf(userA));
            when(gameKeyUtil.getRemainPoints(gameId, userA)).thenReturn(50);
            when(gameKeyUtil.getPlayers(gameId)).thenReturn(Arrays.asList(String.valueOf(userA), String.valueOf(userB)));

            Long result = sut.usePoints(gameId, userA, 20);

            assertThat(result).isEqualTo(-1L);
            // 남은 포인트 30으로 저장
            verify(gameKeyUtil).setRemainPoint(gameId, userA, "30");
            // 히스토리 push
            verify(gameKeyUtil).addHistory(gameId, userA, 20);
            // 턴 넘김 -> userB
            verify(gameKeyUtil).setTurnUser(gameId, String.valueOf(userB));
        }
    }

    @Test
    @DisplayName("getPointRange(): 단계 계산 검증")
    void pointRange() {
        when(gameKeyUtil.getRemainPoints(gameId,userA)).thenReturn(85, 65, 45, 25, 10);

        assertThat(sut.getPointRange(gameId,userA)).isEqualTo(5);
        assertThat(sut.getPointRange(gameId,userA)).isEqualTo(4);
        assertThat(sut.getPointRange(gameId,userA)).isEqualTo(3);
        assertThat(sut.getPointRange(gameId,userA)).isEqualTo(2);
        assertThat(sut.getPointRange(gameId,userA)).isEqualTo(1);
    }
}