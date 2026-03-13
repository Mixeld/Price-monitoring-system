package com.pricetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceStatsDto(
    Long productId,
    String productName,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    BigDecimal avgPrice,
    BigDecimal currentPrice,
    BigDecimal priceChange,
    double priceChangePercent,
    LocalDateTime firstRecorded,
    LocalDateTime lastRecorded,
    int totalRecords
) {}