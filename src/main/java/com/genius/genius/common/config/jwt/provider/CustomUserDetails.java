package com.genius.genius.common.config.jwt.provider;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 사용자 인증 및 권한 관리를 위한 사용자 세부 정보 클래스
 */
public class CustomUserDetails implements UserDetails {
    private final String username; // 사용자 아이디
    private final String password; // 비밀번호
    @Getter
    private final Long userId; // 사용자 ID
    private final List<GrantedAuthority> authorities; // 권한 목록

    /**
     * 생성자
     *
     * @param username 이메일
     * @param password 비밀번호
     * @param userId 사용자 식별 아이디
     * @param authorities 권한 목록
     */
    public CustomUserDetails(String username, String password, Long userId, List<GrantedAuthority> authorities){
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}