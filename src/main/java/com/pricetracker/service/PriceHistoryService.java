package com.pricetracker.service;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.dto.PriceStatsDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.Store;
import com.pricetracker.mapper.PriceHistoryMapper;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryService {

  private final PriceHistoryRepository historyRepository;
  private final ProductRepository productRepository;
  private final StoreRepository storeRepository;
  private final PriceHistoryMapper mapper;

  /**
   * Записать новую цену
   */
  @Transactional
  public PriceHistoryDto recordPrice(PriceHistoryDto dto) {
    log.info("Recording price for product ID: {}, price: {}, store ID: {}",
        dto.productId(), dto.price(), dto.storeId());

    // Валидация цены
    validatePrice(dto.price());

    // Валидация даты
    LocalDateTime recordDate = validateAndGetDate(dto.dateRecorded());

    // Получаем продукт
    Product product = findProductById(dto.productId());

    // Создаем запись истории
    PriceHistory history = mapper.toEntity(dto);
    history.setDateRecorded(recordDate);
    history.setProduct(product);

    // Устанавливаем магазин, если указан
    if (dto.storeId() != null) {
      Store store = findStoreById(dto.storeId());
      history.setStore(store);
    }

    // Сохраняем историю
    PriceHistory saved = historyRepository.save(history);
    log.info("Price history recorded with id: {} for product: {}, price: {}",
        saved.getId(), product.getName(), dto.price());

    // Обновляем текущую цену продукта
    updateProductCurrentPrice(product, dto.price());

    return mapper.toDto(saved);
  }

  /**
   * Получить запись истории по ID
   */
  @Transactional(readOnly = true)
  public PriceHistoryDto getHistoryById(Long id) {
    log.debug("Getting price history by id: {}", id);
    return historyRepository.findById(id)
        .map(mapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("Price history not found with id: " + id));
  }

  /**
   * Получить всю историю цен для продукта
   */
  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryForProduct(Long productId) {
    log.debug("Getting price history for product ID: {}", productId);

    checkProductExists(productId);

    return historyRepository.findByProductIdOrderByDateRecordedDesc(productId)
        .stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Получить историю цен для продукта за указанный период
   */
  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryForProductInDateRange(
      Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting price history for product ID: {} from {} to {}", productId, from, to);

    checkProductExists(productId);
    validateDateRange(from, to);

    return historyRepository.findByProductIdAndDateRecordedBetweenOrderByDateRecordedDesc(
            productId, from, to)
        .stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Получить историю цен для магазина
   */
  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryForStore(Long storeId) {
    log.debug("Getting price history for store ID: {}", storeId);

    checkStoreExists(storeId);

    return historyRepository.findByStoreIdOrderByDateRecordedDesc(storeId)
        .stream()
        .map(mapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Получить последнюю цену продукта
   */
  @Transactional(readOnly = true)
  public PriceHistoryDto getLatestPrice(Long productId) {
    log.debug("Getting latest price for product ID: {}", productId);

    checkProductExists(productId);

    return historyRepository.findFirstByProductIdOrderByDateRecordedDesc(productId)
        .map(mapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "No price history found for product with id: " + productId));
  }

  /**
   * Получить статистику цен для продукта за последние N дней
   */
  @Transactional(readOnly = true)
  public PriceStatsDto getPriceStats(Long productId, int days) {
    log.debug("Getting price stats for product ID: {} for last {} days", productId, days);

    checkProductExists(productId);

    if (days <= 0 || days > 365) {
      throw new IllegalArgumentException("Days must be between 1 and 365");
    }

    LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
    List<PriceHistory> histories = historyRepository
        .findByProductIdAndDateRecordedAfterOrderByDateRecordedAsc(productId, fromDate);

    if (histories.isEmpty()) {
      throw new EntityNotFoundException("No price history found for product in the last " + days + " days");
    }

    Product product = productRepository.findById(productId).orElseThrow();

    return calculatePriceStats(product, histories);
  }

  /**
   * Получить минимальную цену продукта за период
   */
  @Transactional(readOnly = true)
  public PriceHistoryDto getMinPrice(Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting min price for product ID: {} from {} to {}", productId, from, to);

    checkProductExists(productId);
    validateDateRange(from, to);

    return historyRepository.findFirstByProductIdAndDateRecordedBetweenOrderByPriceAsc(
            productId, from, to)
        .map(mapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "No price history found for product in the specified period"));
  }

  /**
   * Получить максимальную цену продукта за период
   */
  @Transactional(readOnly = true)
  public PriceHistoryDto getMaxPrice(Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting max price for product ID: {} from {} to {}", productId, from, to);

    checkProductExists(productId);
    validateDateRange(from, to);

    return historyRepository.findFirstByProductIdAndDateRecordedBetweenOrderByPriceDesc(
            productId, from, to)
        .map(mapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "No price history found for product in the specified period"));
  }

  /**
   * Получить среднюю цену продукта за период
   */
  @Transactional(readOnly = true)
  public Double getAveragePrice(Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting average price for product ID: {} from {} to {}", productId, from, to);

    checkProductExists(productId);
    validateDateRange(from, to);

    Double avg = historyRepository.getAveragePriceByProductIdAndDateRange(productId, from, to);

    if (avg == null) {
      throw new EntityNotFoundException("No price history found for product in the specified period");
    }

    return avg;
  }

  /**
   * Получить количество записей истории для продукта
   */
  @Transactional(readOnly = true)
  public long countHistoryForProduct(Long productId) {
    checkProductExists(productId);
    return historyRepository.countByProductId(productId);
  }

  /**
   * Проверить, существует ли история для продукта
   */
  @Transactional(readOnly = true)
  public boolean existsForProduct(Long productId) {
    return historyRepository.existsByProductId(productId);
  }

  /**
   * Удалить запись истории
   */
  @Transactional
  public void deleteHistoryRecord(Long id) {
    log.debug("Deleting price history with id: {}", id);

    PriceHistory history = historyRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Price history not found with id: " + id));

    historyRepository.delete(history);
    log.info("Price history deleted with id: {}", id);
  }

  /**
   * Удалить всю историю для продукта
   */
  @Transactional
  public void deleteHistoryForProduct(Long productId) {
    log.debug("Deleting all price history for product ID: {}", productId);

    checkProductExists(productId);

    long count = historyRepository.countByProductId(productId);
    historyRepository.deleteByProductId(productId);

    log.info("Deleted {} price history records for product ID: {}", count, productId);
  }

  // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

  /**
   * Валидация цены
   */
  private void validatePrice(BigDecimal price) {
    if (price == null) {
      throw new IllegalArgumentException("Price cannot be null");
    }
    if (price.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    if (price.scale() > 2) {
      throw new IllegalArgumentException("Price cannot have more than 2 decimal places");
    }
  }

  /**
   * Валидация и нормализация даты
   */
  private LocalDateTime validateAndGetDate(LocalDateTime date) {
    if (date == null) {
      return LocalDateTime.now();
    }
    if (date.isAfter(LocalDateTime.now())) {
      throw new IllegalArgumentException("Date cannot be in the future");
    }
    return date;
  }

  /**
   * Валидация диапазона дат
   */
  private void validateDateRange(LocalDateTime from, LocalDateTime to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("Date range parameters cannot be null");
    }
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
  }

  /**
   * Найти продукт по ID или выбросить исключение
   */
  private Product findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));
  }

  /**
   * Проверить существование продукта
   */
  private void checkProductExists(Long productId) {
    if (!productRepository.existsById(productId)) {
      throw new EntityNotFoundException("Product not found with id: " + productId);
    }
  }

  /**
   * Найти магазин по ID или выбросить исключение
   */
  private Store findStoreById(Long storeId) {
    return storeRepository.findById(storeId)
        .orElseThrow(() -> new EntityNotFoundException("Store not found with id: " + storeId));
  }

  /**
   * Проверить существование магазина
   */
  private void checkStoreExists(Long storeId) {
    if (!storeRepository.existsById(storeId)) {
      throw new EntityNotFoundException("Store not found with id: " + storeId);
    }
  }

  /**
   * Обновить текущую цену продукта
   */
  private void updateProductCurrentPrice(Product product, BigDecimal newPrice) {
    product.setCurrentPrice(newPrice);
    productRepository.save(product);
    log.debug("Updated current price for product ID: {} to: {}", product.getId(), newPrice);
  }

  /**
   * Рассчитать статистику цен
   */
  private PriceStatsDto calculatePriceStats(Product product, List<PriceHistory> histories) {
    BigDecimal min = histories.stream()
        .map(PriceHistory::getPrice)
        .min(BigDecimal::compareTo)
        .orElse(product.getCurrentPrice());

    BigDecimal max = histories.stream()
        .map(PriceHistory::getPrice)
        .max(BigDecimal::compareTo)
        .orElse(product.getCurrentPrice());

    double avg = histories.stream()
        .mapToDouble(h -> h.getPrice().doubleValue())
        .average()
        .orElse(0.0);

    BigDecimal first = histories.get(0).getPrice();
    BigDecimal last = histories.get(histories.size() - 1).getPrice();
    BigDecimal change = last.subtract(first);

    double changePercent = 0;
    if (first.compareTo(BigDecimal.ZERO) != 0) {
      changePercent = (change.doubleValue() / first.doubleValue()) * 100;
    }

    return new PriceStatsDto(
        product.getId(),
        product.getName(),
        min,
        max,
        BigDecimal.valueOf(avg),
        product.getCurrentPrice(),
        change,
        changePercent,
        histories.get(0).getDateRecorded(),
        histories.get(histories.size() - 1).getDateRecorded(),
        histories.size()
    );
  }
}