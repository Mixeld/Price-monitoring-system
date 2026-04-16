package com.pricetracker.controller;

import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Store;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.StoreRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для DebugPriceHistoryController")
class DebugPriceHistoryControllerTest {

  @Mock
  private PriceHistoryRepository priceHistoryRepository;

  @Mock
  private StoreRepository storeRepository;

  @InjectMocks
  private DebugPriceHistoryController controller;

  private Store store1;
  private Store store2;
  private PriceHistory history1;
  private PriceHistory history2;
  private PriceHistory history3;

  @BeforeEach
  void setUp() {
    store1 = new Store();
    store1.setId(1L);
    store1.setName("Amazon");
    store1.setWebsiteUrl("https://amazon.com");

    store2 = new Store();
    store2.setId(2L);
    store2.setName("Ozon");
    store2.setWebsiteUrl("https://ozon.ru");

    history1 = PriceHistory.builder()
        .id(101L)
        .price(new BigDecimal("899.99"))
        .dateRecorded(LocalDateTime.of(2024, 1, 15, 10, 0))
        .store(store1)
        .build();

    history2 = PriceHistory.builder()
        .id(102L)
        .price(new BigDecimal("899.99"))
        .dateRecorded(LocalDateTime.of(2024, 1, 16, 10, 0))
        .store(store1)
        .build();

    history3 = PriceHistory.builder()
        .id(103L)
        .price(new BigDecimal("899.99"))
        .dateRecorded(LocalDateTime.of(2024, 1, 17, 10, 0))
        .store(null)
        .build();
  }

  // ==================== ТЕСТЫ ДЛЯ getPriceHistoryWithNPlus1 ====================

  @Nested
  @DisplayName("Тесты метода getPriceHistoryWithNPlus1()")
  class GetPriceHistoryWithNPlus1Tests {

