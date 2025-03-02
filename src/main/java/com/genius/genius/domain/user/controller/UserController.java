package com.genius.genius.domain.user.controller;


import com.genius.genius.common.config.jwt.entity.RedisToken;
import com.genius.genius.common.config.jwt.service.TokenService;
import com.genius.genius.common.config.jwt.util.JwtTokenizer;
import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.common.response.CustomResponse;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.request.UserLoginRequest;
import com.genius.genius.domain.user.request.UserRegRequest;
import com.genius.genius.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;
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
    public CustomResponse<Void> reg(@RequestBody @Valid UserRegRequest userRegRequest, BindingResult bindingResult) {
        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            throw new ApiException(ExceptionEnum.NOT_ALLOW_FILED);
        }

        // 비밀번호 2개가 일치하는지 확인
        if (!userRegRequest.getPassword().equals(userRegRequest.getPasswordCheck())) {
            throw new ApiException(ExceptionEnum.PASSWORD_CONFIRMATION_MISMATCH);
        }
        // 회원가입 서비스 호출
        userService.registerUser(userRegRequest);

        return new CustomResponse<>(HttpStatus.CREATED, "회원가입 성공", null);
    }

    /**
     * 사용자를 로그인하고 JWT 토큰을 생성
     *
     * @param userLoginRequest 로그인 요청
     * @param bindingResult    요청 데이터 검증
     * @param response         HTTP 응답 객체
     * @return 로그인 결과
     */
    @PostMapping("/login")
    @Operation(summary = "사용자 로그인", description = "사용자를 인증하고 JWT 토큰을 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "사용자 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "비밀번호 불일치")
    })
    public CustomResponse<String> login(@RequestBody @Valid UserLoginRequest userLoginRequest, BindingResult bindingResult, HttpServletResponse response) {

        // 필드 에러 확인
        if (bindingResult.hasErrors()) {
            throw new ApiException(ExceptionEnum.NOT_ALLOW_FILED);
        }
        User user = userService.findByUsername(userLoginRequest.getUsername()).orElseThrow(() -> new ApiException(ExceptionEnum.USER_NOT_FOUND));

        // 비밀번호 일치여부 체크
        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new ApiException(ExceptionEnum.DIFFERENT_PASSWORD);
        }

        // 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtTokenizer.createAccessToken(user.getId(), user.getUsername(), user.getName(), user.getAuthority());
        String refreshToken = jwtTokenizer.createRefreshToken(user.getId(), user.getUsername(), user.getName(), user.getAuthority());

        // 리프레시 토큰 저장
        tokenService.saveOrRefresh(new RedisToken(user.getId(), refreshToken, tokenService.calTimeout()));

        // 액세스 토큰을 쿠키에 추가
        Cookie accessTokenCookie = new Cookie("Access-Token", accessToken);
        accessTokenCookie.setPath("/"); // 모든 경로
        accessTokenCookie.setHttpOnly(true); // JS 에서 쿠키 접근 불가
//        accessTokenCookie.setSecure(true); // HTTPS 에서만 쿠키 전송
        accessTokenCookie.setMaxAge((int) jwtTokenizer.getAccessTokenExpire()); // 액세스 토큰 만료 시간 설정
        response.addCookie(accessTokenCookie);

        // 리프레시 토큰을 응답 헤더에 추가
        response.setHeader("Refresh-Token", refreshToken);
        return new CustomResponse<>(HttpStatus.OK, "로그인 성공", null);
    }
}
