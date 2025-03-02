package com.genius.genius.domain.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.Pattern;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegRequest {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[0-9a-zA-Z]{4,16}$")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,16}$",
            message = "영어, 숫자, 특수문자를 포함한 8~16자 비밀번호를 입력해주세요.")
    private String password;

    @JsonProperty("password_check")
    @NotEmpty(message = "비밀번호 확인을 입력해주세요.")
    private String passwordCheck;
}
