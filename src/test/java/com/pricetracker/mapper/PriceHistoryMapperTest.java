package com.pricetracker.mapper;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для PriceHistoryMapper")
class PriceHistoryMapperTest {

  private PriceHistoryMapper priceHistoryMapper;

  private Product product;
  private Store store;
  private PriceHistory priceHistoryWithStore;
  private PriceHistory priceHistoryWithoutStore;
  private PriceHistoryDto priceHistoryDtoWithStore;
  private PriceHistoryDto priceHistoryDtoWithoutStore;

  private final Long HISTORY_ID = 100L;
  private final BigDecimal PRICE = new BigDecimal("899.99");
  private final LocalDateTime DATE_RECORDED = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
  private final Long PRODUCT_ID = 1L;
  private final Long STORE_ID = 10L;
  private final String STORE_NAME = "Amazon";

  @BeforeEach
  void setUp() {
    priceHistoryMapper = new PriceHistoryMapper();

    product = new Product();
    product.setId(PRODUCT_ID);
    product.setName("iPhone 15");

    store = new Store();
    store.setId(STORE_ID);
    store.setName(STORE_NAME);
    store.setWebsiteUrl("https://amazon.com");

    priceHistoryWithStore = new PriceHistory();
    priceHistoryWithStore.setId(HISTORY_ID);
    priceHistoryWithStore.setPrice(PRICE);
    priceHistoryWithStore.setDateRecorded(DATE_RECORDED);
    priceHistoryWithStore.setProduct(product);
    priceHistoryWithStore.setStore(store);

    priceHistoryWithoutStore = new PriceHistory();
    priceHistoryWithoutStore.setId(HISTORY_ID);
    priceHistoryWithoutStore.setPrice(PRICE);
    priceHistoryWithoutStore.setDateRecorded(DATE_RECORDED);
    priceHistoryWithoutStore.setProduct(product);
    priceHistoryWithoutStore.setStore(null);

    priceHistoryDtoWithStore = new PriceHistoryDto(
        HISTORY_ID, PRICE, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME
    );

    priceHistoryDtoWithoutStore = new PriceHistoryDto(
        HISTORY_ID, PRICE, DATE_RECORDED, PRODUCT_ID, null, null
    );
  }

  // ==================== ТЕСТЫ ДЛЯ toDto ====================

