package com.osslab.kafka.producer;

import com.osslab.kafka.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequiredArgsConstructor
public class ProducerController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean isProducing = false;

    @GetMapping("/start/{tps}")
    public String startProducing(@PathVariable int tps) {
        if (isProducing) {
            return "Already producing messages.";
        }
        isProducing = true;
        long delay = 1000 / tps;
        AtomicLong counter = new AtomicLong();

        scheduler.scheduleAtFixedRate(() -> {
            if (isProducing) {
                String message = "Event " + counter.incrementAndGet();
                kafkaTemplate.send(KafkaTopicConfig.REALTIME_EVENTS_TOPIC, message);
            }
        }, 0, delay, TimeUnit.MILLISECONDS);

        return "Started producing " + tps + " messages per second.";
    }

    @GetMapping("/stop")
    public String stopProducing() {
        isProducing = false;
        return "Stopped producing messages.";
    }
}