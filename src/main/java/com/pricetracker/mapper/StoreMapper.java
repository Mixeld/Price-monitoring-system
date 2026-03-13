package com.pricetracker.mapper;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.entity.Store;
import org.springframework.stereotype.Component;

@Component
public class StoreMapper {

  public StoreDto toDto(Store store) {
    if (store == null) {
      return null;
    }

    return new StoreDto(
        store.getId(),
        store.getName(),
        store.getWebsiteUrl()
    );
  }

  public Store toEntity(StoreDto dto) {
    if (dto == null) {
      return null;
    }

    Store store = new Store();
    store.setName(dto.name());
    store.setWebsiteUrl(dto.websiteUrl());
    return store;
  }
}