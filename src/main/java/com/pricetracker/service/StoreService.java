package com.pricetracker.service;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Store;
import com.pricetracker.mapper.StoreMapper;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

  private final StoreRepository storeRepository;
  private final PriceHistoryRepository priceHistoryRepository;
  private final StoreMapper storeMapper;

  @Transactional(readOnly = true)
  public List<StoreDto> getAllStores() {
    log.debug("Fetching all stores");
    return storeRepository.findAll().stream()
        .map(storeMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public StoreDto getStoreById(Long id) {
    log.debug("Fetching store by id: {}", id);
    return storeRepository.findById(id)
        .map(storeMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public StoreDto getStoreByName(String name) {
    log.debug("Fetching store by name: {}", name);
    return storeRepository.findByName(name)
        .map(storeMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("Store not found with name: " + name));
  }

  @Transactional
  public StoreDto createStore(StoreDto dto) {
    log.debug("Creating new store with name: {}", dto.name());

    // Проверка на существующий магазин с таким же именем
    if (storeRepository.findByName(dto.name()).isPresent()) {
      throw new IllegalArgumentException("Store with name '" + dto.name() + "' already exists");
    }

    // Проверка на существующий магазин с таким же websiteUrl
    if (dto.websiteUrl() != null && !dto.websiteUrl().isBlank() &&
        storeRepository.findByWebsiteUrl(dto.websiteUrl()).isPresent()) {
      throw new IllegalArgumentException("Store with website URL '" + dto.websiteUrl() + "' already exists");
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
        .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + id));

    // Проверка уникальности имени (если оно меняется)
    if (!store.getName().equals(dto.name()) &&
        storeRepository.findByName(dto.name()).isPresent()) {
      throw new IllegalArgumentException("Store with name '" + dto.name() + "' already exists");
    }

    // Проверка уникальности websiteUrl (если он меняется и не пустой)
    if (dto.websiteUrl() != null && !dto.websiteUrl().isBlank() &&
        !dto.websiteUrl().equals(store.getWebsiteUrl()) &&
        storeRepository.findByWebsiteUrl(dto.websiteUrl()).isPresent()) {
      throw new IllegalArgumentException("Store with website URL '" + dto.websiteUrl() + "' already exists");
    }

    store.setName(dto.name());
    store.setWebsiteUrl(dto.websiteUrl());

    log.info("Store updated successfully with id: {}", id);
    return storeMapper.toDto(store);
  }

  @Transactional
  public void deleteStore(Long id) {
    log.debug("Deleting store with id: {}", id);

    Store store = storeRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + id));

    // Проверка, есть ли у магазина записи в истории цен
    List<PriceHistory> priceHistories = priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(id);

    if (!priceHistories.isEmpty()) {
      int historyCount = priceHistories.size();
      log.warn("Cannot delete store with id: {} because it has {} price history records",
          id, historyCount);
      throw new IllegalStateException(
          "Cannot delete store '" + store.getName() +
              "' because it has " + historyCount + " price history records. " +
              "Delete these records first or reassign them to another store.");
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
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public long getPriceHistoryCount(Long storeId) {
    if (!storeRepository.existsById(storeId)) {
      throw new EntityNotFoundException("Store not found with id: " + storeId);
    }
    return priceHistoryRepository.countByStoreId(storeId);
  }
}