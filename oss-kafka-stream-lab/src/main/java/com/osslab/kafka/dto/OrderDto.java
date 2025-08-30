package com.osslab.kafka.dto;

public record OrderDto(
    String orderId,
    String userId,
    String productId,
    int quantity,
    double price
) {

}
