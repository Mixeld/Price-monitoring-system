package com.pricetracker.dto;

import java.time.LocalDateTime;

public record PriceHistoryDto(
    Long id,
    Double price,
    LocalDateTime dateRecorded,
    Long productId,
    Long storeId  // Добавить, если нужно
) {}