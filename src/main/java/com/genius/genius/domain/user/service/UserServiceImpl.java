package com.genius.genius.domain.user.service;

import com.genius.genius.common.exception.ApiException;
import com.genius.genius.common.exception.ExceptionEnum;
import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.repository.UserRepository;
import com.genius.genius.domain.user.request.UserRegRequest;
import com.genius.genius.domain.user.util.RandomName;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RandomName randomName;
    @Override
    public void registerUser(UserRegRequest userRegRequest) {
        // 아이디 중복 확인
        if (userRepository.findByUsername(userRegRequest.getUsername()).isPresent()) {
            throw new ApiException(ExceptionEnum.EXIST_USERNAME);
        }

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(userRegRequest.getPassword());

        // 유저로 변환
        User user = User.builder()
                .username(userRegRequest.getUsername())
                .password(encodedPassword)
                .name(randomName.generate())
                .isDeleted(false)
                .build();

        // 저장
        userRepository.save(user);
    }
}
