package com.pricetracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryDto(
    Long id,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,

    LocalDateTime dateRecorded,

    @NotNull(message = "Product ID is required")
    Long productId,

    Long storeId,

    String storeName
) {}