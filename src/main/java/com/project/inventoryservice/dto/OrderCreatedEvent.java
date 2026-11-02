package com.project.inventoryservice.dto;

public record OrderCreatedEvent(Long orderId, String productId, Integer quantity) {
}
