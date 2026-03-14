package com.pricetracker.service;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Store;
import com.pricetracker.mapper.StoreMapper;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

  private static final String STORE_NOT_FOUND_BY_ID = "Store not found with id: ";
  private static final String STORE_NOT_FOUND_BY_NAME = "Store not found with name: ";
  private static final String STORE_ALREADY_EXISTS = "Store with name '%s' already exists";
  private static final String STORE_WITH_URL_ALREADY_EXISTS = "Store with website URL '%s' already exists";
  private static final String CANNOT_DELETE_STORE_WITH_HISTORY =
      "Cannot delete store '%s' because it has %d price history records. " +
          "Delete these records first or reassign them to another store.";

  private final StoreRepository storeRepository;
  private final PriceHistoryRepository priceHistoryRepository;
  private final StoreMapper storeMapper;

  @Transactional(readOnly = true)
  public List<StoreDto> getAllStores() {
    log.debug("Fetching all stores");
    return storeRepository.findAll().stream()
        .map(storeMapper::toDto)
        .toList(); // Заменено collect(Collectors.toList()) на toList()
  }

  @Transactional(readOnly = true)
  public StoreDto getStoreById(Long id) {
    log.debug("Fetching store by id: {}", id);
    return storeRepository.findById(id)
        .map(storeMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(STORE_NOT_FOUND_BY_ID + id));
  }

  @Transactional(readOnly = true)
  public StoreDto getStoreByName(String name) {
    log.debug("Fetching store by name: {}", name);
    return storeRepository.findByName(name)
        .map(storeMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(STORE_NOT_FOUND_BY_NAME + name));
  }

  @Transactional
  public StoreDto createStore(StoreDto dto) {
    log.debug("Creating new store with name: {}", dto.name());

    if (storeRepository.findByName(dto.name()).isPresent()) {
      throw new IllegalArgumentException(
          String.format(STORE_ALREADY_EXISTS, dto.name()));
    }

    if (dto.websiteUrl() != null && !dto.websiteUrl().isBlank() &&
        storeRepository.findByWebsiteUrl(dto.websiteUrl()).isPresent()) {
      throw new IllegalArgumentException(
          String.format(STORE_WITH_URL_ALREADY_EXISTS, dto.websiteUrl()));
    }

    Store store = storeMapper.toEntity(dto);
    Store savedStore = storeRepository.save(store);
    log.info("Store created successfully with id: {} and name: {}",
        savedStore.getId(), savedStore.getName());

    return storeMapper.toDto(savedStore);
  }

  @Transactional
  public StoreDto updateStore(Long id, StoreDto dto) {
    log.debug("Updating store with id: {}", id);

    Store store = storeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(STORE_NOT_FOUND_BY_ID + id));

    if (!store.getName().equals(dto.name()) &&
        storeRepository.findByName(dto.name()).isPresent()) {
      throw new IllegalArgumentException(
          String.format(STORE_ALREADY_EXISTS, dto.name()));
    }

    if (dto.websiteUrl() != null && !dto.websiteUrl().isBlank() &&
        !dto.websiteUrl().equals(store.getWebsiteUrl()) &&
        storeRepository.findByWebsiteUrl(dto.websiteUrl()).isPresent()) {
      throw new IllegalArgumentException(
          String.format(STORE_WITH_URL_ALREADY_EXISTS, dto.websiteUrl()));
    }

    store.setName(dto.name());
    store.setWebsiteUrl(dto.websiteUrl());

    log.info("Store updated successfully with id: {} -> new name: {}", id, dto.name());

    return storeMapper.toDto(store);
  }

  @Transactional
  public void deleteStore(Long id) {
    log.debug("Deleting store with id: {}", id);

    Store store = storeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(STORE_NOT_FOUND_BY_ID + id));

    List<PriceHistory> priceHistories = priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(id);
    if (!priceHistories.isEmpty()) {
      int historyCount = priceHistories.size();
      log.warn("Cannot delete store with id: {} because it has {} price history records",
          id, historyCount);
      throw new IllegalStateException(
          String.format(CANNOT_DELETE_STORE_WITH_HISTORY,
              store.getName(), historyCount));
    }

    storeRepository.delete(store);
    log.info("Store deleted successfully with id: {}", id);
  }

  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    return storeRepository.findByName(name).isPresent();
  }

  @Transactional(readOnly = true)
  public boolean existsByWebsiteUrl(String websiteUrl) {
    if (websiteUrl == null || websiteUrl.isBlank()) {
      return false;
    }
    return storeRepository.findByWebsiteUrl(websiteUrl).isPresent();
  }

  @Transactional(readOnly = true)
  public List<StoreDto> searchStoresByName(String namePattern) {
    log.debug("Searching stores by name pattern: {}", namePattern);
    return storeRepository.findByNameContainingIgnoreCase(namePattern).stream()
        .map(storeMapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public long getPriceHistoryCount(Long storeId) {
    if (!storeRepository.existsById(storeId)) {
      throw new EntityNotFoundException(STORE_NOT_FOUND_BY_ID + storeId);
    }
    return priceHistoryRepository.countByStoreId(storeId);
  }
}