package com.project.inventoryservice.dto;

public record OrderCancelledEvent(Long orderId, String productId, Integer quantity) {
}
