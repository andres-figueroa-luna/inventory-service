package com.project.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful product creation response",
        example = "{\"id\": 1, \"sku\": \"MONITOR-LG-01\", \"message\": \"Product created successfully\"}")
public record ProductCreatedResponse(Long id, String sku, String message) {
}
