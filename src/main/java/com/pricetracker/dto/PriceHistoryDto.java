package com.pricetracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceHistoryDto(
    Long id,
    BigDecimal price,
    LocalDateTime dateRecorded,
    Long productId,
    Long storeId
) {

}