    @Test
    @DisplayName("[УСПЕХ] Получение истории цен с демонстрацией N+1 проблемы")
    void getPriceHistoryWithNPlus1_shouldReturnAllHistories() {
      List<PriceHistory> histories = List.of(history1, history2, history3);
      when(priceHistoryRepository.findAll()).thenReturn(histories);

      List<PriceHistory> result = controller.getPriceHistoryWithNPlus1();

      assertThat(result).hasSize(3);
      assertThat(result).containsExactly(history1, history2, history3);
      verify(priceHistoryRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка историй")
    void getPriceHistoryWithNPlus1_shouldReturnEmptyList_whenNoHistories() {
      when(priceHistoryRepository.findAll()).thenReturn(List.of());

      List<PriceHistory> result = controller.getPriceHistoryWithNPlus1();

      assertThat(result).isEmpty();
      verify(priceHistoryRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории где у всех записей store = null")
    void getPriceHistoryWithNPlus1_shouldHandleAllNullStores() {
      PriceHistory historyWithoutStore1 = PriceHistory.builder()
          .id(201L)
          .price(new BigDecimal("100.00"))
          .dateRecorded(LocalDateTime.now())
          .store(null)
          .build();
      PriceHistory historyWithoutStore2 = PriceHistory.builder()
          .id(202L)
          .price(new BigDecimal("200.00"))
          .dateRecorded(LocalDateTime.now())
          .store(null)
          .build();

      when(priceHistoryRepository.findAll()).thenReturn(List.of(historyWithoutStore1, historyWithoutStore2));

      List<PriceHistory> result = controller.getPriceHistoryWithNPlus1();

      assertThat(result).hasSize(2);
      verify(priceHistoryRepository).findAll();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getStoreWithHistoriesNPlus1 ====================

  @Nested
  @DisplayName("Тесты метода getStoreWithHistoriesNPlus1(Long storeId)")
  class GetStoreWithHistoriesNPlus1Tests {

    @Test
    @DisplayName("[УСПЕХ] Получение магазина с демонстрацией N+1 проблемы")
    void getStoreWithHistoriesNPlus1_shouldReturnStore_whenExists() {
      when(storeRepository.findById(1L)).thenReturn(Optional.of(store1));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L))
          .thenReturn(List.of(history1, history2));

      Store result = controller.getStoreWithHistoriesNPlus1(1L);

      assertThat(result).isEqualTo(store1);
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getName()).isEqualTo("Amazon");
      verify(storeRepository).findById(1L);
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(1L);
    }

    @Test
    @DisplayName("[ОШИБКА] Выброс исключения при несуществующем магазине")
    void getStoreWithHistoriesNPlus1_shouldThrowException_whenStoreNotFound() {
      when(storeRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> controller.getStoreWithHistoriesNPlus1(99L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Store not found");

      verify(storeRepository).findById(99L);
      verify(priceHistoryRepository, never()).findByStoreIdOrderByDateRecordedDesc(anyLong());
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазина без истории цен")
    void getStoreWithHistoriesNPlus1_shouldReturnStoreWithEmptyHistory() {
      when(storeRepository.findById(2L)).thenReturn(Optional.of(store2));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(2L))
          .thenReturn(List.of());

      Store result = controller.getStoreWithHistoriesNPlus1(2L);

      assertThat(result).isEqualTo(store2);
      verify(storeRepository).findById(2L);
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(2L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getAllStoresWithHistoriesNPlus1 ====================

  @Nested
  @DisplayName("Тесты метода getAllStoresWithHistoriesNPlus1()")
  class GetAllStoresWithHistoriesNPlus1Tests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех магазинов с демонстрацией N+1 проблемы")
    void getAllStoresWithHistoriesNPlus1_shouldReturnAllStores() {
      List<Store> stores = List.of(store1, store2);
      when(storeRepository.findAll()).thenReturn(stores);
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L))
          .thenReturn(List.of(history1, history2));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(2L))
          .thenReturn(List.of(history3));

      List<Store> result = controller.getAllStoresWithHistoriesNPlus1();

      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(store1, store2);
      verify(storeRepository).findAll();
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(1L);
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(2L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка магазинов")
    void getAllStoresWithHistoriesNPlus1_shouldReturnEmptyList_whenNoStores() {
      when(storeRepository.findAll()).thenReturn(List.of());

      List<Store> result = controller.getAllStoresWithHistoriesNPlus1();

      assertThat(result).isEmpty();
      verify(storeRepository).findAll();
      verify(priceHistoryRepository, never()).findByStoreIdOrderByDateRecordedDesc(anyLong());
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазинов, у некоторых нет истории")
    void getAllStoresWithHistoriesNPlus1_shouldHandleStoresWithoutHistory() {
      Store storeWithoutHistory = new Store();
      storeWithoutHistory.setId(3L);
      storeWithoutHistory.setName("Wildberries");

      List<Store> stores = List.of(store1, storeWithoutHistory);
      when(storeRepository.findAll()).thenReturn(stores);
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L))
          .thenReturn(List.of(history1));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(3L))
          .thenReturn(List.of());

      List<Store> result = controller.getAllStoresWithHistoriesNPlus1();

      assertThat(result).hasSize(2);
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(1L);
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(3L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение одного магазина")
    void getAllStoresWithHistoriesNPlus1_shouldHandleSingleStore() {
      when(storeRepository.findAll()).thenReturn(List.of(store1));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L))
          .thenReturn(List.of(history1, history2));

      List<Store> result = controller.getAllStoresWithHistoriesNPlus1();

      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(store1);
      verify(priceHistoryRepository).findByStoreIdOrderByDateRecordedDesc(1L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getAllStoresWithHistoriesFixed ====================

  @Nested
  @DisplayName("Тесты метода getAllStoresWithHistoriesFixed()")
  class GetAllStoresWithHistoriesFixedTests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех магазинов с историей (оптимизированный запрос)")
    void getAllStoresWithHistoriesFixed_shouldReturnAllStores() {
      store1.setPriceHistories(List.of(history1, history2));
      store2.setPriceHistories(List.of(history3));
      List<Store> stores = List.of(store1, store2);
      when(storeRepository.findAll()).thenReturn(stores);

      List<Store> result = controller.getAllStoresWithHistoriesFixed();

      assertThat(result).hasSize(2);
      assertThat(result).containsExactly(store1, store2);
      verify(storeRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка магазинов")
    void getAllStoresWithHistoriesFixed_shouldReturnEmptyList_whenNoStores() {
      when(storeRepository.findAll()).thenReturn(List.of());

      List<Store> result = controller.getAllStoresWithHistoriesFixed();

      assertThat(result).isEmpty();
      verify(storeRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазинов с null списком истории")
    void getAllStoresWithHistoriesFixed_shouldHandleNullHistories() {
      store1.setPriceHistories(null);
      List<Store> stores = List.of(store1);
      when(storeRepository.findAll()).thenReturn(stores);

      List<Store> result = controller.getAllStoresWithHistoriesFixed();

      assertThat(result).hasSize(1);
      assertThat(result.get(0)).isEqualTo(store1);
      verify(storeRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазинов с пустым списком истории")
    void getAllStoresWithHistoriesFixed_shouldHandleEmptyHistories() {
      store1.setPriceHistories(new ArrayList<>());
      List<Store> stores = List.of(store1);
      when(storeRepository.findAll()).thenReturn(stores);

      List<Store> result = controller.getAllStoresWithHistoriesFixed();

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getPriceHistories()).isEmpty();
      verify(storeRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение нескольких магазинов с разным количеством истории")
    void getAllStoresWithHistoriesFixed_shouldHandleMultipleStoresWithVariousHistories() {
      store1.setPriceHistories(List.of(history1, history2));
      store2.setPriceHistories(new ArrayList<>());

      Store store3 = new Store();
      store3.setId(3L);
      store3.setName("Yandex Market");
      store3.setPriceHistories(null);

      List<Store> stores = List.of(store1, store2, store3);
      when(storeRepository.findAll()).thenReturn(stores);

      List<Store> result = controller.getAllStoresWithHistoriesFixed();

      assertThat(result).hasSize(3);
      assertThat(result.get(0).getPriceHistories()).hasSize(2);
      assertThat(result.get(1).getPriceHistories()).isEmpty();
      assertThat(result.get(2).getPriceHistories()).isNull();
      verify(storeRepository).findAll();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getPriceHistoryFixed ====================

  @Nested
  @DisplayName("Тесты метода getPriceHistoryFixed()")
  class GetPriceHistoryFixedTests {

    @Test
    @DisplayName("[УСПЕХ] Получение истории цен (оптимизированный запрос)")
    void getPriceHistoryFixed_shouldReturnAllHistories() {
      List<PriceHistory> histories = List.of(history1, history2, history3);
      when(priceHistoryRepository.findAll()).thenReturn(histories);

      List<PriceHistory> result = controller.getPriceHistoryFixed();

      assertThat(result).hasSize(3);
      assertThat(result).containsExactly(history1, history2, history3);
      verify(priceHistoryRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка историй")
    void getPriceHistoryFixed_shouldReturnEmptyList_whenNoHistories() {
      when(priceHistoryRepository.findAll()).thenReturn(List.of());

      List<PriceHistory> result = controller.getPriceHistoryFixed();

      assertThat(result).isEmpty();
      verify(priceHistoryRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории где у всех записей store = null")
    void getPriceHistoryFixed_shouldHandleAllNullStores() {
      PriceHistory historyWithoutStore1 = PriceHistory.builder()
          .id(201L)
          .price(new BigDecimal("100.00"))
          .dateRecorded(LocalDateTime.now())
          .store(null)
          .build();
      PriceHistory historyWithoutStore2 = PriceHistory.builder()
          .id(202L)
          .price(new BigDecimal("200.00"))
          .dateRecorded(LocalDateTime.now())
          .store(null)
          .build();

      when(priceHistoryRepository.findAll()).thenReturn(List.of(historyWithoutStore1, historyWithoutStore2));

      List<PriceHistory> result = controller.getPriceHistoryFixed();

      assertThat(result).hasSize(2);
      verify(priceHistoryRepository).findAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение истории с частично null store")
    void getPriceHistoryFixed_shouldHandleMixedStores() {
      when(priceHistoryRepository.findAll()).thenReturn(List.of(history1, history3));

      List<PriceHistory> result = controller.getPriceHistoryFixed();

      assertThat(result).hasSize(2);
      assertThat(result.get(0).getStore()).isEqualTo(store1);
      assertThat(result.get(1).getStore()).isNull();
      verify(priceHistoryRepository).findAll();
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ДЛЯ СРАВНЕНИЯ ====================

  @Nested
  @DisplayName("Сравнение N+1 и оптимизированных запросов")
  class ComparisonTests {

    @Test
    @DisplayName("[СРАВНЕНИЕ] N+1 запросы vs оптимизированные запросы")
    void compareNPlus1VsOptimized() {
      // Setup
      List<Store> stores = List.of(store1, store2);
      when(storeRepository.findAll()).thenReturn(stores);
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(1L))
          .thenReturn(List.of(history1, history2));
      when(priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(2L))
          .thenReturn(List.of(history3));

      // N+1 version - multiple queries
      List<Store> nPlus1Result = controller.getAllStoresWithHistoriesNPlus1();
      assertThat(nPlus1Result).hasSize(2);

      // Reset mocks for optimized version
      reset(storeRepository);

      store1.setPriceHistories(List.of(history1, history2));
      store2.setPriceHistories(List.of(history3));
      when(storeRepository.findAll()).thenReturn(stores);

      // Optimized version - single query
      List<Store> optimizedResult = controller.getAllStoresWithHistoriesFixed();
      assertThat(optimizedResult).hasSize(2);

      // Both return same data
      assertThat(nPlus1Result.get(0).getId()).isEqualTo(optimizedResult.get(0).getId());
      assertThat(nPlus1Result.get(1).getId()).isEqualTo(optimizedResult.get(1).getId());
    }

    @Test
    @DisplayName("[СРАВНЕНИЕ] N+1 запросы vs оптимизированные запросы для PriceHistory")
    void compareNPlus1VsOptimizedForPriceHistory() {
      // Setup
      List<PriceHistory> histories = List.of(history1, history2, history3);
      when(priceHistoryRepository.findAll()).thenReturn(histories);

      // N+1 version - accesses store lazily
      List<PriceHistory> nPlus1Result = controller.getPriceHistoryWithNPlus1();
      assertThat(nPlus1Result).hasSize(3);

      // Optimized version - single query with join
      List<PriceHistory> optimizedResult = controller.getPriceHistoryFixed();
      assertThat(optimizedResult).hasSize(3);

      // Both return same data
      assertThat(nPlus1Result.get(0).getId()).isEqualTo(optimizedResult.get(0).getId());
      assertThat(nPlus1Result.get(1).getId()).isEqualTo(optimizedResult.get(1).getId());
      assertThat(nPlus1Result.get(2).getId()).isEqualTo(optimizedResult.get(2).getId());
    }
  }
}