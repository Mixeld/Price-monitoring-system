package com.pricetracker.repository;

import com.pricetracker.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  // Поиск по продукту
  List<PriceHistory> findByProductIdOrderByDateRecordedDesc(Long productId);

  // Поиск по магазину - ЭТОТ МЕТОД ДОЛЖЕН БЫТЬ
  List<PriceHistory> findByStoreIdOrderByDateRecordedDesc(Long storeId);

  // Поиск по продукту и диапазону дат
  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByDateRecordedDesc(
      Long productId, LocalDateTime from, LocalDateTime to);

  // Поиск по продукту и дате после указанной
  List<PriceHistory> findByProductIdAndDateRecordedAfterOrderByDateRecordedAsc(
      Long productId, LocalDateTime from);

  // Последняя запись для продукта
  Optional<PriceHistory> findFirstByProductIdOrderByDateRecordedDesc(Long productId);

  // Минимальная цена за период
  Optional<PriceHistory> findFirstByProductIdAndDateRecordedBetweenOrderByPriceAsc(
      Long productId, LocalDateTime from, LocalDateTime to);

  // Максимальная цена за период
  Optional<PriceHistory> findFirstByProductIdAndDateRecordedBetweenOrderByPriceDesc(
      Long productId, LocalDateTime from, LocalDateTime to);

  // Средняя цена за период
  @Query("SELECT AVG(ph.price) FROM PriceHistory ph " +
      "WHERE ph.product.id = :productId " +
      "AND ph.dateRecorded BETWEEN :from AND :to")
  Double getAveragePriceByProductIdAndDateRange(
      @Param("productId") Long productId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);

  // Количество записей для продукта
  long countByProductId(Long productId);

  // Количество записей для магазина - ЭТОТ МЕТОД ТОЖЕ ДОЛЖЕН БЫТЬ
  long countByStoreId(Long storeId);

  // Проверка существования записей для продукта
  boolean existsByProductId(Long productId);

  // Удаление всех записей для продукта
  void deleteByProductId(Long productId);

}