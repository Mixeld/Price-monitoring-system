package com.pricetracker.service.cache;

import com.pricetracker.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для SearchCache")
class SearchCacheTest {

  private SearchCache searchCache;
  private SearchCache.SearchKey searchKey1;
  private SearchCache.SearchKey searchKey2;
  private Page<ProductDto> testPage;

  @BeforeEach
  void setUp() {
    searchCache = new SearchCache();

    searchKey1 = SearchCache.SearchKey.builder()
        .category("Electronics")
        .minPrice(new BigDecimal("100"))
        .maxPrice(new BigDecimal("1000"))
        .page(0)
        .size(10)
        .sort("price,asc")
        .useNative(false)
        .build();

    searchKey2 = SearchCache.SearchKey.builder()
        .category("Books")
        .minPrice(new BigDecimal("10"))
        .maxPrice(new BigDecimal("50"))
        .page(1)
        .size(5)
        .sort("name,desc")
        .useNative(true)
        .build();

    testPage = new PageImpl<>(List.of(
        new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Latest iPhone", "Electronics")
    ));
  }

  // ==================== ТЕСТЫ ДЛЯ put И get ====================

  @Nested
  @DisplayName("Тесты методов put и get")
  class PutAndGetTests {

    @Test
    @DisplayName("[УСПЕХ] Сохранение и получение значения по ключу")
    void putAndGet_shouldStoreAndRetrieveValue() {
      searchCache.put(searchKey1, testPage);

      Optional<Page<ProductDto>> retrieved = searchCache.get(searchKey1);

      assertThat(retrieved).isPresent();
      assertThat(retrieved.get()).isEqualTo(testPage);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение отсутствующего ключа возвращает Optional.empty()")
    void get_shouldReturnEmpty_whenKeyNotFound() {
      Optional<Page<ProductDto>> retrieved = searchCache.get(searchKey1);

      assertThat(retrieved).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Перезапись значения по существующему ключу")
    void put_shouldOverwriteExistingValue() {
      Page<ProductDto> firstPage = new PageImpl<>(List.of());
      Page<ProductDto> secondPage = testPage;

      searchCache.put(searchKey1, firstPage);
      searchCache.put(searchKey1, secondPage);

      Optional<Page<ProductDto>> retrieved = searchCache.get(searchKey1);
      assertThat(retrieved).isPresent();
      assertThat(retrieved.get()).isEqualTo(secondPage);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ invalidateAll ====================

  @Nested
  @DisplayName("Тесты метода invalidateAll")
  class InvalidateAllTests {

    @Test
    @DisplayName("[УСПЕХ] Очистка всего кэша")
    void invalidateAll_shouldClearAllEntries() {
      searchCache.put(searchKey1, testPage);
      searchCache.put(searchKey2, testPage);

      assertThat(searchCache.getSize()).isEqualTo(2);

      searchCache.invalidateAll();

      assertThat(searchCache.getSize()).isEqualTo(0);
      assertThat(searchCache.get(searchKey1)).isEmpty();
      assertThat(searchCache.get(searchKey2)).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Очистка пустого кэша")
    void invalidateAll_shouldHandleEmptyCache() {
      assertThat(searchCache.getSize()).isEqualTo(0);

      searchCache.invalidateAll();

      assertThat(searchCache.getSize()).isEqualTo(0);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ invalidateByCategory ====================

  @Nested
  @DisplayName("Тесты метода invalidateByCategory")
  class InvalidateByCategoryTests {

    @Test
    @DisplayName("[УСПЕХ] Очистка кэша по категории")
    void invalidateByCategory_shouldClearEntriesWithMatchingCategory() {
      searchCache.put(searchKey1, testPage);  // Electronics
      searchCache.put(searchKey2, testPage);  // Books

      assertThat(searchCache.getSize()).isEqualTo(2);

      searchCache.invalidateByCategory("Electronics");

      assertThat(searchCache.getSize()).isEqualTo(1);
      assertThat(searchCache.get(searchKey1)).isEmpty();
      assertThat(searchCache.get(searchKey2)).isPresent();
    }

    @Test
    @DisplayName("[УСПЕХ] Очистка по категории, которой нет в кэше")
    void invalidateByCategory_shouldDoNothing_whenCategoryNotFound() {
      searchCache.put(searchKey1, testPage);

      searchCache.invalidateByCategory("NonExistent");

      assertThat(searchCache.getSize()).isEqualTo(1);
      assertThat(searchCache.get(searchKey1)).isPresent();
    }

    @Test
    @DisplayName("[УСПЕХ] Очистка по null категории")
    void invalidateByCategory_shouldHandleNullCategory() {
      searchCache.put(searchKey1, testPage);

      searchCache.invalidateByCategory(null);

      assertThat(searchCache.getSize()).isEqualTo(1);
      assertThat(searchCache.get(searchKey1)).isPresent();
    }

    @Test
    @DisplayName("[УСПЕХ] Очистка по категории с несколькими ключами")
    void invalidateByCategory_shouldClearAllKeysWithMatchingCategory() {
      SearchCache.SearchKey anotherElectronicsKey = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("200"))
          .maxPrice(new BigDecimal("800"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      searchCache.put(searchKey1, testPage);
      searchCache.put(anotherElectronicsKey, testPage);
      searchCache.put(searchKey2, testPage);

      assertThat(searchCache.getSize()).isEqualTo(3);

      searchCache.invalidateByCategory("Electronics");

      assertThat(searchCache.getSize()).isEqualTo(1);
      assertThat(searchCache.get(searchKey1)).isEmpty();
      assertThat(searchCache.get(anotherElectronicsKey)).isEmpty();
      assertThat(searchCache.get(searchKey2)).isPresent();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getSize ====================

  @Nested
  @DisplayName("Тесты метода getSize")
  class GetSizeTests {

    @Test
    @DisplayName("[УСПЕХ] Получение размера кэша")
    void getSize_shouldReturnCorrectSize() {
      assertThat(searchCache.getSize()).isEqualTo(0);

      searchCache.put(searchKey1, testPage);
      assertThat(searchCache.getSize()).isEqualTo(1);

      searchCache.put(searchKey2, testPage);
      assertThat(searchCache.getSize()).isEqualTo(2);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getKeys ====================

  @Nested
  @DisplayName("Тесты метода getKeys")
  class GetKeysTests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех ключей кэша")
    void getKeys_shouldReturnAllKeys() {
      searchCache.put(searchKey1, testPage);
      searchCache.put(searchKey2, testPage);

      Set<SearchCache.SearchKey> keys = searchCache.getKeys();

      assertThat(keys).hasSize(2);
      assertThat(keys).contains(searchKey1, searchKey2);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение ключей из пустого кэша")
    void getKeys_shouldReturnEmptySet_whenCacheEmpty() {
      Set<SearchCache.SearchKey> keys = searchCache.getKeys();

      assertThat(keys).isEmpty();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ containsKey ====================

  @Nested
  @DisplayName("Тесты метода containsKey")
  class ContainsKeyTests {

    @Test
    @DisplayName("[УСПЕХ] Проверка существующего ключа")
    void containsKey_shouldReturnTrue_whenKeyExists() {
      searchCache.put(searchKey1, testPage);

      assertThat(searchCache.containsKey(searchKey1)).isTrue();
    }

    @Test
    @DisplayName("[УСПЕХ] Проверка отсутствующего ключа")
    void containsKey_shouldReturnFalse_whenKeyDoesNotExist() {
      assertThat(searchCache.containsKey(searchKey1)).isFalse();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ clear ====================

  @Nested
  @DisplayName("Тесты метода clear")
  class ClearTests {

    @Test
    @DisplayName("[УСПЕХ] Очистка кэша")
    void clear_shouldRemoveAllEntries() {
      searchCache.put(searchKey1, testPage);
      searchCache.put(searchKey2, testPage);

      assertThat(searchCache.getSize()).isEqualTo(2);

      searchCache.clear();

      assertThat(searchCache.getSize()).isEqualTo(0);
      assertThat(searchCache.get(searchKey1)).isEmpty();
      assertThat(searchCache.get(searchKey2)).isEmpty();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ SearchKey ====================

  @Nested
  @DisplayName("Тесты внутреннего класса SearchKey")
  class SearchKeyTests {

    @Test
    @DisplayName("[УСПЕХ] Создание SearchKey через Builder")
    void searchKeyBuilder_shouldCreateValidKey() {
      SearchCache.SearchKey key = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key.getCategory()).isEqualTo("Electronics");
      assertThat(key.getMinPrice()).isEqualByComparingTo("100");
      assertThat(key.getMaxPrice()).isEqualByComparingTo("1000");
      assertThat(key.getPage()).isEqualTo(0);
      assertThat(key.getSize()).isEqualTo(10);
      assertThat(key.getSort()).isEqualTo("price,asc");
      assertThat(key.isUseNative()).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание SearchKey с null значениями")
    void searchKeyBuilder_shouldHandleNullValues() {
      SearchCache.SearchKey key = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(null)
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      assertThat(key.getCategory()).isNull();
      assertThat(key.getMinPrice()).isNull();
      assertThat(key.getMaxPrice()).isNull();
      assertThat(key.getSort()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - одинаковые объекты")
    void equals_shouldReturnTrue_forSameObject() {
      assertThat(searchKey1.equals(searchKey1)).isTrue();
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - null")
    void equals_shouldReturnFalse_forNull() {
      assertThat(searchKey1.equals(null)).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - другой тип")
    void equals_shouldReturnFalse_forDifferentType() {
      assertThat(searchKey1.equals("string")).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - одинаковые значения полей")
    void equals_shouldReturnTrue_forEqualKeys() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные категории")
    void equals_shouldReturnFalse_forDifferentCategory() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Books")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные minPrice")
    void equals_shouldReturnFalse_forDifferentMinPrice() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("200"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные maxPrice")
    void equals_shouldReturnFalse_forDifferentMaxPrice() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("2000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные page")
    void equals_shouldReturnFalse_forDifferentPage() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(1)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные size")
    void equals_shouldReturnFalse_forDifferentSize() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(20)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные sort")
    void equals_shouldReturnFalse_forDifferentSort() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("name,desc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - разные useNative")
    void equals_shouldReturnFalse_forDifferentUseNative() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(true)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - с null полями")
    void equals_shouldHandleNullFields() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(null)
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(null)
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      assertThat(key1).isEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - один null category, другой не null")
    void equals_shouldReturnFalse_whenOneCategoryNull() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - один null minPrice, другой не null")
    void equals_shouldReturnFalse_whenOneMinPriceNull() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(null)
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - один null maxPrice, другой не null")
    void equals_shouldReturnFalse_whenOneMaxPriceNull() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] Equals - один null sort, другой не null")
    void equals_shouldReturnFalse_whenOneSortNull() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1).isNotEqualTo(key2);
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode - одинаковые объекты имеют одинаковый hashCode")
    void hashCode_shouldBeEqual_forEqualKeys() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode - с null полями")
    void hashCode_shouldHandleNullFields() {
      SearchCache.SearchKey key1 = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(null)
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      SearchCache.SearchKey key2 = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(null)
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] Метод toString - нормальный вывод")
    void searchKey_toString_shouldReturnFormattedString() {
      SearchCache.SearchKey key = SearchCache.SearchKey.builder()
          .category("Electronics")
          .minPrice(new BigDecimal("100"))
          .maxPrice(new BigDecimal("1000"))
          .page(0)
          .size(10)
          .sort("price,asc")
          .useNative(false)
          .build();

      String toString = key.toString();

      assertThat(toString).contains("SearchCache.SearchKey");
      assertThat(toString).contains("category=Electronics");
      assertThat(toString).contains("minPrice=100");
      assertThat(toString).contains("maxPrice=1000");
      assertThat(toString).contains("page=0");
      assertThat(toString).contains("size=10");
      assertThat(toString).contains("sort=price,asc");
      assertThat(toString).contains("useNative=false");
    }

    @Test
    @DisplayName("[УСПЕХ] Метод toString с null значениями")
    void searchKey_toString_shouldHandleNullValues() {
      SearchCache.SearchKey key = SearchCache.SearchKey.builder()
          .category(null)
          .minPrice(null)
          .maxPrice(null)
          .page(0)
          .size(10)
          .sort(null)
          .useNative(false)
          .build();

      String toString = key.toString();

      assertThat(toString).contains("SearchCache.SearchKey");
      assertThat(toString).contains("category=null");
      assertThat(toString).contains("minPrice=null");
      assertThat(toString).contains("maxPrice=null");
      assertThat(toString).contains("page=0");
      assertThat(toString).contains("size=10");
      assertThat(toString).contains("sort=null");
      assertThat(toString).contains("useNative=false");
    }

    @Test
    @DisplayName("[УСПЕХ] Builder методы возвращают правильные значения")
    void searchKeyBuilderMethods_shouldSetCorrectValues() {
      SearchCache.SearchKey.SearchKeyBuilder builder = SearchCache.SearchKey.builder();

      assertThat(builder.category("Test")).isSameAs(builder);
      assertThat(builder.minPrice(new BigDecimal("10"))).isSameAs(builder);
      assertThat(builder.maxPrice(new BigDecimal("100"))).isSameAs(builder);
      assertThat(builder.page(5)).isSameAs(builder);
      assertThat(builder.size(20)).isSameAs(builder);
      assertThat(builder.sort("id,desc")).isSameAs(builder);
      assertThat(builder.useNative(true)).isSameAs(builder);

      SearchCache.SearchKey key = builder.build();
      assertThat(key.getCategory()).isEqualTo("Test");
      assertThat(key.getMinPrice()).isEqualByComparingTo("10");
      assertThat(key.getMaxPrice()).isEqualByComparingTo("100");
      assertThat(key.getPage()).isEqualTo(5);
      assertThat(key.getSize()).isEqualTo(20);
      assertThat(key.getSort()).isEqualTo("id,desc");
      assertThat(key.isUseNative()).isTrue();
    }

    @Test
    @DisplayName("[УСПЕХ] toString() метод билдера")
    void builder_toString_shouldReturnFormattedString() {
      SearchCache.SearchKey.SearchKeyBuilder builder = SearchCache.SearchKey.builder();

      // Устанавливаем значения через билдер
      builder.category("Test")
          .minPrice(new BigDecimal("10"))
          .maxPrice(new BigDecimal("100"))
          .page(5)
          .size(20)
          .sort("id,desc")
          .useNative(true);

      String toString = builder.toString();

      // Проверяем, что toString() билдера возвращает строку (не null и не пустую)
      assertThat(toString).isNotNull();
      assertThat(toString).isNotBlank();
      // Lombok генерирует toString для билдера в формате:
      // SearchCache.SearchKey.SearchKeyBuilder(category=Test, minPrice=10, maxPrice=100, page=5, size=20, sort=id,desc, useNative=true)
      assertThat(toString).contains("SearchKeyBuilder");
      assertThat(toString).contains("category=Test");
      assertThat(toString).contains("minPrice=10");
      assertThat(toString).contains("maxPrice=100");
      assertThat(toString).contains("page=5");
      assertThat(toString).contains("size=20");
      assertThat(toString).contains("sort=id,desc");
      assertThat(toString).contains("useNative=true");
    }

    @Test
    @DisplayName("[УСПЕХ] toString() метод билдера с null значениями")
    void builder_toString_shouldHandleNullValues() {
      SearchCache.SearchKey.SearchKeyBuilder builder = SearchCache.SearchKey.builder();

      // Не устанавливаем значения (все null/default)
      String toString = builder.toString();

      assertThat(toString).isNotNull();
      assertThat(toString).contains("SearchKeyBuilder");
      assertThat(toString).contains("category=null");
      assertThat(toString).contains("minPrice=null");
      assertThat(toString).contains("maxPrice=null");
      assertThat(toString).contains("page=0");
      assertThat(toString).contains("size=0");
      assertThat(toString).contains("sort=null");
      assertThat(toString).contains("useNative=false");
    }
  }
}