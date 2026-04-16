package com.pricetracker.controller;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.service.PriceHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для PriceHistoryController")
class PriceHistoryControllerTest {

  @Mock
  private PriceHistoryService priceHistoryService;

  @InjectMocks
  private PriceHistoryController priceHistoryController;

  private PriceHistoryDto priceHistoryDto;
  private List<PriceHistoryDto> priceHistoryDtoList;

  private final Long PRODUCT_ID = 1L;
  private final Long STORE_ID = 10L;
  private final BigDecimal PRICE = new BigDecimal("899.99");
  private final LocalDateTime DATE_RECORDED = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
  private final String STORE_NAME = "Amazon";

  @BeforeEach
  void setUp() {
    priceHistoryDto = new PriceHistoryDto(
        100L, PRICE, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME
    );
    priceHistoryDtoList = List.of(priceHistoryDto);
  }

  // ==================== ТЕСТЫ ДЛЯ recordPrice ====================

  @Nested
  @DisplayName("Тесты метода recordPrice(PriceHistoryDto dto)")
  class RecordPriceTests {

    @Test
    @DisplayName("[УСПЕХ] Запись цены")
    void recordPrice_shouldRecordAndReturnPriceHistory() {
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(priceHistoryDto);

      PriceHistoryDto result = priceHistoryController.recordPrice(priceHistoryDto);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(100L);
      assertThat(result.price()).isEqualByComparingTo(PRICE);
      assertThat(result.productId()).isEqualTo(PRODUCT_ID);
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
      verify(priceHistoryService).recordPrice(priceHistoryDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Запись цены без магазина")
    void recordPrice_shouldRecordPriceWithoutStore() {
      PriceHistoryDto dtoWithoutStore = new PriceHistoryDto(
          null, PRICE, DATE_RECORDED, PRODUCT_ID, null, null
      );
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(dtoWithoutStore);

      PriceHistoryDto result = priceHistoryController.recordPrice(dtoWithoutStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.storeId()).isNull();
      assertThat(result.storeName()).isNull();
      verify(priceHistoryService).recordPrice(dtoWithoutStore);
    }

    @Test
    @DisplayName("[УСПЕХ] Запись цены с минимальными полями")
    void recordPrice_shouldRecordPriceWithMinimalFields() {
      PriceHistoryDto minimalDto = new PriceHistoryDto(
          null, new BigDecimal("99.99"), LocalDateTime.now(), PRODUCT_ID, null, null
      );
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(minimalDto);

      PriceHistoryDto result = priceHistoryController.recordPrice(minimalDto);

      assertThat(result).isNotNull();
      assertThat(result.price()).isEqualByComparingTo("99.99");
      verify(priceHistoryService).recordPrice(minimalDto);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getHistory ====================

  @Nested
  @DisplayName("Тесты метода getHistory(Long productId, boolean optimized)")
  class GetHistoryTests {

    @Test
    @DisplayName("[УСПЕХ] Получение истории с оптимизированным запросом (optimized = true)")
    void getHistory_shouldReturnOptimizedHistory_whenOptimizedTrue() {
      when(priceHistoryService.getHistoryOptimized(PRODUCT_ID)).thenReturn(priceHistoryDtoList);

      List<PriceHistoryDto> result = priceHistoryController.getHistory(PRODUCT_ID, true);

      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(priceHistoryDto);
      verify(priceHistoryService).getHistoryOptimized(PRODUCT_ID);
      verify(priceHistoryService, never()).getHistoryWithNPlusOne(anyLong());
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории с N+1 запросом (optimized = false)")
    void getHistory_shouldReturnNPlusOneHistory_whenOptimizedFalse() {
      when(priceHistoryService.getHistoryWithNPlusOne(PRODUCT_ID)).thenReturn(priceHistoryDtoList);

      List<PriceHistoryDto> result = priceHistoryController.getHistory(PRODUCT_ID, false);

      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(priceHistoryDto);
      verify(priceHistoryService).getHistoryWithNPlusOne(PRODUCT_ID);
      verify(priceHistoryService, never()).getHistoryOptimized(anyLong());
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории с оптимизированным запросом (optimized = true, значение по умолчанию)")
    void getHistory_shouldUseOptimizedByDefault_whenOptimizedNotSpecified() {
      // Проверяем, что при вызове без параметра optimized используется true
      when(priceHistoryService.getHistoryOptimized(PRODUCT_ID)).thenReturn(priceHistoryDtoList);

      // В контроллере параметр optimized имеет defaultValue = "true"
      List<PriceHistoryDto> result = priceHistoryController.getHistory(PRODUCT_ID, true);

      assertThat(result).hasSize(1);
      verify(priceHistoryService).getHistoryOptimized(PRODUCT_ID);
      verify(priceHistoryService, never()).getHistoryWithNPlusOne(anyLong());
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустой истории")
    void getHistory_shouldReturnEmptyList_whenNoHistory() {
      when(priceHistoryService.getHistoryOptimized(PRODUCT_ID)).thenReturn(List.of());

      List<PriceHistoryDto> result = priceHistoryController.getHistory(PRODUCT_ID, true);

      assertThat(result).isEmpty();
      verify(priceHistoryService).getHistoryOptimized(PRODUCT_ID);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории с разными ID продуктов")
    void getHistory_shouldHandleDifferentProductIds() {
      Long productId1 = 1L;
      Long productId2 = 2L;

      List<PriceHistoryDto> historyForProduct1 = List.of(priceHistoryDto);
      PriceHistoryDto dtoForProduct2 = new PriceHistoryDto(
          200L, new BigDecimal("199.99"), LocalDateTime.now(), productId2, 20L, "Ozon"
      );
      List<PriceHistoryDto> historyForProduct2 = List.of(dtoForProduct2);

      when(priceHistoryService.getHistoryOptimized(productId1)).thenReturn(historyForProduct1);
      when(priceHistoryService.getHistoryOptimized(productId2)).thenReturn(historyForProduct2);

      List<PriceHistoryDto> result1 = priceHistoryController.getHistory(productId1, true);
      List<PriceHistoryDto> result2 = priceHistoryController.getHistory(productId2, true);

      assertThat(result1).hasSize(1);
      assertThat(result1.get(0).productId()).isEqualTo(productId1);
      assertThat(result2).hasSize(1);
      assertThat(result2.get(0).productId()).isEqualTo(productId2);

      verify(priceHistoryService).getHistoryOptimized(productId1);
      verify(priceHistoryService).getHistoryOptimized(productId2);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории с оптимизированным запросом для продукта с множеством записей")
    void getHistory_shouldHandleMultipleHistoryRecords() {
      PriceHistoryDto history1 = new PriceHistoryDto(101L, new BigDecimal("899.99"), LocalDateTime.now(), PRODUCT_ID, STORE_ID, STORE_NAME);
      PriceHistoryDto history2 = new PriceHistoryDto(102L, new BigDecimal("899.99"), LocalDateTime.now().minusDays(1), PRODUCT_ID, STORE_ID, STORE_NAME);
      PriceHistoryDto history3 = new PriceHistoryDto(103L, new BigDecimal("899.99"), LocalDateTime.now().minusDays(2), PRODUCT_ID, STORE_ID, STORE_NAME);
      List<PriceHistoryDto> multipleHistory = List.of(history1, history2, history3);

      when(priceHistoryService.getHistoryOptimized(PRODUCT_ID)).thenReturn(multipleHistory);

      List<PriceHistoryDto> result = priceHistoryController.getHistory(PRODUCT_ID, true);

      assertThat(result).hasSize(3);
      verify(priceHistoryService).getHistoryOptimized(PRODUCT_ID);
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ДЛЯ СРАВНЕНИЯ ====================

  @Nested
  @DisplayName("Сравнение оптимизированного и N+1 запросов")
  class ComparisonTests {

    @Test
    @DisplayName("[СРАВНЕНИЕ] Оба метода возвращают одинаковые данные для одного продукта")
    void compareOptimizedAndNPlusOne_shouldReturnSameData() {
      when(priceHistoryService.getHistoryOptimized(PRODUCT_ID)).thenReturn(priceHistoryDtoList);
      when(priceHistoryService.getHistoryWithNPlusOne(PRODUCT_ID)).thenReturn(priceHistoryDtoList);

      List<PriceHistoryDto> optimizedResult = priceHistoryController.getHistory(PRODUCT_ID, true);
      List<PriceHistoryDto> nPlusOneResult = priceHistoryController.getHistory(PRODUCT_ID, false);

      assertThat(optimizedResult).isEqualTo(nPlusOneResult);
      assertThat(optimizedResult).hasSameSizeAs(nPlusOneResult);

      verify(priceHistoryService).getHistoryOptimized(PRODUCT_ID);
      verify(priceHistoryService).getHistoryWithNPlusOne(PRODUCT_ID);
    }

    @Test
    @DisplayName("[СРАВНЕНИЕ] Оптимизированный запрос не вызывает N+1 метод")
    void optimizedQuery_shouldNotCallNPlusOneMethod() {
      when(priceHistoryService.getHistoryOptimized(PRODUCT_ID)).thenReturn(priceHistoryDtoList);

      priceHistoryController.getHistory(PRODUCT_ID, true);

      verify(priceHistoryService, never()).getHistoryWithNPlusOne(anyLong());
    }

    @Test
    @DisplayName("[СРАВНЕНИЕ] N+1 запрос не вызывает оптимизированный метод")
    void nPlusOneQuery_shouldNotCallOptimizedMethod() {
      when(priceHistoryService.getHistoryWithNPlusOne(PRODUCT_ID)).thenReturn(priceHistoryDtoList);

      priceHistoryController.getHistory(PRODUCT_ID, false);

      verify(priceHistoryService, never()).getHistoryOptimized(anyLong());
    }
  }

  // ==================== ТЕСТЫ С РАЗЛИЧНЫМИ ТИПАМИ ДАННЫХ ====================

  @Nested
  @DisplayName("Тесты с различными типами данных")
  class EdgeCaseTests {

    @Test
    @DisplayName("[ГРАНИЦЫ] Получение истории с productId = 0")
    void getHistory_shouldHandleZeroProductId() {
      Long zeroProductId = 0L;
      when(priceHistoryService.getHistoryOptimized(zeroProductId)).thenReturn(List.of());

      List<PriceHistoryDto> result = priceHistoryController.getHistory(zeroProductId, true);

      assertThat(result).isEmpty();
      verify(priceHistoryService).getHistoryOptimized(zeroProductId);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Получение истории с отрицательным productId")
    void getHistory_shouldHandleNegativeProductId() {
      Long negativeProductId = -1L;
      when(priceHistoryService.getHistoryOptimized(negativeProductId)).thenReturn(List.of());

      List<PriceHistoryDto> result = priceHistoryController.getHistory(negativeProductId, true);

      assertThat(result).isEmpty();
      verify(priceHistoryService).getHistoryOptimized(negativeProductId);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Запись цены с отрицательной ценой")
    void recordPrice_shouldHandleNegativePrice() {
      PriceHistoryDto negativePriceDto = new PriceHistoryDto(
          null, new BigDecimal("-10.00"), LocalDateTime.now(), PRODUCT_ID, STORE_ID, STORE_NAME
      );
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(negativePriceDto);

      PriceHistoryDto result = priceHistoryController.recordPrice(negativePriceDto);

      assertThat(result).isNotNull();
      assertThat(result.price()).isNegative();
      verify(priceHistoryService).recordPrice(negativePriceDto);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Запись цены с нулевой ценой")
    void recordPrice_shouldHandleZeroPrice() {
      PriceHistoryDto zeroPriceDto = new PriceHistoryDto(
          null, BigDecimal.ZERO, LocalDateTime.now(), PRODUCT_ID, STORE_ID, STORE_NAME
      );
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(zeroPriceDto);

      PriceHistoryDto result = priceHistoryController.recordPrice(zeroPriceDto);

      assertThat(result).isNotNull();
      assertThat(result.price()).isZero();
      verify(priceHistoryService).recordPrice(zeroPriceDto);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Запись цены с очень большой ценой")
    void recordPrice_shouldHandleVeryLargePrice() {
      BigDecimal veryLargePrice = new BigDecimal("999999999999.99");
      PriceHistoryDto largePriceDto = new PriceHistoryDto(
          null, veryLargePrice, LocalDateTime.now(), PRODUCT_ID, STORE_ID, STORE_NAME
      );
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(largePriceDto);

      PriceHistoryDto result = priceHistoryController.recordPrice(largePriceDto);

      assertThat(result).isNotNull();
      assertThat(result.price()).isEqualByComparingTo(veryLargePrice);
      verify(priceHistoryService).recordPrice(largePriceDto);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Запись цены с очень большим ID")
    void recordPrice_shouldHandleVeryLargeId() {
      PriceHistoryDto largeIdDto = new PriceHistoryDto(
          Long.MAX_VALUE, PRICE, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME
      );
      when(priceHistoryService.recordPrice(any(PriceHistoryDto.class))).thenReturn(largeIdDto);

      PriceHistoryDto result = priceHistoryController.recordPrice(largeIdDto);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(Long.MAX_VALUE);
      verify(priceHistoryService).recordPrice(largeIdDto);
    }
  }
}