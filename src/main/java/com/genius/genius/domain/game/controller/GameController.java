package com.genius.genius.domain.game.controller;

import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.game.dto.GameStateResponse;
import com.genius.genius.domain.game.dto.GameInitMessage;
import com.genius.genius.domain.game.dto.UsePointsMessage;
import com.genius.genius.domain.game.service.GameService;
import com.genius.genius.domain.game.util.GameKeyUtil;
import com.genius.genius.domain.rank.entity.Rank;
import com.genius.genius.domain.rank.service.RankService;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@Tag(name = "Game", description = "게임 API")
public class GameController {
    private final GameService gameService;
    private final UserService userService;
    private final RankService rankService;
    private final GameKeyUtil gameKeyUtil;
    private final SimpMessagingTemplate messagingTemplate;

    /**
      * 게임 생성 – 서버가 UUID를 발급해 초기화까지 수행
      */
    @PostMapping("/create")
    @Operation(summary = "게임 생성", description = "게임 생성 후 초기 설정")
    public CustomResponse<UUID> createGame(@RequestBody List<Long> players) {
        UUID gameId = UUID.randomUUID();
        gameService.initGame(gameId, players);
        return new CustomResponse<>(HttpStatus.CREATED, "게임 생성", gameId);
    }

    @MessageMapping("/usePoints")
    public void usePoints(UsePointsMessage msg) {
        // 1) 포인트 차감, 기록, 라운드 결과
        Long roundResult = gameService.usePoints(msg.getGameId(), msg.getUserId(), msg.getPoints());

        // 2) 남은 포인트 단계
        int remain = gameService.getPointRange(msg.getGameId(), msg.getUserId());

        // 3) 브로드캐스트
        GameStateResponse response = new GameStateResponse(msg.getGameId(), msg.getUserId(), remain, roundResult,gameService.isEndGame(msg.getGameId()));

        messagingTemplate.convertAndSend("/topic/game/" + msg.getGameId(), response);
    }

    /**
     * 게임 결과
     * @return 현재 유저 승패기록
     */
    @PostMapping("/result")
    @Operation(summary = "게임 결과", description = "게임 결과 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 결과 확인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public CustomResponse<Rank> gameResult(@RequestParam("gameId") UUID gameId) {
        // 끝났다면 게임 결과 저장
        gameService.save(gameId);
        User currentUser = userService.getCurrentUser();
        gameKeyUtil.deleteKeys(gameId);
        return new CustomResponse<>(HttpStatus.OK, "게임 결과 저장 성공", rankService.findByUser(currentUser));
    }
}