package com.example.adapter.kafka;

import com.example.adapter.common.exception.BusinessException;
import com.example.adapter.common.exception.RetryableException;
import com.example.adapter.dto.AdapterMessage;
import com.example.adapter.service.AdapterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdapterMessageListener {

    private final ObjectMapper objectMapper;
    private final AdapterService adapterService;
    private final DeadLetterQueueService deadLetterQueueService;

    @KafkaListener(topics = "${adapter.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String traceId = null;
        AdapterMessage message = null;
        String originalMessage = record.value();

        try {
            message = objectMapper.readValue(originalMessage, AdapterMessage.class);

            traceId = message.getTraceId();
            if (!StringUtils.hasText(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", "");
                message.setTraceId(traceId);
            }
            MDC.put("traceId", traceId);

            log.info("Received message, tenantCode={}, apiCode={}, traceId={}",
                    message.getTenantCode(), message.getApiCode(), traceId);

            validateMessage(message);
            adapterService.process(message);

            // 处理成功，清除重试计数
            deadLetterQueueService.clearRetryCount(traceId);
            ack.acknowledge();
            log.info("Message processed successfully, traceId={}", traceId);

        } catch (RetryableException e) {
            // 环境原因导致的异常（网络超时、连接失败、5xx错误等）
            handleRetryableException(traceId, originalMessage, e, ack);

        } catch (BusinessException e) {
            // 业务异常（租户不存在、配置错误、限流等），进行ack，不重试
            log.error("Business error occurred, will not retry. traceId={}, tenantCode={}, apiCode={}, error={}",
                    traceId,
                    message != null ? message.getTenantCode() : "unknown",
                    message != null ? message.getApiCode() : "unknown",
                    e.getMessage());
            ack.acknowledge();

        } catch (IllegalArgumentException e) {
            // 参数校验失败，进行ack，不重试
            log.error("Message validation failed, will not retry. traceId={}, error={}", traceId, e.getMessage());
            ack.acknowledge();

        } catch (Exception e) {
            // 消息解析失败等其他异常，进行ack，不重试（避免无限重试无法解析的消息）
            log.error("Unexpected error occurred, will not retry. traceId={}, error={}", traceId, e.getMessage(), e);
            ack.acknowledge();
        } finally {
            MDC.remove("traceId");
        }
    }

    /**
     * 处理可重试异常，检查重试次数，超过阈值则发送到死信队列
     */
    private void handleRetryableException(String traceId, String originalMessage,
                                          RetryableException e, Acknowledgment ack) {
        // 增加重试计数
        int retryCount = deadLetterQueueService.incrementRetryCount(traceId);
        int maxRetryCount = deadLetterQueueService.getMaxRetryCount();

        if (retryCount >= maxRetryCount) {
            // 超过最大重试次数，发送到死信队列
            log.error("Max retry count exceeded, sending to dead letter queue. traceId={}, retryCount={}/{}",
                    traceId, retryCount, maxRetryCount);
            deadLetterQueueService.sendToDeadLetterQueue(originalMessage, traceId, e.getMessage(), retryCount);
            ack.acknowledge();
        } else {
            // 未超过最大重试次数，不进行ack，触发Kafka重试
            log.warn("Retryable error occurred, will retry. traceId={}, retryCount={}/{}, error={}",
                    traceId, retryCount, maxRetryCount, e.getMessage());
            // 不调用 ack.acknowledge()，让Kafka进行重试
        }
    }

    private void validateMessage(AdapterMessage message) {
        if (!StringUtils.hasText(message.getTenantCode())) {
            throw new IllegalArgumentException("tenantCode is required");
        }
        if (!StringUtils.hasText(message.getApiCode())) {
            throw new IllegalArgumentException("apiCode is required");
        }
    }
}
