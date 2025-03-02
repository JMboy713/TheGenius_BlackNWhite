package com.genius.genius.common.config.jwt.util;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.user.domain.Authority;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.SignatureException;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스
 */
/**
 * JWT 토큰을 생성하고 검증하는 유틸리티 클래스
 */
@Component
public class JwtTokenizer {

    @Value("${LOC_JWT_ACCESS_SECRET}")
    private String accessTokenSecretBase64;

    @Value("${LOC_JWT_REFRESH_SECRET}")
    private String refreshTokenSecretBase64;

    /**
     * 액세스 토큰 만료 시간을 반환 (밀리초 단위)
     *
     * @return 액세스 토큰 만료 시간
     */
    @Getter
    @Value("${LOC_JWT_ACCESS_EXPIRE}")
    private long accessTokenExpire;

    @Getter
    @Value("${LOC_JWT_REFRESH_EXPIRE}")
    private long refreshTokenExpire;

    private byte[] accessSecret;
    private byte[] refreshSecret;

    /**
     * 초기화 메서드. Base64로 인코딩된 비밀키를 디코딩하여 바이트 배열로 변환
     */
    @PostConstruct
    public void init() {
        accessSecret = Base64.getDecoder().decode(accessTokenSecretBase64);
        refreshSecret = Base64.getDecoder().decode(refreshTokenSecretBase64);
    }

    /**
     * 액세스 토큰을 생성
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param authority 사용자 권한
     * @return 생성된 액세스 토큰
     */
    public String createAccessToken(Long id, String email, String nickname, Authority authority) {
        return createToken(id, email, nickname, authority, accessTokenExpire, accessSecret);
    }

    /**
     * 리프레시 토큰을 생성
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param authority 사용자 권한
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(Long id, String email, String nickname, Authority authority) {
        return createToken(id, email, nickname, authority, refreshTokenExpire, refreshSecret);
    }

    /**
     * JWT 토큰을 생성
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param nickname 사용자 닉네임
     * @param authority 사용자 권한
     * @param expire 토큰 만료 시간 (밀리초)
     * @param secretKey 비밀키
     * @return 생성된 JWT 토큰
     */
    private String createToken(Long id, String email, String nickname, Authority authority, Long expire, byte[] secretKey) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("authority", authority);
        claims.put("userId", id);
        claims.put("nickname", nickname);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expire))
                .signWith(getSigningKey(secretKey))
                .compact();
    }

    /**
     * 액세스 토큰을 파싱하여 클레임을 반환
     *
     * @param accessToken 액세스 토큰
     * @return 토큰에서 추출된 클레임
     */
    public Claims parseAccessToken(String accessToken) {
        return parseToken(accessToken, accessSecret);
    }

    /**
     * 리프레시 토큰을 파싱하여 클레임을 반환
     *
     * @param refreshToken 리프레시 토큰
     * @return 토큰에서 추출된 클레임
     */
    public Claims parseRefreshToken(String refreshToken) {
        return parseToken(refreshToken, refreshSecret);
    }

    /**
     * JWT 토큰을 파싱하여 클레임을 반환
     *
     * @param token JWT 토큰
     * @param secretKey 비밀키
     * @return 토큰에서 추출된 클레임
     */
    public Claims parseToken(String token, byte[] secretKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
        catch (UnsupportedJwtException e) {
            throw new ApiException(ExceptionEnum.UNSUPPORTED_TOKEN);
        } catch (MalformedJwtException e) {
            throw new ApiException(ExceptionEnum.INVALID_TOKEN);
        } catch (SignatureException e) {
            throw new ApiException(ExceptionEnum.INVALID_SIGNATURE);
        } catch (IllegalArgumentException e) {
            throw new ApiException(ExceptionEnum.ILLEGAL_ARGUMENT);
        }
    }

    /**
     * 비밀키를 HMAC SHA로 변환하여 반환
     *
     * @param secretKey 비밀키
     * @return 변환된 시크릿 키
     */
    public static Key getSigningKey(byte[] secretKey) {
        return Keys.hmacShaKeyFor(secretKey);
    }
}
