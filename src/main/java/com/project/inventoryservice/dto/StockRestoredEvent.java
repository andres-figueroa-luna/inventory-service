package com.project.inventoryservice.dto;

public record StockRestoredEvent(Long orderId, String sku, Integer quantity) {
}
