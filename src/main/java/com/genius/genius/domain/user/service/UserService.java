package com.genius.genius.domain.user.service;


import com.genius.genius.domain.user.domain.User;
import com.genius.genius.domain.user.request.UserRegRequest;

import java.util.Optional;

public interface UserService {
    void registerUser(UserRegRequest userRegRequest);
    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    User getCurrentUser();
}
