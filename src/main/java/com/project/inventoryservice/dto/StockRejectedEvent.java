package com.project.inventoryservice.dto;

public record StockRejectedEvent(Long orderId, String sku, String reason) {
}
