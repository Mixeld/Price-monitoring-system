package com.pricetracker.dto;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String name,
    String description,
    BigDecimal currentPrice,
    String categoryName) {
}