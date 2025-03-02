package com.genius.genius.common.config.jwt.filter;

import com.genius.genius.common.config.jwt.entity.RedisToken;
import com.genius.genius.common.config.jwt.provider.CustomUserDetails;
import com.genius.genius.common.config.jwt.provider.JwtAuthenticationToken;
import com.genius.genius.common.config.jwt.service.TokenService;
import com.genius.genius.common.config.jwt.util.JwtTokenizer;
import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.user.domain.Authority;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JWT 토큰 인증 필터 클래스
 *  - 매 요청마다 쿠키에서 토큰을 꺼내 검증
 *  - 만료가 임박하면 자동으로 새 토큰(액세스/리프레시) 발급
 */
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getAccessTokenFromCookie(request);
        if (StringUtils.hasText(token)) {
            try {
                // 토큰 파싱 및 인증객체 생성
                setupAuthentication(token);

                // 남은 유효시간 체크 -> 자동 리프레시
                if (shouldRefreshToken(token)) {
                    makeNewResponseTokens(response, token);
                }

            } catch (ExpiredJwtException e) {
                // 토큰 만료되면 쿠키에서 삭제
                Cookie invalidCookie = new Cookie("Access-Token", null);
                invalidCookie.setPath("/");
                invalidCookie.setDomain("localhost");
                invalidCookie.setHttpOnly(true);
                invalidCookie.setMaxAge(0);
                response.addCookie(invalidCookie);
                // 이때 로그아웃시킴
                throw new ApiException(ExceptionEnum.EXPIRED_TOKEN);
            } catch (UnsupportedJwtException e) {
                throw new ApiException(ExceptionEnum.UNSUPPORTED_TOKEN);
            } catch (MalformedJwtException | SignatureException e) {
                throw new ApiException(ExceptionEnum.INVALID_TOKEN);
            } catch (IllegalArgumentException e) {
                throw new ApiException(ExceptionEnum.TOKEN_NOT_FOUND);
            }
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 액세스 토큰 쿠키에서 추출
     */
    private String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("Access-Token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 토큰 기반으로 Authentication 객체 생성 -> SecurityContext에 저장
     */
    private void setupAuthentication(String token) {
        Claims claims = jwtTokenizer.parseAccessToken(token);
        String username = claims.getSubject(); // 사용자 아이디
        Long userId = claims.get("userId", Long.class); // 사용자 ID
        Authority authority = Authority.valueOf(claims.get("authority", String.class));

        Collection<? extends GrantedAuthority> authorities =
                Collections.singletonList(authority); // 단일 권한
        CustomUserDetails userDetails = new CustomUserDetails(
                username,
                /* password = */ "",
                userId,
                (List<GrantedAuthority>) authorities
        );

        // 커스텀 토큰 객체 생성
        Authentication authentication =
                new JwtAuthenticationToken(authorities, userDetails, null);

        // SecurityContextHolder 에 인증정보 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 남은 유효 시간이 전체의 1/3 이하인가?
     */
    private boolean shouldRefreshToken(String accessToken) {
        Claims claims = jwtTokenizer.parseAccessToken(accessToken);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        long remainingTime = expiration.getTime() - now.getTime();
        // 밀리초 단위
        long fullExpire = jwtTokenizer.getAccessTokenExpire();

        return remainingTime < (fullExpire / 3);
    }

    /**
     * 자동으로 새로운 토큰들(Access/Refresh) 발급 후,
     * Redis 저장 & 응답 쿠키/헤더에 담아줌
     */
    private void makeNewResponseTokens(HttpServletResponse response, String oldAccessToken) {
        Claims claims = jwtTokenizer.parseAccessToken(oldAccessToken);
        Long userId = claims.get("userId", Long.class);
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        Authority authority = Authority.valueOf(claims.get("authority", String.class));

        // 새 액세스 토큰 & 리프레시 토큰 생성
        String newAccessToken = jwtTokenizer.createAccessToken(userId, email, nickname, authority);
        String newRefreshToken = jwtTokenizer.createRefreshToken(userId, email, nickname, authority);

        // Redis 저장
        tokenService.saveOrRefresh(
                RedisToken.builder()
                        .userId(userId)
                        .refreshToken(newRefreshToken)
                        .expiration(jwtTokenizer.getRefreshTokenExpire() / 1000)
                        .build()
        );

        // 새 액세스 토큰 -> 쿠키
        Cookie accessTokenCookie = new Cookie("Access-Token", newAccessToken);
        accessTokenCookie.setHttpOnly(true); // JS 에서 접근 불가
        accessTokenCookie.setSecure(true); // HTTPS 에서만 전송
        accessTokenCookie.setPath("/");
        // 액세스 토큰 만료 시간 (초 단위)
        accessTokenCookie.setMaxAge((int) (jwtTokenizer.getAccessTokenExpire() / 1000));
        response.addCookie(accessTokenCookie);

        // 새 리프레시 토큰 -> 헤더
        response.setHeader("Refresh-Token", newRefreshToken);
    }
}
