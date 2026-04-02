package com.pricetracker.service.cache;

import com.pricetracker.dto.ProductDto;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SearchCache {

  private final Map<SearchKey, Page<ProductDto>> cache = new ConcurrentHashMap<>();

  public Optional<Page<ProductDto>> get(SearchKey key) {
    return Optional.ofNullable(cache.get(key));
  }

  public void put(SearchKey key, Page<ProductDto> value) {
    cache.put(key, value);
  }

  public void invalidateAll() {
    int sizeBefore = cache.size();
    cache.clear();
    log.info("Cache invalidated. Size before: {}, Size after: 0", sizeBefore);
  }

  public void invalidateByCategory(String category) {
    int sizeBefore = cache.size();
    cache.keySet().removeIf(key ->
        category != null && category.equals(key.getCategory())
    );
    int sizeAfter = cache.size();
    log.info("Cache invalidated for category: {}. Size before: {}, Size after: {}",
        category, sizeBefore, sizeAfter);
  }

  public int getSize() {
    return cache.size();
  }

  public Set<SearchKey> getKeys() {
    return cache.keySet();
  }

  public boolean containsKey(SearchKey key) {
    return cache.containsKey(key);
  }

  public void clear() {
    cache.clear();
  }

  @Data
  @Builder
  public static class SearchKey {
    private final String category;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final int page;
    private final int size;
    private final String sort;
    private final boolean useNative;

    @Override
    public String toString() {
      return String.format("SearchKey{cat=%s, price=[%s-%s], page=%d/%d, sort=%s, native=%s}",
          category, minPrice, maxPrice, page, size, sort, useNative);
    }
  }
}