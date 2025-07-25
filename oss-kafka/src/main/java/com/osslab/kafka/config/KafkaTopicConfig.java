package com.osslab.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String REALTIME_EVENTS_TOPIC = "realtime-events";

    @Bean
    public NewTopic realtimeEventsTopic() {
        return TopicBuilder.name(REALTIME_EVENTS_TOPIC)
                .partitions(3) // 파티션을 3개로 설정하여 컨슈머 확장성을 테스트할 수 있도록 함
                .replicas(1)   // 현재는 단일 브로커이므로 복제본은 1
                .build();
    }
}