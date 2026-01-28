package com.example.adapter.common.exception;

/**
 * 可重试异常 - 环境原因导致的异常，如网络超时、连接失败等
 * 抛出此异常时，Kafka消息不会被ack，将触发重试
 */
public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
