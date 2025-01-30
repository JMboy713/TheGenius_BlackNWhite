package com.genius.genius.common.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ExceptionEnum exception;

    public ApiException(ExceptionEnum exception) {
        super(exception.getMessage());
        this.exception = exception;
    }
}
