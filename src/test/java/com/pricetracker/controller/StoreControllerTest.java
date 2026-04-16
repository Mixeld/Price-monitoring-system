package com.pricetracker.controller;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для StoreController")
class StoreControllerTest {

  @Mock
  private StoreService storeService;

  @InjectMocks
  private StoreController storeController;

  private StoreDto storeDto;
  private List<StoreDto> storeDtoList;

  @BeforeEach
  void setUp() {
    storeDto = new StoreDto(1L, "Amazon", "https://amazon.com");
    storeDtoList = List.of(storeDto);
  }

  // ==================== ТЕСТЫ ДЛЯ getAllStores ====================

  @Nested
  @DisplayName("Тесты метода getAllStores()")
  class GetAllStoresTests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех магазинов")
    void getAllStores_shouldReturnAllStores() {
      when(storeService.getAllStores()).thenReturn(storeDtoList);

      ResponseEntity<List<StoreDto>> response = storeController.getAllStores();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      assertThat(response.getBody().get(0)).isEqualTo(storeDto);
      verify(storeService).getAllStores();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка магазинов")
    void getAllStores_shouldReturnEmptyList_whenNoStores() {
      when(storeService.getAllStores()).thenReturn(List.of());

      ResponseEntity<List<StoreDto>> response = storeController.getAllStores();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(storeService).getAllStores();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение нескольких магазинов")
    void getAllStores_shouldReturnMultipleStores() {
      StoreDto store2 = new StoreDto(2L, "Ozon", "https://ozon.ru");
      StoreDto store3 = new StoreDto(3L, "Wildberries", "https://wildberries.ru");
      List<StoreDto> multipleStores = List.of(storeDto, store2, store3);

      when(storeService.getAllStores()).thenReturn(multipleStores);

      ResponseEntity<List<StoreDto>> response = storeController.getAllStores();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(3);
      assertThat(response.getBody()).containsExactly(storeDto, store2, store3);
      verify(storeService).getAllStores();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getStoreById ====================

  @Nested
  @DisplayName("Тесты метода getStoreById(Long id)")
  class GetStoreByIdTests {

    @Test
    @DisplayName("[УСПЕХ] Получение магазина по ID")
    void getStoreById_shouldReturnStore_whenExists() {
      when(storeService.getStoreById(1L)).thenReturn(storeDto);

      ResponseEntity<StoreDto> response = storeController.getStoreById(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(storeDto);
      verify(storeService).getStoreById(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазина с ID = 0")
    void getStoreById_shouldHandleZeroId() {
      StoreDto zeroIdStore = new StoreDto(0L, "Zero Store", "https://zero.com");
      when(storeService.getStoreById(0L)).thenReturn(zeroIdStore);

      ResponseEntity<StoreDto> response = storeController.getStoreById(0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(0L);
      verify(storeService).getStoreById(0L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазина с большим ID")
    void getStoreById_shouldHandleLargeId() {
      Long largeId = Long.MAX_VALUE;
      StoreDto largeIdStore = new StoreDto(largeId, "Large ID Store", "https://large.com");
      when(storeService.getStoreById(largeId)).thenReturn(largeIdStore);

      ResponseEntity<StoreDto> response = storeController.getStoreById(largeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(largeId);
      verify(storeService).getStoreById(largeId);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getStoreByName ====================

  @Nested
  @DisplayName("Тесты метода getStoreByName(String name)")
  class GetStoreByNameTests {

    @Test
    @DisplayName("[УСПЕХ] Получение магазина по имени")
    void getStoreByName_shouldReturnStore_whenExists() {
      when(storeService.getStoreByName("Amazon")).thenReturn(storeDto);

      ResponseEntity<StoreDto> response = storeController.getStoreByName("Amazon");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(storeDto);
      verify(storeService).getStoreByName("Amazon");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазина с именем в нижнем регистре")
    void getStoreByName_shouldHandleLowercaseName() {
      StoreDto lowercaseStore = new StoreDto(1L, "amazon", "https://amazon.com");
      when(storeService.getStoreByName("amazon")).thenReturn(lowercaseStore);

      ResponseEntity<StoreDto> response = storeController.getStoreByName("amazon");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("amazon");
      verify(storeService).getStoreByName("amazon");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазина с именем в верхнем регистре")
    void getStoreByName_shouldHandleUppercaseName() {
      StoreDto uppercaseStore = new StoreDto(1L, "AMAZON", "https://amazon.com");
      when(storeService.getStoreByName("AMAZON")).thenReturn(uppercaseStore);

      ResponseEntity<StoreDto> response = storeController.getStoreByName("AMAZON");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("AMAZON");
      verify(storeService).getStoreByName("AMAZON");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение магазина с именем содержащим пробелы")
    void getStoreByName_shouldHandleNameWithSpaces() {
      String nameWithSpaces = "Best Buy";
      StoreDto spacedStore = new StoreDto(1L, nameWithSpaces, "https://bestbuy.com");
      when(storeService.getStoreByName(nameWithSpaces)).thenReturn(spacedStore);

      ResponseEntity<StoreDto> response = storeController.getStoreByName(nameWithSpaces);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo(nameWithSpaces);
      verify(storeService).getStoreByName(nameWithSpaces);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ searchStores ====================

  @Nested
  @DisplayName("Тесты метода searchStores(String query)")
  class SearchStoresTests {

    @Test
    @DisplayName("[УСПЕХ] Поиск магазинов по запросу")
    void searchStores_shouldReturnMatchingStores() {
      when(storeService.searchStoresByName("amaz")).thenReturn(storeDtoList);

      ResponseEntity<List<StoreDto>> response = storeController.searchStores("amaz");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      verify(storeService).searchStoresByName("amaz");
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск магазинов с пустым результатом")
    void searchStores_shouldReturnEmptyList_whenNoMatches() {
      when(storeService.searchStoresByName("nonexistent")).thenReturn(List.of());

      ResponseEntity<List<StoreDto>> response = storeController.searchStores("nonexistent");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(storeService).searchStoresByName("nonexistent");
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск магазинов с пустым запросом")
    void searchStores_shouldHandleEmptyQuery() {
      when(storeService.searchStoresByName("")).thenReturn(List.of());

      ResponseEntity<List<StoreDto>> response = storeController.searchStores("");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(storeService).searchStoresByName("");
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск магазинов с запросом из одного символа")
    void searchStores_shouldHandleSingleCharacterQuery() {
      when(storeService.searchStoresByName("a")).thenReturn(storeDtoList);

      ResponseEntity<List<StoreDto>> response = storeController.searchStores("a");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      verify(storeService).searchStoresByName("a");
    }
  }

  // ==================== ТЕСТЫ ДЛЯ createStore ====================

  @Nested
  @DisplayName("Тесты метода createStore(StoreDto storeDto)")
  class CreateStoreTests {

    @Test
    @DisplayName("[УСПЕХ] Создание магазина")
    void createStore_shouldCreateAndReturnCreatedStore() {
      StoreDto createdStore = new StoreDto(1L, "Amazon", "https://amazon.com");
      when(storeService.createStore(any(StoreDto.class))).thenReturn(createdStore);

      ResponseEntity<StoreDto> response = storeController.createStore(storeDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(createdStore);
      assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/stores/1"));
      verify(storeService).createStore(storeDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание магазина с null id")
    void createStore_shouldCreateStoreWithNullId() {
      StoreDto newStore = new StoreDto(null, "New Store", "https://newstore.com");
      StoreDto createdStore = new StoreDto(5L, "New Store", "https://newstore.com");
      when(storeService.createStore(newStore)).thenReturn(createdStore);

      ResponseEntity<StoreDto> response = storeController.createStore(newStore);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().id()).isEqualTo(5L);
      assertThat(response.getBody().name()).isEqualTo("New Store");
      assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/stores/5"));
      verify(storeService).createStore(newStore);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание магазина с пустым URL")
    void createStore_shouldCreateStoreWithEmptyUrl() {
      StoreDto emptyUrlStore = new StoreDto(null, "Store", "");
      StoreDto createdStore = new StoreDto(2L, "Store", "");
      when(storeService.createStore(emptyUrlStore)).thenReturn(createdStore);

      ResponseEntity<StoreDto> response = storeController.createStore(emptyUrlStore);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().websiteUrl()).isEmpty();
      verify(storeService).createStore(emptyUrlStore);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание магазина с null URL")
    void createStore_shouldCreateStoreWithNullUrl() {
      StoreDto nullUrlStore = new StoreDto(null, "Store", null);
      StoreDto createdStore = new StoreDto(3L, "Store", null);
      when(storeService.createStore(nullUrlStore)).thenReturn(createdStore);

      ResponseEntity<StoreDto> response = storeController.createStore(nullUrlStore);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().websiteUrl()).isNull();
      verify(storeService).createStore(nullUrlStore);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ updateStore ====================

  @Nested
  @DisplayName("Тесты метода updateStore(Long id, StoreDto storeDto)")
  class UpdateStoreTests {

    @Test
    @DisplayName("[УСПЕХ] Обновление магазина")
    void updateStore_shouldUpdateAndReturnUpdatedStore() {
      StoreDto updatedStore = new StoreDto(1L, "Amazon Prime", "https://prime.amazon.com");
      when(storeService.updateStore(eq(1L), any(StoreDto.class))).thenReturn(updatedStore);

      ResponseEntity<StoreDto> response = storeController.updateStore(1L, storeDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(updatedStore);
      verify(storeService).updateStore(1L, storeDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление магазина с новым именем")
    void updateStore_shouldUpdateStoreWithNewName() {
      StoreDto updateDto = new StoreDto(1L, "New Store Name", "https://amazon.com");
      StoreDto updatedStore = new StoreDto(1L, "New Store Name", "https://amazon.com");
      when(storeService.updateStore(eq(1L), any(StoreDto.class))).thenReturn(updatedStore);

      ResponseEntity<StoreDto> response = storeController.updateStore(1L, updateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().name()).isEqualTo("New Store Name");
      verify(storeService).updateStore(1L, updateDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление магазина с новым URL")
    void updateStore_shouldUpdateStoreWithNewUrl() {
      StoreDto updateDto = new StoreDto(1L, "Amazon", "https://new-amazon.com");
      StoreDto updatedStore = new StoreDto(1L, "Amazon", "https://new-amazon.com");
      when(storeService.updateStore(eq(1L), any(StoreDto.class))).thenReturn(updatedStore);

      ResponseEntity<StoreDto> response = storeController.updateStore(1L, updateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().websiteUrl()).isEqualTo("https://new-amazon.com");
      verify(storeService).updateStore(1L, updateDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление магазина с null URL")
    void updateStore_shouldUpdateStoreWithNullUrl() {
      StoreDto updateDto = new StoreDto(1L, "Amazon", null);
      StoreDto updatedStore = new StoreDto(1L, "Amazon", null);
      when(storeService.updateStore(eq(1L), any(StoreDto.class))).thenReturn(updatedStore);

      ResponseEntity<StoreDto> response = storeController.updateStore(1L, updateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().websiteUrl()).isNull();
      verify(storeService).updateStore(1L, updateDto);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ deleteStore ====================

  @Nested
  @DisplayName("Тесты метода deleteStore(Long id)")
  class DeleteStoreTests {

    @Test
    @DisplayName("[УСПЕХ] Удаление магазина")
    void deleteStore_shouldDeleteAndReturnNoContent() {
      doNothing().when(storeService).deleteStore(1L);

      ResponseEntity<Void> response = storeController.deleteStore(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(storeService).deleteStore(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление магазина с ID = 0")
    void deleteStore_shouldHandleZeroId() {
      doNothing().when(storeService).deleteStore(0L);

      ResponseEntity<Void> response = storeController.deleteStore(0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(storeService).deleteStore(0L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление магазина с большим ID")
    void deleteStore_shouldHandleLargeId() {
      Long largeId = Long.MAX_VALUE;
      doNothing().when(storeService).deleteStore(largeId);

      ResponseEntity<Void> response = storeController.deleteStore(largeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(storeService).deleteStore(largeId);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getPriceHistoryCount ====================

  @Nested
  @DisplayName("Тесты метода getPriceHistoryCount(Long id)")
  class GetPriceHistoryCountTests {

    @Test
    @DisplayName("[УСПЕХ] Получение количества записей истории цен")
    void getPriceHistoryCount_shouldReturnCount() {
      when(storeService.getPriceHistoryCount(1L)).thenReturn(10L);
      when(storeService.getStoreById(1L)).thenReturn(storeDto);

      ResponseEntity<Map<String, Object>> response = storeController.getPriceHistoryCount(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("storeId", 1L);
      assertThat(response.getBody()).containsEntry("storeName", "Amazon");
      assertThat(response.getBody()).containsEntry("priceHistoryCount", 10L);
      verify(storeService).getPriceHistoryCount(1L);
      verify(storeService).getStoreById(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение количества записей истории цен для магазина без истории")
    void getPriceHistoryCount_shouldReturnZero_whenNoHistory() {
      when(storeService.getPriceHistoryCount(1L)).thenReturn(0L);
      when(storeService.getStoreById(1L)).thenReturn(storeDto);

      ResponseEntity<Map<String, Object>> response = storeController.getPriceHistoryCount(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("priceHistoryCount", 0L);
      verify(storeService).getPriceHistoryCount(1L);
      verify(storeService).getStoreById(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение количества записей для магазина с большим количеством истории")
    void getPriceHistoryCount_shouldHandleLargeCount() {
      long largeCount = 999999L;
      when(storeService.getPriceHistoryCount(1L)).thenReturn(largeCount);
      when(storeService.getStoreById(1L)).thenReturn(storeDto);

      ResponseEntity<Map<String, Object>> response = storeController.getPriceHistoryCount(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("priceHistoryCount", largeCount);
      verify(storeService).getPriceHistoryCount(1L);
      verify(storeService).getStoreById(1L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ checkStoreExists ====================

  @Nested
  @DisplayName("Тесты метода checkStoreExists(String name, String websiteUrl)")
  class CheckStoreExistsTests {

    @Test
    @DisplayName("[УСПЕХ] Проверка существования магазина по имени")
    void checkStoreExists_shouldCheckByNameOnly() {
      when(storeService.existsByName("Amazon")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("Amazon", null);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("nameExists", true);
      assertThat(response.getBody()).doesNotContainKey("websiteUrlExists");
      verify(storeService).existsByName("Amazon");
      verify(storeService, never()).existsByWebsiteUrl(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка существования магазина по URL")
    void checkStoreExists_shouldCheckByUrlOnly() {
      when(storeService.existsByWebsiteUrl("https://amazon.com")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists(null, "https://amazon.com");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("websiteUrlExists", true);
      assertThat(response.getBody()).doesNotContainKey("nameExists");
      verify(storeService).existsByWebsiteUrl("https://amazon.com");
      verify(storeService, never()).existsByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка существования магазина по имени и URL")
    void checkStoreExists_shouldCheckBothNameAndUrl() {
      when(storeService.existsByName("Amazon")).thenReturn(true);
      when(storeService.existsByWebsiteUrl("https://amazon.com")).thenReturn(false);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("Amazon", "https://amazon.com");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("nameExists", true);
      assertThat(response.getBody()).containsEntry("websiteUrlExists", false);
      verify(storeService).existsByName("Amazon");
      verify(storeService).existsByWebsiteUrl("https://amazon.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка когда магазин не существует")
    void checkStoreExists_shouldReturnFalse_whenStoreDoesNotExist() {
      when(storeService.existsByName("Nonexistent")).thenReturn(false);
      when(storeService.existsByWebsiteUrl("https://nonexistent.com")).thenReturn(false);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("Nonexistent", "https://nonexistent.com");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("nameExists", false);
      assertThat(response.getBody()).containsEntry("websiteUrlExists", false);
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка с пустым именем")
    void checkStoreExists_shouldSkipCheck_whenNameIsEmpty() {
      when(storeService.existsByWebsiteUrl("https://amazon.com")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("", "https://amazon.com");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("websiteUrlExists", true);
      assertThat(response.getBody()).doesNotContainKey("nameExists");
      verify(storeService, never()).existsByName(anyString());
      verify(storeService).existsByWebsiteUrl("https://amazon.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка с именем из пробелов")
    void checkStoreExists_shouldSkipCheck_whenNameIsBlank() {
      when(storeService.existsByWebsiteUrl("https://amazon.com")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("   ", "https://amazon.com");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("websiteUrlExists", true);
      assertThat(response.getBody()).doesNotContainKey("nameExists");
      verify(storeService, never()).existsByName(anyString());
      verify(storeService).existsByWebsiteUrl("https://amazon.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка с пустым URL")
    void checkStoreExists_shouldSkipCheck_whenUrlIsEmpty() {
      when(storeService.existsByName("Amazon")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("Amazon", "");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("nameExists", true);
      assertThat(response.getBody()).doesNotContainKey("websiteUrlExists");
      verify(storeService).existsByName("Amazon");
      verify(storeService, never()).existsByWebsiteUrl(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка с URL из пробелов")
    void checkStoreExists_shouldSkipCheck_whenUrlIsBlank() {
      when(storeService.existsByName("Amazon")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("Amazon", "   ");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).containsEntry("nameExists", true);
      assertThat(response.getBody()).doesNotContainKey("websiteUrlExists");
      verify(storeService).existsByName("Amazon");
      verify(storeService, never()).existsByWebsiteUrl(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка с null параметрами")
    void checkStoreExists_shouldReturnEmptyMap_whenBothParamsNull() {
      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists(null, null);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(storeService, never()).existsByName(anyString());
      verify(storeService, never()).existsByWebsiteUrl(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка с обоими пустыми параметрами")
    void checkStoreExists_shouldReturnEmptyMap_whenBothParamsEmpty() {
      ResponseEntity<Map<String, Boolean>> response = storeController.checkStoreExists("", "");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(storeService, never()).existsByName(anyString());
      verify(storeService, never()).existsByWebsiteUrl(anyString());
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (цепочки вызовов)")
  class IntegrationTests {

    @Test
    @DisplayName("[ИНТЕГРАЦИЯ] Полный CRUD цикл для магазина")
    void fullCrudCycle_shouldWorkCorrectly() {
      // 1. Create
      StoreDto newStore = new StoreDto(null, "Test Store", "https://test.com");
      StoreDto createdStore = new StoreDto(10L, "Test Store", "https://test.com");
      when(storeService.createStore(newStore)).thenReturn(createdStore);

      ResponseEntity<StoreDto> createResponse = storeController.createStore(newStore);
      assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      Long createdId = createResponse.getBody().id();

      // 2. Get by ID
      when(storeService.getStoreById(createdId)).thenReturn(createdStore);
      ResponseEntity<StoreDto> getResponse = storeController.getStoreById(createdId);
      assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(getResponse.getBody().name()).isEqualTo("Test Store");

      // 3. Update
      StoreDto updateDto = new StoreDto(createdId, "Updated Store", "https://updated.com");
      StoreDto updatedStore = new StoreDto(createdId, "Updated Store", "https://updated.com");
      when(storeService.updateStore(eq(createdId), any(StoreDto.class))).thenReturn(updatedStore);

      ResponseEntity<StoreDto> updateResponse = storeController.updateStore(createdId, updateDto);
      assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(updateResponse.getBody().name()).isEqualTo("Updated Store");

      // 4. Get price history count
      when(storeService.getPriceHistoryCount(createdId)).thenReturn(0L);
      when(storeService.getStoreById(createdId)).thenReturn(updatedStore);

      ResponseEntity<Map<String, Object>> countResponse = storeController.getPriceHistoryCount(createdId);
      assertThat(countResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(countResponse.getBody()).containsEntry("priceHistoryCount", 0L);

      // 5. Delete
      doNothing().when(storeService).deleteStore(createdId);
      ResponseEntity<Void> deleteResponse = storeController.deleteStore(createdId);
      assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      // Verifications - getStoreById is called twice: once in getStoreById and once in getPriceHistoryCount
      verify(storeService).createStore(newStore);
      verify(storeService, times(2)).getStoreById(createdId); // Changed from times(1) to times(2)
      verify(storeService).updateStore(createdId, updateDto);
      verify(storeService).getPriceHistoryCount(createdId);
      verify(storeService).deleteStore(createdId);
    }

    @Test
    @DisplayName("[ИНТЕГРАЦИЯ] Создание и проверка существования магазина")
    void createAndCheckStore_shouldWorkCorrectly() {
      // 1. Check before create - should not exist
      when(storeService.existsByName("New Store")).thenReturn(false);
      when(storeService.existsByWebsiteUrl("https://newstore.com")).thenReturn(false);

      ResponseEntity<Map<String, Boolean>> checkBefore = storeController.checkStoreExists("New Store", "https://newstore.com");
      assertThat(checkBefore.getBody()).containsEntry("nameExists", false);
      assertThat(checkBefore.getBody()).containsEntry("websiteUrlExists", false);

      // 2. Create store
      StoreDto newStore = new StoreDto(null, "New Store", "https://newstore.com");
      StoreDto createdStore = new StoreDto(5L, "New Store", "https://newstore.com");
      when(storeService.createStore(newStore)).thenReturn(createdStore);
      storeController.createStore(newStore);

      // 3. Check after create - should exist
      when(storeService.existsByName("New Store")).thenReturn(true);
      when(storeService.existsByWebsiteUrl("https://newstore.com")).thenReturn(true);

      ResponseEntity<Map<String, Boolean>> checkAfter = storeController.checkStoreExists("New Store", "https://newstore.com");
      assertThat(checkAfter.getBody()).containsEntry("nameExists", true);
      assertThat(checkAfter.getBody()).containsEntry("websiteUrlExists", true);

      verify(storeService, times(2)).existsByName("New Store");
      verify(storeService, times(2)).existsByWebsiteUrl("https://newstore.com");
      verify(storeService).createStore(newStore);
    }
  }
}