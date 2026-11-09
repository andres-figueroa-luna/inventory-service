package com.project.inventoryservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductRequest(
        @Schema(description = "Unique business identifier for the product", example = "MONITOR-LG-01")
        @NotBlank(message = "SKU id is required")
        String sku,

        @Schema(description = "Initial stock amount", example = "10", minimum = "0")
        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock
) {
}
