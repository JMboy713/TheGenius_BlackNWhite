package com.genius.genius.domain.user.service;


import com.genius.genius.domain.user.request.UserRegRequest;

public interface UserService {
    void registerUser(UserRegRequest userRegRequest);
}
