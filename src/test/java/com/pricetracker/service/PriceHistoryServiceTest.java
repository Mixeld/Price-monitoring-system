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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для PriceHistoryService")
class PriceHistoryServiceTest {

  @Mock
  private PriceHistoryRepository historyRepository;
  @Mock
  private ProductRepository productRepository;
  @Mock
  private StoreRepository storeRepository;
  @Mock
  private PriceHistoryMapper mapper;

  @InjectMocks
  private PriceHistoryService priceHistoryService;

  private PriceHistoryDto priceHistoryDtoWithStore;
  private Product product;
  private Store store;

  @BeforeEach
  void setUp() {
    product = new Product();
    product.setId(1L);
    store = new Store();
    store.setId(1L);
    store.setName("Amazon");

    priceHistoryDtoWithStore = new PriceHistoryDto(null, new BigDecimal("899.99"), LocalDateTime.now(), 1L, 1L, "Amazon");
  }

  @Nested
  @DisplayName("Тесты на запись цены (recordPrice)")
  class RecordPriceTests {

    @Test
    @DisplayName("Успешная запись цены с указанием магазина")
    void recordPrice_shouldSucceed_withStore() {
      when(productRepository.findById(1L)).thenReturn(Optional.of(product));
      when(storeRepository.findById(1L)).thenReturn(Optional.of(store));
      when(mapper.toEntity(priceHistoryDtoWithStore)).thenReturn(new PriceHistory());
      when(historyRepository.save(any(PriceHistory.class))).thenAnswer(inv -> inv.getArgument(0));
      when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
      when(mapper.toDto(any(PriceHistory.class))).thenReturn(priceHistoryDtoWithStore);

      priceHistoryService.recordPrice(priceHistoryDtoWithStore);

      ArgumentCaptor<PriceHistory> historyCaptor = ArgumentCaptor.forClass(PriceHistory.class);
      verify(historyRepository).save(historyCaptor.capture());
      PriceHistory capturedHistory = historyCaptor.getValue();
      assertThat(capturedHistory.getProduct()).isEqualTo(product);
      assertThat(capturedHistory.getStore()).isEqualTo(store);

      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      Product capturedProduct = productCaptor.getValue();
      assertThat(capturedProduct.getPrice()).isEqualByComparingTo("899.99");
    }

    @Test
    @DisplayName("Успешная запись цены БЕЗ указания магазина")
    void recordPrice_shouldSucceed_withoutStore() {
      PriceHistoryDto dtoWithoutStore = new PriceHistoryDto(null, new BigDecimal("999.99"), LocalDateTime.now(), 1L, null, null);
      when(productRepository.findById(1L)).thenReturn(Optional.of(product));
      when(mapper.toEntity(dtoWithoutStore)).thenReturn(new PriceHistory());
      when(historyRepository.save(any(PriceHistory.class))).thenReturn(new PriceHistory());
      when(mapper.toDto(any(PriceHistory.class))).thenReturn(dtoWithoutStore);

      priceHistoryService.recordPrice(dtoWithoutStore);

      verify(storeRepository, never()).findById(any());

      ArgumentCaptor<PriceHistory> historyCaptor = ArgumentCaptor.forClass(PriceHistory.class);
      verify(historyRepository).save(historyCaptor.capture());
      PriceHistory capturedHistory = historyCaptor.getValue();
      assertThat(capturedHistory.getStore()).isNull();
    }

    @Test
    @DisplayName("Ошибка, если продукт не найден")
    void recordPrice_shouldFail_whenProductNotFound() {
      when(mapper.toEntity(priceHistoryDtoWithStore)).thenReturn(new PriceHistory());
      when(productRepository.findById(1L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> priceHistoryService.recordPrice(priceHistoryDtoWithStore))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Ошибка, если магазин не найден")
    void recordPrice_shouldFail_whenStoreNotFound() {
      when(mapper.toEntity(priceHistoryDtoWithStore)).thenReturn(new PriceHistory());
      when(productRepository.findById(1L)).thenReturn(Optional.of(product));
      when(storeRepository.findById(1L)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> priceHistoryService.recordPrice(priceHistoryDtoWithStore))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Store not found");
    }
  }

  @Nested
  @DisplayName("Тесты на получение истории (N+1 демонстрация)")
  class GetHistoryTests {

    @Test
    @DisplayName("getHistoryWithNPlusOne должен обрабатывать записи с магазином и без него")
    void getHistoryWithNPlusOne_shouldHandleHistoryWithAndWithoutStore() {
      // Arrange
      // Создаем два объекта: один с магазином, другой без
      PriceHistory historyWithStore = PriceHistory.builder().id(1L).product(product).store(store).build();
      PriceHistory historyWithoutStore = PriceHistory.builder().id(2L).product(product).store(null).build();
      List<PriceHistory> historyList = List.of(historyWithStore, historyWithoutStore);

      when(historyRepository.findByProductIdOrderByDateRecordedDesc(1L)).thenReturn(historyList);
      // Неважно, что возвращает toDto, главное, что он вызывается
      when(mapper.toDto(any(PriceHistory.class))).thenReturn(priceHistoryDtoWithStore);

      // Act
      List<PriceHistoryDto> result = priceHistoryService.getHistoryWithNPlusOne(1L);

      // Assert
      verify(historyRepository).findByProductIdOrderByDateRecordedDesc(1L);
      verify(mapper, times(2)).toDto(any(PriceHistory.class));
      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("getHistoryOptimized должен обрабатывать записи с магазином и без него")
    void getHistoryOptimized_shouldHandleHistoryWithAndWithoutStore() {
      // Arrange
      PriceHistory historyWithStore = PriceHistory.builder().id(1L).product(product).store(store).build();
      PriceHistory historyWithoutStore = PriceHistory.builder().id(2L).product(product).store(null).build();
      List<PriceHistory> historyList = List.of(historyWithStore, historyWithoutStore);

      when(historyRepository.findWithStoreByProductId(1L)).thenReturn(historyList);
      when(mapper.toDto(any(PriceHistory.class))).thenReturn(priceHistoryDtoWithStore);

      // Act
      List<PriceHistoryDto> result = priceHistoryService.getHistoryOptimized(1L);

      // Assert
      verify(historyRepository).findWithStoreByProductId(1L);
      verify(mapper, times(2)).toDto(any(PriceHistory.class));
      assertThat(result).hasSize(2);
    }
  }
}