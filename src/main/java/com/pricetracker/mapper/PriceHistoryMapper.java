package com.pricetracker.mapper;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.Store;
import org.springframework.stereotype.Component;

@Component
public final class PriceHistoryMapper {

  public PriceHistoryDto toDto(final PriceHistory history) {
    if (history == null) {
      return null;
    }

    Long productId = (history.getProduct() != null) ? history.getProduct().getId() : null;

    Long storeId = null;
    String storeName = null;

    if (history.getStore() != null) {
      storeId = history.getStore().getId();
      storeName = history.getStore().getName();
    }

    return new PriceHistoryDto(
        history.getId(),
        history.getPrice(),
        history.getDateRecorded(),
        productId,
        storeId,
        storeName
    );
  }

  public PriceHistory toEntity(final PriceHistoryDto dto) {
    if (dto == null) {
      return null;
    }
    PriceHistory history = new PriceHistory();
    history.setId(dto.id());
    history.setPrice(dto.price());
    history.setDateRecorded(dto.dateRecorded());

    return history;
  }
}