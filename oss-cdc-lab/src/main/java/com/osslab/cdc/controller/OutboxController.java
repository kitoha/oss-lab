package com.osslab.cdc.controller;

import com.osslab.cdc.model.Outbox;
import com.osslab.cdc.repository.OutboxRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/outbox")
public class OutboxController {

    private final OutboxRepository outboxRepository;

    public OutboxController(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @PostMapping("/send")
    public ResponseEntity<Outbox> sendMessage(@RequestBody MessageRequest request) {
        Outbox outboxEvent = new Outbox(
                "Message",
                UUID.randomUUID().toString(),
                "MessageCreated",
                request.content()
        );
        Outbox savedEvent = outboxRepository.save(outboxEvent);
        return ResponseEntity.ok(savedEvent);
    }

    public record MessageRequest(String content) {}
}
