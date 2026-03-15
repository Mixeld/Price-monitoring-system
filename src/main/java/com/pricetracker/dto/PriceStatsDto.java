package com.pricetracker.dto;

import java.time.LocalDateTime;

public record PriceStatsDto(
    Long productId,
    String productName,
    Double minPrice,        // Изменено с BigDecimal
    Double maxPrice,        // Изменено с BigDecimal
    Double avgPrice,        // Изменено с BigDecimal
    Double currentPrice,    // Изменено с BigDecimal
    Double priceChange,     // Изменено с BigDecimal
    double priceChangePercent,
    LocalDateTime firstRecorded,
    LocalDateTime lastRecorded,
    int totalRecords
) {}