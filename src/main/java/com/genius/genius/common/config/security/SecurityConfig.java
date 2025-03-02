package com.genius.genius.common.config.security;

import com.genius.genius.common.config.jwt.filter.TokenAuthenticationFilter;
import com.genius.genius.common.config.jwt.service.TokenService;
import com.genius.genius.common.config.jwt.util.JwtTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenizer jwtTokenizer;
    private final TokenService tokenService;
    // 모든 유저 허용 페이지
    String[] allAllowPage = new String[]{
            "/api/v1/", // 메인페이지
            "/api/v1/error", // 에러페이지
            "/api/v1/user/login", // 로그인 페이지
            "/api/v1/user/reg", // 회원가입 페이지

    };

    // swagger
    String[] swaggerAllowPage = new String[]{
            "/swagger-ui/**", // Swagger UI
            "/v3/api-docs/**", // Swagger API docs
            "/swagger-resources/**", // Swagger resources
            "/swagger-ui.html", // Swagger HTML
            "/webjars/**",// Webjars for Swagger
            "/swagger/**"// Swagger try it out
    };

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 유저별 페이지 접근 허용
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(allAllowPage).permitAll() // 모든 유저
                .requestMatchers(swaggerAllowPage).permitAll()
                .anyRequest().authenticated()
        );

        // 세션 관리 Stateless 설정(서버가 클라이언트 상태 저장x)
        http.sessionManagement(auth -> auth.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // CSRF 비활성화
        http.csrf(csrf -> csrf.disable());

        // 로그인 폼 비활성화
        http.formLogin(auth -> auth.disable());

        // http 기본 인증(헤더) 비활성화
        http.httpBasic(auth -> auth.disable());

        //jwt 필터 검사 인증
        http.addFilterBefore(new TokenAuthenticationFilter(jwtTokenizer, tokenService), UsernamePasswordAuthenticationFilter.class);

        // SecurityFilterChain 빌드 후 반환
        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
