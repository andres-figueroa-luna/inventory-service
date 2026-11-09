package com.project.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful product updated response",
        example = "{\"id\": 1, \"sku\": \"MONITOR-LG-01\", \"newStock\": \"5\", \"message\": \"Product updated successfully\"}")
public record ProductUpdatedResponse(Long id, String sku, Integer newStock, String message) {
}
