package com.project.inventoryservice.dto;

public record StockReservedEvent(Long orderId, String sku, Integer quantity) {
}
