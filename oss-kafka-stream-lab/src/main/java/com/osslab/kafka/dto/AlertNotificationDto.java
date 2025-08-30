package com.osslab.kafka.dto;

public record AlertNotificationDto(
    String alertKey,
    long alertCount,
    long windowStart,
    long windowEnd
) {

}
