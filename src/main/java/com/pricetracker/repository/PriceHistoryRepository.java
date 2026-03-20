package com.pricetracker.repository;

import com.pricetracker.entity.PriceHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  List<PriceHistory> findByProductIdOrderByDateRecordedDesc(Long productId);

  List<PriceHistory> findByStoreIdOrderByDateRecordedDesc(Long storeId);

  long countByProductId(Long productId);
  long countByStoreId(Long storeId);

  void deleteByProductId(Long productId);

  Optional<PriceHistory> findFirstByProductIdOrderByDateRecordedDesc(Long productId);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByDateRecordedAsc(
      Long productId, LocalDateTime start, LocalDateTime end);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByPriceAsc(
      Long productId, LocalDateTime start, LocalDateTime end);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByPriceDesc(
      Long productId, LocalDateTime start, LocalDateTime end);

  @EntityGraph(attributePaths = {"store"})
  @Query("SELECT ph FROM PriceHistory ph WHERE ph.product.id = :productId ORDER BY ph.dateRecorded DESC")
  List<PriceHistory> findWithStoreByProductId(Long productId);

  @Override
  @EntityGraph(attributePaths = {"store"})
  List<PriceHistory> findAll();
}