  @Nested
  @DisplayName("Тесты метода toDto(PriceHistory)")
  class ToDtoTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory в PriceHistoryDto с магазином")
    void toDto_shouldConvertHistoryToDto_withStore() {
      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(HISTORY_ID);
      assertThat(result.price()).isEqualByComparingTo(PRICE);
      assertThat(result.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(result.productId()).isEqualTo(PRODUCT_ID);
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory в PriceHistoryDto без магазина")
    void toDto_shouldConvertHistoryToDto_withoutStore() {
      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithoutStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(HISTORY_ID);
      assertThat(result.price()).isEqualByComparingTo(PRICE);
      assertThat(result.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(result.productId()).isEqualTo(PRODUCT_ID);
      assertThat(result.storeId()).isNull();
      assertThat(result.storeName()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с null продуктом")
    void toDto_shouldConvertHistoryWithNullProduct() {
      priceHistoryWithStore.setProduct(null);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(HISTORY_ID);
      assertThat(result.price()).isEqualByComparingTo(PRICE);
      assertThat(result.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(result.productId()).isNull();
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с null id")
    void toDto_shouldConvertHistoryWithNullId() {
      priceHistoryWithStore.setId(null);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.price()).isEqualByComparingTo(PRICE);
      assertThat(result.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(result.productId()).isEqualTo(PRODUCT_ID);
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с null price")
    void toDto_shouldConvertHistoryWithNullPrice() {
      priceHistoryWithStore.setPrice(null);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(HISTORY_ID);
      assertThat(result.price()).isNull();
      assertThat(result.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(result.productId()).isEqualTo(PRODUCT_ID);
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с null dateRecorded")
    void toDto_shouldConvertHistoryWithNullDate() {
      priceHistoryWithStore.setDateRecorded(null);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(HISTORY_ID);
      assertThat(result.price()).isEqualByComparingTo(PRICE);
      assertThat(result.dateRecorded()).isNull();
      assertThat(result.productId()).isEqualTo(PRODUCT_ID);
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с магазином, у которого null id")
    void toDto_shouldConvertHistoryWithStoreHavingNullId() {
      store.setId(null);
      priceHistoryWithStore.setStore(store);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.storeId()).isNull();
      assertThat(result.storeName()).isEqualTo(STORE_NAME);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с магазином, у которого null name")
    void toDto_shouldConvertHistoryWithStoreHavingNullName() {
      store.setName(null);
      priceHistoryWithStore.setStore(store);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с магазином, у которого null id и null name")
    void toDto_shouldConvertHistoryWithStoreHavingNullIdAndName() {
      store.setId(null);
      store.setName(null);
      priceHistoryWithStore.setStore(store);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.storeId()).isNull();
      assertThat(result.storeName()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistory с пустым именем магазина")
    void toDto_shouldConvertHistoryWithEmptyStoreName() {
      store.setName("");
      priceHistoryWithStore.setStore(store);

      PriceHistoryDto result = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(result).isNotNull();
      assertThat(result.storeId()).isEqualTo(STORE_ID);
      assertThat(result.storeName()).isEmpty();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null PriceHistory в null DTO")
    void toDto_shouldReturnNull_whenHistoryIsNull() {
      PriceHistoryDto result = priceHistoryMapper.toDto(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация PriceHistory со всеми null полями")
    void toDto_shouldHandleAllNullFields() {
      PriceHistory history = new PriceHistory();
      history.setId(null);
      history.setPrice(null);
      history.setDateRecorded(null);
      history.setProduct(null);
      history.setStore(null);

      PriceHistoryDto result = priceHistoryMapper.toDto(history);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.price()).isNull();
      assertThat(result.dateRecorded()).isNull();
      assertThat(result.productId()).isNull();
      assertThat(result.storeId()).isNull();
      assertThat(result.storeName()).isNull();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ toEntity ====================

  @Nested
  @DisplayName("Тесты метода toEntity(PriceHistoryDto)")
  class ToEntityTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto в PriceHistory с магазином")
    void toEntity_shouldConvertDtoToHistory_withStore() {
      PriceHistory result = priceHistoryMapper.toEntity(priceHistoryDtoWithStore);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(HISTORY_ID);
      assertThat(result.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(result.getDateRecorded()).isEqualTo(DATE_RECORDED);
      // Product и Store не устанавливаются в toEntity
      assertThat(result.getProduct()).isNull();
      assertThat(result.getStore()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto в PriceHistory без магазина")
    void toEntity_shouldConvertDtoToHistory_withoutStore() {
      PriceHistory result = priceHistoryMapper.toEntity(priceHistoryDtoWithoutStore);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(HISTORY_ID);
      assertThat(result.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(result.getDateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(result.getProduct()).isNull();
      assertThat(result.getStore()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto с null id")
    void toEntity_shouldConvertDtoWithNullId() {
      PriceHistoryDto dto = new PriceHistoryDto(null, PRICE, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(result.getDateRecorded()).isEqualTo(DATE_RECORDED);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto с null price")
    void toEntity_shouldConvertDtoWithNullPrice() {
      PriceHistoryDto dto = new PriceHistoryDto(HISTORY_ID, null, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(HISTORY_ID);
      assertThat(result.getPrice()).isNull();
      assertThat(result.getDateRecorded()).isEqualTo(DATE_RECORDED);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto с null dateRecorded")
    void toEntity_shouldConvertDtoWithNullDate() {
      PriceHistoryDto dto = new PriceHistoryDto(HISTORY_ID, PRICE, null, PRODUCT_ID, STORE_ID, STORE_NAME);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(HISTORY_ID);
      assertThat(result.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(result.getDateRecorded()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto с id = 0")
    void toEntity_shouldConvertDtoWithZeroId() {
      PriceHistoryDto dto = new PriceHistoryDto(0L, PRICE, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(0L);
      assertThat(result.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(result.getDateRecorded()).isEqualTo(DATE_RECORDED);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto с отрицательным id")
    void toEntity_shouldConvertDtoWithNegativeId() {
      PriceHistoryDto dto = new PriceHistoryDto(-1L, PRICE, DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(-1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация PriceHistoryDto с отрицательной ценой")
    void toEntity_shouldConvertDtoWithNegativePrice() {
      PriceHistoryDto dto = new PriceHistoryDto(HISTORY_ID, new BigDecimal("-10.00"), DATE_RECORDED, PRODUCT_ID, STORE_ID, STORE_NAME);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getPrice()).isEqualByComparingTo("-10.00");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null DTO в null Entity")
    void toEntity_shouldReturnNull_whenDtoIsNull() {
      PriceHistory result = priceHistoryMapper.toEntity(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация PriceHistoryDto со всеми null полями")
    void toEntity_shouldHandleAllNullFields() {
      PriceHistoryDto dto = new PriceHistoryDto(null, null, null, null, null, null);

      PriceHistory result = priceHistoryMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getPrice()).isNull();
      assertThat(result.getDateRecorded()).isNull();
      assertThat(result.getProduct()).isNull();
      assertThat(result.getStore()).isNull();
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (конвертация туда и обратно)")
  class RoundTripTests {

    @Test
    @DisplayName("[УСПЕХ] Round-trip конвертация: PriceHistory -> PriceHistoryDto -> PriceHistory с магазином")
    void roundTrip_shouldPreserveData_withStore() {
      // PriceHistory -> PriceHistoryDto
      PriceHistoryDto dto = priceHistoryMapper.toDto(priceHistoryWithStore);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo(HISTORY_ID);
      assertThat(dto.price()).isEqualByComparingTo(PRICE);
      assertThat(dto.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(dto.productId()).isEqualTo(PRODUCT_ID);
      assertThat(dto.storeId()).isEqualTo(STORE_ID);
      assertThat(dto.storeName()).isEqualTo(STORE_NAME);

      // PriceHistoryDto -> PriceHistory
      PriceHistory historyBack = priceHistoryMapper.toEntity(dto);

      assertThat(historyBack).isNotNull();
      assertThat(historyBack.getId()).isEqualTo(HISTORY_ID);
      assertThat(historyBack.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(historyBack.getDateRecorded()).isEqualTo(DATE_RECORDED);
      // Product и Store теряются при обратной конвертации
      assertThat(historyBack.getProduct()).isNull();
      assertThat(historyBack.getStore()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Round-trip конвертация: PriceHistory -> PriceHistoryDto -> PriceHistory без магазина")
    void roundTrip_shouldPreserveData_withoutStore() {
      // PriceHistory -> PriceHistoryDto
      PriceHistoryDto dto = priceHistoryMapper.toDto(priceHistoryWithoutStore);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo(HISTORY_ID);
      assertThat(dto.price()).isEqualByComparingTo(PRICE);
      assertThat(dto.dateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(dto.productId()).isEqualTo(PRODUCT_ID);
      assertThat(dto.storeId()).isNull();
      assertThat(dto.storeName()).isNull();

      // PriceHistoryDto -> PriceHistory
      PriceHistory historyBack = priceHistoryMapper.toEntity(dto);

      assertThat(historyBack).isNotNull();
      assertThat(historyBack.getId()).isEqualTo(HISTORY_ID);
      assertThat(historyBack.getPrice()).isEqualByComparingTo(PRICE);
      assertThat(historyBack.getDateRecorded()).isEqualTo(DATE_RECORDED);
      assertThat(historyBack.getProduct()).isNull();
      assertThat(historyBack.getStore()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание нового PriceHistory через DTO")
    void roundTrip_shouldCreateNewPriceHistory() {
      PriceHistoryDto newDto = new PriceHistoryDto(null, new BigDecimal("199.99"), LocalDateTime.now(), 5L, 3L, "New Store");

      PriceHistory entity = priceHistoryMapper.toEntity(newDto);

      assertThat(entity.getId()).isNull();
      assertThat(entity.getPrice()).isEqualByComparingTo("199.99");
      assertThat(entity.getDateRecorded()).isNotNull();
      assertThat(entity.getProduct()).isNull();
      assertThat(entity.getStore()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Round-trip с null значениями")
    void roundTrip_shouldHandleNullValues() {
      PriceHistory history = new PriceHistory();
      history.setId(null);
      history.setPrice(null);
      history.setDateRecorded(null);
      history.setProduct(null);
      history.setStore(null);

      // PriceHistory -> PriceHistoryDto
      PriceHistoryDto dto = priceHistoryMapper.toDto(history);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isNull();
      assertThat(dto.price()).isNull();
      assertThat(dto.dateRecorded()).isNull();
      assertThat(dto.productId()).isNull();
      assertThat(dto.storeId()).isNull();
      assertThat(dto.storeName()).isNull();

      // PriceHistoryDto -> PriceHistory
      PriceHistory historyBack = priceHistoryMapper.toEntity(dto);

      assertThat(historyBack).isNotNull();
      assertThat(historyBack.getId()).isNull();
      assertThat(historyBack.getPrice()).isNull();
      assertThat(historyBack.getDateRecorded()).isNull();
      assertThat(historyBack.getProduct()).isNull();
      assertThat(historyBack.getStore()).isNull();
    }
  }
}