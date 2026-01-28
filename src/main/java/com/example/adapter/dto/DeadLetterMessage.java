package com.example.adapter.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 死信队列消息
 */
@Data
public class DeadLetterMessage {

    /**
     * 原始消息内容
     */
    private String originalMessage;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 失败时间
     */
    private LocalDateTime failedTime;
}
