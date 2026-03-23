package com.pricetracker.service.cache;

import java.math.BigDecimal;

public record ProductSearchKey(
    String categoryName,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    int pageNumber,
    int pageSize,
    boolean useNativeQuery
) {
}
