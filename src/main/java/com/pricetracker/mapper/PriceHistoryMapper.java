package com.pricetracker.mapper;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.entity.PriceHistory;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class PriceHistoryMapper {

  public PriceHistoryDto toDto(PriceHistory history) {
    if (history == null) {
      return null;
    }

    Long productId = (history.getProduct() != null) ? history.getProduct().getId() : null;
    Long storeId = (history.getStore() != null) ? history.getStore().getId() : null;

    return new PriceHistoryDto(
        history.getId(),
        history.getPrice().doubleValue(),
        history.getDateRecorded(),
        productId,
        storeId
    );
  }

  public PriceHistory toEntity(PriceHistoryDto dto) {
    if (dto == null) {
      return null;
    }

    PriceHistory history = new PriceHistory();
    history.setId(dto.id());
    history.setPrice(BigDecimal.valueOf(dto.price()));
    history.setDateRecorded(dto.dateRecorded());

    return history;
  }
}