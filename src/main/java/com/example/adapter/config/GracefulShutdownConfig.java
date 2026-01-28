package com.example.adapter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 优雅停机配置 - 确保Kafka消息处理完成后再关闭
 */
@Slf4j
@Component
public class GracefulShutdownConfig implements ApplicationListener<ContextClosedEvent> {

    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    public GracefulShutdownConfig(KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
        this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        log.info("Graceful shutdown initiated...");

        // 停止Kafka消费者
        kafkaListenerEndpointRegistry.stop();
        log.info("Kafka listeners stopped");

        // 等待处理中的消息完成
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Graceful shutdown completed");
    }
}
