package com.osslab.kafka.controller;

import com.osslab.kafka.dto.OrderDto;
import com.osslab.kafka.service.EventProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EventProducerController {

  private final EventProducerService eventProducerService;

  @PostMapping("/produce-events")
  public ResponseEntity<String> produceEvents(@RequestBody OrderDto orderDto) {
    eventProducerService.sendMessage(orderDto);
    return ResponseEntity.ok("Events produced successfully");
  }

}
