package com.pricetracker.service;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.dto.PriceStatsDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Product;
import com.pricetracker.exception.ResourceNotFoundException;
import com.pricetracker.mapper.PriceHistoryMapper;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.repository.StoreRepository;
import com.pricetracker.service.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PriceHistoryService extends BaseService<PriceHistory, PriceHistoryDto, Long> {

  private final PriceHistoryRepository priceHistoryRepository;
  private final ProductRepository productRepository;
  private final StoreRepository storeRepository;
  private final PriceHistoryMapper mapper;
  private final PriceHistoryService self;  // Добавляем final поле

  private static final String PRODUCT = "Product";

  @Autowired
  public PriceHistoryService(PriceHistoryRepository priceHistoryRepository,
      ProductRepository productRepository,
      StoreRepository storeRepository,
      PriceHistoryMapper mapper,
      @Lazy PriceHistoryService self) {  // Добавляем self в конструктор с @Lazy
    super(priceHistoryRepository, "PriceHistory", mapper::toDto, mapper::toEntity);
    this.priceHistoryRepository = priceHistoryRepository;
    this.productRepository = productRepository;
    this.storeRepository = storeRepository;
    this.mapper = mapper;
    this.self = self;
  }

  @Override
  protected Long getIdValue(PriceHistory entity) {
    return entity.getId();
  }

  @Override
  protected void validateBeforeCreate(PriceHistoryDto dto) {
    if (!productRepository.existsById(dto.productId())) {
      throw new ResourceNotFoundException(PRODUCT, "id", dto.productId());
    }
  }

  @Override
  protected void updateEntity(PriceHistory entity, PriceHistoryDto dto) {
    entity.setPrice(BigDecimal.valueOf(dto.price()));
    entity.setDateRecorded(dto.dateRecorded() != null ? dto.dateRecorded() : LocalDateTime.now());

    if (dto.productId() != null && !dto.productId().equals(entity.getProduct().getId())) {
      Product product = productRepository.findById(dto.productId())
          .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", dto.productId()));
      entity.setProduct(product);
    }
  }

  @Override
  protected void beforeSave(PriceHistory entity) {
    if (entity.getDateRecorded() == null) {
      entity.setDateRecorded(LocalDateTime.now());
    }
  }

  @Transactional(readOnly = true)
  public PriceHistoryDto getHistoryById(Long id) {
    log.debug("Getting price history by id: {}", id);
    return self.getById(id);  // Теперь self инициализирован
  }

  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryForProduct(Long productId) {
    log.debug("Getting price history for product id: {}", productId);
    return priceHistoryRepository.findByProductIdOrderByDateRecordedDesc(productId)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryForProductInDateRange(
      Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting price history for product {} between {} and {}", productId, from, to);
    return priceHistoryRepository.findByProductIdAndDateRecordedBetweenOrderByDateRecordedAsc(
            productId, from, to)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public PriceHistoryDto getLatestPrice(Long productId) {
    log.debug("Getting latest price for product id: {}", productId);
    return priceHistoryRepository.findFirstByProductIdOrderByDateRecordedDesc(productId)
        .map(mapper::toDto)
        .orElse(null);
  }

  @Transactional
  public PriceHistoryDto recordPrice(PriceHistoryDto dto) {
    log.debug("Recording new price for product: {}", dto.productId());
    return self.create(dto);
  }

  @Transactional
  public void deleteHistoryRecord(Long id) {
    log.debug("Deleting price history record: {}", id);
    self.delete(id);
  }

  @Transactional
  public void deleteHistoryForProduct(Long productId) {
    log.debug("Deleting all price history for product: {}", productId);
    priceHistoryRepository.deleteByProductId(productId);
  }

  @Transactional(readOnly = true)
  public long countHistoryForProduct(Long productId) {
    return priceHistoryRepository.countByProductId(productId);
  }

  @Transactional(readOnly = true)
  public PriceHistoryDto getMinPrice(Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting min price for product {} between {} and {}", productId, from, to);

    List<PriceHistory> histories = priceHistoryRepository
        .findByProductIdAndDateRecordedBetweenOrderByPriceAsc(productId, from, to);

    return histories.isEmpty() ? null : mapper.toDto(histories.get(0));
  }

  @Transactional(readOnly = true)
  public PriceHistoryDto getMaxPrice(Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting max price for product {} between {} and {}", productId, from, to);

    List<PriceHistory> histories = priceHistoryRepository
        .findByProductIdAndDateRecordedBetweenOrderByPriceDesc(productId, from, to);

    return histories.isEmpty() ? null : mapper.toDto(histories.get(0));
  }

  @Transactional(readOnly = true)
  public Double getAveragePrice(Long productId, LocalDateTime from, LocalDateTime to) {
    log.debug("Getting average price for product {} between {} and {}", productId, from, to);

    List<PriceHistory> histories = priceHistoryRepository
        .findByProductIdAndDateRecordedBetweenOrderByDateRecordedAsc(productId, from, to);

    if (histories.isEmpty()) {
      return 0.0;
    }

    return histories.stream()
        .map(PriceHistory::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(histories.size()), 2, RoundingMode.HALF_UP)
        .doubleValue();
  }

  @Transactional(readOnly = true)
  public boolean existsForProduct(Long productId) {
    return priceHistoryRepository.countByProductId(productId) > 0;
  }

  @Transactional(readOnly = true)
  public PriceStatsDto getPriceStats(Long productId, int days) {
    log.debug("Getting price stats for product {} for last {} days", productId, days);

    LocalDateTime end = LocalDateTime.now();
    LocalDateTime start = end.minusDays(days);

    List<PriceHistory> histories = priceHistoryRepository
        .findByProductIdAndDateRecordedBetweenOrderByDateRecordedAsc(productId, start, end);

    if (histories.isEmpty()) {
      return null;
    }

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", productId));

    BigDecimal min = histories.stream()
        .map(PriceHistory::getPrice)
        .min(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    BigDecimal max = histories.stream()
        .map(PriceHistory::getPrice)
        .max(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    BigDecimal sum = histories.stream()
        .map(PriceHistory::getPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal avg = sum.divide(BigDecimal.valueOf(histories.size()), 2, RoundingMode.HALF_UP);

    BigDecimal current = histories.get(histories.size() - 1).getPrice();
    BigDecimal first = histories.get(0).getPrice();
    BigDecimal change = current.subtract(first);

    double changePercent = first.compareTo(BigDecimal.ZERO) != 0
        ? change.multiply(BigDecimal.valueOf(100))
        .divide(first, 2, RoundingMode.HALF_UP)
        .doubleValue()
        : 0.0;

    return new PriceStatsDto(
        productId,
        product.getName(),
        min.doubleValue(),
        max.doubleValue(),
        avg.doubleValue(),
        current.doubleValue(),
        change.doubleValue(),
        changePercent,
        histories.get(0).getDateRecorded(),
        histories.get(histories.size() - 1).getDateRecorded(),
        histories.size()
    );
  }

  @Transactional(readOnly = true)
  public List<PriceHistoryDto> getHistoryForStore(Long storeId) {
    log.debug("Getting price history for store: {}", storeId);

    if (!storeRepository.existsById(storeId)) {
      throw new ResourceNotFoundException("Store", "id", storeId);
    }

    return priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(storeId)
        .stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional
  public List<PriceHistoryDto> addBulkPrices(Long productId, List<Double> prices) {
    log.debug("Adding {} bulk prices for product: {}", prices.size(), productId);

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException(PRODUCT, "id", productId));

    List<PriceHistory> histories = prices.stream()
        .map(price -> PriceHistory.builder()
            .product(product)
            .price(BigDecimal.valueOf(price))
            .dateRecorded(LocalDateTime.now())
            .build())
        .toList();

    return priceHistoryRepository.saveAll(histories).stream()
        .map(mapper::toDto)
        .toList();
  }

  @Transactional
  public void deleteByProductId(Long productId) {
    log.debug("Deleting all price history for product id: {}", productId);
    priceHistoryRepository.deleteByProductId(productId);
  }
}
