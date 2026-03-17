package com.pricetracker.repository;

import com.pricetracker.entity.PriceHistory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

  List<PriceHistory> findByProductIdOrderByDateRecordedDesc(Long productId);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByDateRecordedAsc(
      Long productId, LocalDateTime start, LocalDateTime end);

  Optional<PriceHistory> findFirstByProductIdOrderByDateRecordedDesc(Long productId);

  long countByProductId(Long productId);

  long countByStoreId(Long storeId);

  void deleteByProductId(Long productId);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByPriceAsc(
      Long productId, LocalDateTime start, LocalDateTime end);

  List<PriceHistory> findByProductIdAndDateRecordedBetweenOrderByPriceDesc(
      Long productId, LocalDateTime start, LocalDateTime end);

  List<PriceHistory> findByStoreIdOrderByDateRecordedDesc(Long storeId);
}