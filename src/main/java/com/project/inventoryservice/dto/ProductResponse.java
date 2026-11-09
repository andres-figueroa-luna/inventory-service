package com.project.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product details response",
        example = "{\"id\": 1, \"sku\": \"MONITOR-LG-01\", \"stock\": 50}")
public record ProductResponse(Long id, String sku, Integer stock) {
}
