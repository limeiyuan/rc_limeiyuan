package com.example.adapter.common.exception;

public class RateLimitException extends BusinessException {

    public RateLimitException() {
        super(ErrorCode.RATE_LIMIT_EXCEEDED);
    }

    public RateLimitException(String message) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED, message);
    }
}
