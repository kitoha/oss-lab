package com.osslab.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

  public static final String INPUT_TOPIC = "input-topic";
  public static final String OUTPUT_TOPIC = "output-topic";

  public static final String ALERTS_TOPIC = "alerts-topic";

  @Bean
  public NewTopic inputTopic() {
      return TopicBuilder.name(INPUT_TOPIC)
              .partitions(3)
              .replicas(1)
              .build();
  }

  @Bean
  public NewTopic outputTopic() {
      return TopicBuilder.name(OUTPUT_TOPIC)
              .partitions(3)
              .replicas(1)
              .build();
  }

  @Bean
  public NewTopic alertsTopic() {
      return TopicBuilder.name(ALERTS_TOPIC)
              .partitions(3)
              .replicas(1)
              .build();
  }
}
