package com.pricetracker.mapper;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unit-тесты для StoreMapper")
class StoreMapperTest {

  private StoreMapper storeMapper;

  private Store existingStore;
  private StoreDto storeDto;

  @BeforeEach
  void setUp() {
    storeMapper = new StoreMapper();

    existingStore = new Store();
    existingStore.setId(1L);
    existingStore.setName("Amazon");
    existingStore.setWebsiteUrl("https://amazon.com");

    storeDto = new StoreDto(1L, "Amazon", "https://amazon.com");
  }

  // ==================== ТЕСТЫ ДЛЯ toDto ====================

  @Nested
  @DisplayName("Тесты метода toDto(Store)")
  class ToDtoTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store в StoreDto")
    void toDto_shouldConvertStoreToDto() {
      StoreDto result = storeMapper.toDto(existingStore);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.name()).isEqualTo("Amazon");
      assertThat(result.websiteUrl()).isEqualTo("https://amazon.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с null id")
    void toDto_shouldConvertStoreWithNullId() {
      Store store = new Store();
      store.setId(null);
      store.setName("Ozon");
      store.setWebsiteUrl("https://ozon.ru");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.name()).isEqualTo("Ozon");
      assertThat(result.websiteUrl()).isEqualTo("https://ozon.ru");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с null name")
    void toDto_shouldConvertStoreWithNullName() {
      Store store = new Store();
      store.setId(2L);
      store.setName(null);
      store.setWebsiteUrl("https://wildberries.ru");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(2L);
      assertThat(result.name()).isNull();
      assertThat(result.websiteUrl()).isEqualTo("https://wildberries.ru");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с пустым name")
    void toDto_shouldConvertStoreWithEmptyName() {
      Store store = new Store();
      store.setId(3L);
      store.setName("");
      store.setWebsiteUrl("https://example.com");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(3L);
      assertThat(result.name()).isEmpty();
      assertThat(result.websiteUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с name из пробелов")
    void toDto_shouldConvertStoreWithBlankName() {
      Store store = new Store();
      store.setId(4L);
      store.setName("   ");
      store.setWebsiteUrl("https://example.com");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(4L);
      assertThat(result.name()).isEqualTo("   ");
      assertThat(result.websiteUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с null websiteUrl")
    void toDto_shouldConvertStoreWithNullWebsiteUrl() {
      Store store = new Store();
      store.setId(5L);
      store.setName("Yandex Market");
      store.setWebsiteUrl(null);

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(5L);
      assertThat(result.name()).isEqualTo("Yandex Market");
      assertThat(result.websiteUrl()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с пустым websiteUrl")
    void toDto_shouldConvertStoreWithEmptyWebsiteUrl() {
      Store store = new Store();
      store.setId(6L);
      store.setName("Store");
      store.setWebsiteUrl("");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(6L);
      assertThat(result.name()).isEqualTo("Store");
      assertThat(result.websiteUrl()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Store с websiteUrl из пробелов")
    void toDto_shouldConvertStoreWithBlankWebsiteUrl() {
      Store store = new Store();
      store.setId(7L);
      store.setName("Store");
      store.setWebsiteUrl("   ");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(7L);
      assertThat(result.name()).isEqualTo("Store");
      assertThat(result.websiteUrl()).isEqualTo("   ");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null Store в null DTO")
    void toDto_shouldReturnNull_whenStoreIsNull() {
      StoreDto result = storeMapper.toDto(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация Store со всеми null полями")
    void toDto_shouldHandleAllNullFields() {
      Store store = new Store();
      store.setId(null);
      store.setName(null);
      store.setWebsiteUrl(null);

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.name()).isNull();
      assertThat(result.websiteUrl()).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация Store с id = 0")
    void toDto_shouldConvertStoreWithZeroId() {
      Store store = new Store();
      store.setId(0L);
      store.setName("Zero ID Store");
      store.setWebsiteUrl("https://example.com");

      StoreDto result = storeMapper.toDto(store);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(0L);
      assertThat(result.name()).isEqualTo("Zero ID Store");
      assertThat(result.websiteUrl()).isEqualTo("https://example.com");
    }
  }

  // ==================== ТЕСТЫ ДЛЯ toEntity ====================

  @Nested
  @DisplayName("Тесты метода toEntity(StoreDto)")
  class ToEntityTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto в Store")
    void toEntity_shouldConvertDtoToStore() {
      Store result = storeMapper.toEntity(storeDto);

      assertThat(result).isNotNull();
      // ID не должен устанавливаться (это ответственность базы данных)
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Amazon");
      assertThat(result.getWebsiteUrl()).isEqualTo("https://amazon.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с null id")
    void toEntity_shouldConvertDtoWithNullId() {
      StoreDto dto = new StoreDto(null, "Ozon", "https://ozon.ru");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Ozon");
      assertThat(result.getWebsiteUrl()).isEqualTo("https://ozon.ru");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с null name")
    void toEntity_shouldConvertDtoWithNullName() {
      StoreDto dto = new StoreDto(1L, null, "https://example.com");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isNull();
      assertThat(result.getWebsiteUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с пустым name")
    void toEntity_shouldConvertDtoWithEmptyName() {
      StoreDto dto = new StoreDto(1L, "", "https://example.com");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEmpty();
      assertThat(result.getWebsiteUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с name из пробелов")
    void toEntity_shouldConvertDtoWithBlankName() {
      StoreDto dto = new StoreDto(1L, "   ", "https://example.com");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("   ");
      assertThat(result.getWebsiteUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с null websiteUrl")
    void toEntity_shouldConvertDtoWithNullWebsiteUrl() {
      StoreDto dto = new StoreDto(1L, "Store", null);

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Store");
      assertThat(result.getWebsiteUrl()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с пустым websiteUrl")
    void toEntity_shouldConvertDtoWithEmptyWebsiteUrl() {
      StoreDto dto = new StoreDto(1L, "Store", "");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Store");
      assertThat(result.getWebsiteUrl()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация StoreDto с websiteUrl из пробелов")
    void toEntity_shouldConvertDtoWithBlankWebsiteUrl() {
      StoreDto dto = new StoreDto(1L, "Store", "   ");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isEqualTo("Store");
      assertThat(result.getWebsiteUrl()).isEqualTo("   ");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация StoreDto с id = 0")
    void toEntity_shouldConvertDtoWithZeroId() {
      StoreDto dto = new StoreDto(0L, "Zero ID Store", "https://example.com");

      Store result = storeMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull(); // ID не копируется
      assertThat(result.getName()).isEqualTo("Zero ID Store");
      assertThat(result.getWebsiteUrl()).isEqualTo("https://example.com");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null DTO в null Entity")
    void toEntity_shouldReturnNull_whenDtoIsNull() {
      Store result = storeMapper.toEntity(null);

      assertThat(result).isNull();
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (конвертация туда и обратно)")
  class RoundTripTests {

    @Test
    @DisplayName("[УСПЕХ] Round-trip конвертация: Store -> StoreDto -> Store")
    void roundTrip_shouldPreserveData() {
      // Store -> StoreDto
      StoreDto dto = storeMapper.toDto(existingStore);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo(1L);
      assertThat(dto.name()).isEqualTo("Amazon");
      assertThat(dto.websiteUrl()).isEqualTo("https://amazon.com");

      // StoreDto -> Store
      Store storeBack = storeMapper.toEntity(dto);

      assertThat(storeBack).isNotNull();
      assertThat(storeBack.getId()).isNull(); // ID не восстанавливается
      assertThat(storeBack.getName()).isEqualTo("Amazon");
      assertThat(storeBack.getWebsiteUrl()).isEqualTo("https://amazon.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Создание нового Store через DTO")
    void roundTrip_shouldCreateNewStore() {
      StoreDto newStoreDto = new StoreDto(null, "New Store", "https://newstore.com");

      Store entity = storeMapper.toEntity(newStoreDto);

      assertThat(entity.getId()).isNull();
      assertThat(entity.getName()).isEqualTo("New Store");
      assertThat(entity.getWebsiteUrl()).isEqualTo("https://newstore.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Round-trip с null значениями")
    void roundTrip_shouldHandleNullValues() {
      Store store = new Store();
      store.setId(null);
      store.setName(null);
      store.setWebsiteUrl(null);

      // Store -> StoreDto
      StoreDto dto = storeMapper.toDto(store);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isNull();
      assertThat(dto.name()).isNull();
      assertThat(dto.websiteUrl()).isNull();

      // StoreDto -> Store
      Store storeBack = storeMapper.toEntity(dto);

      assertThat(storeBack).isNotNull();
      assertThat(storeBack.getId()).isNull();
      assertThat(storeBack.getName()).isNull();
      assertThat(storeBack.getWebsiteUrl()).isNull();
    }
  }
}