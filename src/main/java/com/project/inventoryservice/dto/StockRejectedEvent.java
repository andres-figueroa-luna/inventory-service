package com.project.inventoryservice.dto;

public record StockRejectedEvent(Long orderId, String productId, String reason) {
}
