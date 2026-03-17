package com.pricetracker.dto;

import java.time.LocalDateTime;

public record PriceStatsDto(
    Long productId,
    String productName,
    Double minPrice,
    Double maxPrice,
    Double avgPrice,
    Double currentPrice,
    Double priceChange,
    double priceChangePercent,
    LocalDateTime firstRecorded,
    LocalDateTime lastRecorded,
    int totalRecords
) {

}