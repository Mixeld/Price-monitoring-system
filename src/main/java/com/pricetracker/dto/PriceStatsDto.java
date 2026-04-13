package com.pricetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Price Statistics Data Transfer Object")
public record PriceStatsDto(
    @Schema(description = "Product ID", example = "1")
    Long productId,

    @Schema(description = "Product name", example = "iPhone 15 Pro")
    String productName,

    @Schema(description = "Minimum recorded price", example = "899.99")
    Double minPrice,

    @Schema(description = "Maximum recorded price", example = "1099.99")
    Double maxPrice,

    @Schema(description = "Average recorded price", example = "999.99")
    Double avgPrice,

    @Schema(description = "Current product price", example = "999.99")
    Double currentPrice,

    @Schema(description = "Absolute price change", example = "50.00")
    Double priceChange,

    @Schema(description = "Percentage price change", example = "5.25")
    double priceChangePercent,

    @Schema(description = "Date of first price record", example = "2024-01-01T10:00:00")
    LocalDateTime firstRecorded,

    @Schema(description = "Date of last price record", example = "2024-01-15T10:30:00")
    LocalDateTime lastRecorded,

    @Schema(description = "Total number of price records", example = "15")
    int totalRecords
) {

}