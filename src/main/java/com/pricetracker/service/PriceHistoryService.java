package com.pricetracker.service;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.Store;
import com.pricetracker.mapper.PriceHistoryMapper;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

  private final PriceHistoryRepository historyRepository;
  private final ProductRepository productRepository;
  private final StoreRepository storeRepository;
  private final PriceHistoryMapper mapper;

  @Transactional
  public PriceHistoryDto recordPrice(PriceHistoryDto dto) {
    PriceHistory history = mapper.toEntity(dto);

    Product product = productRepository.findById(dto.productId())
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + dto.productId()));
    history.setProduct(product);

    if (dto.storeId() != null) {
      Store store = storeRepository.findById(dto.storeId())
          .orElseThrow(() -> new EntityNotFoundException("Store not found: " + dto.storeId()));
      history.setStore(store);
    }

    PriceHistory saved = historyRepository.save(history);

    product.setPrice(dto.price());
    productRepository.save(product);

    return mapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryWithNPlusOne(Long productId) {
    log.info("--- Starting N+1 demonstration (Standard Find) ---");

    List<PriceHistory> historyList =
        historyRepository.findByProductIdOrderByDateRecordedDesc(productId);

    log.info("Primary SQL query executed. Records found: {}", historyList.size());

    List<PriceHistoryDto> result = new ArrayList<>();
    int queryCounter = 1;

    for (PriceHistory ph : historyList) {
      if (ph.getStore() != null) {
        log.info("Accessing Store with ID: {}. Hibernate will execute SELECT statement.",
            ph.getStore().getId());
        queryCounter++;
      }
      result.add(mapper.toDto(ph));
    }

    log.info("--- Summary: Total queries executed: {} (1 primary + {} additional) ---",
        queryCounter, queryCounter - 1);

    return result;
  }

  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryOptimized(Long productId) {
    log.info("--- Starting Optimized demonstration (EntityGraph) ---");

    List<PriceHistory> historyList = historyRepository.findWithStoreByProductId(productId);

    log.info("Primary SQL query executed with JOIN. Records found: {}",
        historyList.size());

    List<PriceHistoryDto> result = new ArrayList<>();

    for (PriceHistory ph : historyList) {
      if (ph.getStore() != null) {
        log.info("Accessing Store with ID: {}. Data already loaded in memory.",
            ph.getStore().getId());
      }
      result.add(mapper.toDto(ph));
    }

    log.info("--- Summary: Total queries executed: 1 ---");

    return result;
  }
}