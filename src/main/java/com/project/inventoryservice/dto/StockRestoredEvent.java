package com.project.inventoryservice.dto;

public record StockRestoredEvent(Long orderId, String productId, Integer quantity) {
}
