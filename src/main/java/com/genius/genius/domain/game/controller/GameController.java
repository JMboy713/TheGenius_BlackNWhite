package com.genius.genius.domain.game.controller;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.game.service.GameService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/game")
@RequiredArgsConstructor
@RestController
@Tag(name = "Game", description = "게임 API")
public class GameController {
    private final GameService gameService;
    private final UserService userService;
    private final RankService rankService;

    /**
     * 게임 시작
     * TODO redis 에서 게임 결과를 가져와야함
     * @return 현재 유저 승패기록
     */
    @PostMapping("/result")
    @Operation(summary = "게임 결과", description = "게임 결과를 확인")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 결과 확인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public CustomResponse<?> gameResult(String winner, String loser) {
        String[] winnerResult = winner.split(":");
        String[] loserResult = loser.split(":");
        if (winnerResult.length != 3 || loserResult.length != 3) {
            throw new ApiException(ExceptionEnum.INVALID_REQUEST);
        }

        // 게임 결과 저장
        gameService.save(winner, loser);
        User currentUser = userService.getCurrentUser();
        return new CustomResponse<>(HttpStatus.OK, "게임 결과 저장 성공", rankService.findByUser(currentUser));
    }
}
