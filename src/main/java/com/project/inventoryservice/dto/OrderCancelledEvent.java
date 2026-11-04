package com.project.inventoryservice.dto;

public record OrderCancelledEvent(Long orderId, String sku, Integer quantity) {
}
