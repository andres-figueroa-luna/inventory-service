package com.project.inventoryservice.dto;

public record ProductUpdatedResponse(Long id, String sku, Integer newStock, String message) {
}
