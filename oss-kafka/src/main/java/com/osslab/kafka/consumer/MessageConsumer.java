package com.osslab.kafka.consumer;

import com.osslab.kafka.config.KafkaTopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    @KafkaListener(topics = KafkaTopicConfig.REALTIME_EVENTS_TOPIC, groupId = "fast-consumer-group")
    public void consumeFast(String message) {
        logger.info("[Fast-Consumer] Received message: {}", message);
    }

    @KafkaListener(topics = KafkaTopicConfig.REALTIME_EVENTS_TOPIC, groupId = "slow-consumer-group")
    public void consumeSlow(String message) throws InterruptedException {
        logger.info("[Slow-Consumer] Received message: {}", message);
        // Simulate slow processing
        Thread.sleep(100);
        logger.info("[Slow-Consumer] Processed message: {}", message);
    }
}