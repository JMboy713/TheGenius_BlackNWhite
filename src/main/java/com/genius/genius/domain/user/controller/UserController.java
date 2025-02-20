package com.genius.genius.domain.user.controller;


import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.user.request.UserRegRequest;
import com.genius.genius.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/user")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "사용자 인증 API")
public class UserController {
    private final UserService userService;
    /**
     * 회원가입 처리
     *
     * @param userRegRequest 회원가입 요청 데이터
     * @param bindingResult   요청 데이터 검증 결과
     * @return 회원가입 결과
     */
    @PostMapping("/reg")
    @Operation(summary = "사용자 회원가입", description = "새로운 사용자를 등록")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 사용자")
    })
    public CustomResponse<String> reg(@RequestBody @Valid UserRegRequest userRegRequest, BindingResult bindingResult) {
        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            throw new ApiException(ExceptionEnum.NOT_ALLOW_FILED);
        }

        // 회원가입 서비스 호출
        userService.registerUser(userRegRequest);

        return new CustomResponse<>(HttpStatus.CREATED, "회원가입 성공", "회원가입이 완료되었습니다.");
    }
}
