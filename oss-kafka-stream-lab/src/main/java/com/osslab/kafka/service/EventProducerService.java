package com.osslab.kafka.service;

import com.osslab.kafka.config.KafkaConfig;
import com.osslab.kafka.config.KafkaTopicConfig;
import com.osslab.kafka.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducerService {

  private final KafkaTemplate<String, OrderDto> kafkaTemplate;

  public void sendMessage(OrderDto orderDto) {
    log.info("Producing message to topic {}: {}", KafkaTopicConfig.INPUT_TOPIC, orderDto.orderId());
    kafkaTemplate.send(KafkaTopicConfig.INPUT_TOPIC, orderDto.productId() , orderDto);
  }

}
