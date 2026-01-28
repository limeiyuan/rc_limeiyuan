package com.example.adapter.kafka;

import com.example.adapter.dto.DeadLetterMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 死信队列服务 - 处理重试多次仍失败的消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterQueueService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${adapter.kafka.dead-letter-topic:http-adapter-request-dlq}")
    private String deadLetterTopic;

    @Value("${adapter.kafka.max-retry-count:3}")
    private int maxRetryCount;

    private static final String RETRY_COUNT_KEY_PREFIX = "adapter:retry:";
    private static final long RETRY_COUNT_EXPIRE_HOURS = 24;

    /**
     * 增加重试次数并返回当前次数
     *
     * @param messageKey 消息唯一标识（通常用 traceId 或 partition+offset）
     * @return 当前重试次数
     */
    public int incrementRetryCount(String messageKey) {
        String key = RETRY_COUNT_KEY_PREFIX + messageKey;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, RETRY_COUNT_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        return count != null ? count.intValue() : 1;
    }

    /**
     * 获取当前重试次数
     */
    public int getRetryCount(String messageKey) {
        String key = RETRY_COUNT_KEY_PREFIX + messageKey;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * 清除重试计数（处理成功时调用）
     */
    public void clearRetryCount(String messageKey) {
        String key = RETRY_COUNT_KEY_PREFIX + messageKey;
        redisTemplate.delete(key);
    }

    /**
     * 检查是否超过最大重试次数
     */
    public boolean isExceededMaxRetry(String messageKey) {
        return getRetryCount(messageKey) >= maxRetryCount;
    }

    /**
     * 发送消息到死信队列
     *
     * @param originalMessage 原始消息内容
     * @param traceId         链路追踪ID
     * @param errorMessage    错误信息
     * @param retryCount      重试次数
     */
    public void sendToDeadLetterQueue(String originalMessage, String traceId,
                                       String errorMessage, int retryCount) {
        try {
            DeadLetterMessage dlqMessage = new DeadLetterMessage();
            dlqMessage.setOriginalMessage(originalMessage);
            dlqMessage.setTraceId(traceId);
            dlqMessage.setErrorMessage(errorMessage);
            dlqMessage.setRetryCount(retryCount);
            dlqMessage.setFailedTime(LocalDateTime.now());

            String messageJson = objectMapper.writeValueAsString(dlqMessage);
            kafkaTemplate.send(deadLetterTopic, traceId, messageJson);

            log.warn("Message sent to dead letter queue, traceId={}, retryCount={}, error={}",
                    traceId, retryCount, errorMessage);

            // 清除重试计数
            clearRetryCount(traceId);

        } catch (Exception e) {
            log.error("Failed to send message to dead letter queue, traceId={}", traceId, e);
        }
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }
}
