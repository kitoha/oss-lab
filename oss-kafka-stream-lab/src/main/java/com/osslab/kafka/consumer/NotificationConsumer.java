package com.osslab.kafka.consumer;

import com.osslab.kafka.config.KafkaTopicConfig;
import com.osslab.kafka.dto.AlertNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

  @KafkaListener(topics = KafkaTopicConfig.ALERTS_TOPIC, groupId = "notification-group", containerFactory = "alertKafkaListenerContainerFactory")
  public void consumeNotification(AlertNotificationDto orderDto) {
    log.info("Received alert notification: {} count {} ", orderDto.alertKey() , orderDto.alertCount());
  }

}
