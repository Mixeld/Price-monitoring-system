package com.pricetracker.repository;

import com.pricetracker.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  // Существующие методы
  List<PriceHistory> findByProductIdOrderByDateRecordedDesc(Long productId);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByDateRecordedAsc(
      Long productId, LocalDateTime start, LocalDateTime end);

  Optional<PriceHistory> findFirstByProductIdOrderByDateRecordedDesc(Long productId);

  long countByProductId(Long productId);

  long countByStoreId(Long storeId);

  void deleteByProductId(Long productId);

  // Добавленные методы для сортировки по цене
  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByPriceAsc(
      Long productId, LocalDateTime start, LocalDateTime end);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByPriceDesc(
      Long productId, LocalDateTime start, LocalDateTime end);

  // Добавленный метод для поиска по store
  List<PriceHistory> findByStoreIdOrderByDateRecordedDesc(Long storeId);
}