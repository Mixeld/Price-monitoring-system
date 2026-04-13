package com.pricetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Price History Data Transfer Object")
public record PriceHistoryDto(
    @Schema(description = "Price history record ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    Long id,

    @Schema(description = "Product price at the time of recording", example = "999.99", required = true)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,

    @Schema(description = "Date and time when price was recorded", example = "2024-01-15T10:30:00")
    LocalDateTime dateRecorded,

    @Schema(description = "Product ID", example = "1", required = true)
    @NotNull(message = "Product ID is required")
    Long productId,

    @Schema(description = "Store ID", example = "1")
    Long storeId,

    @Schema(description = "Store name (read-only)", example = "Amazon")
    String storeName
) {}