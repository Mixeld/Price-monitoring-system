package com.pricetracker.service.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для ProductSearchKey")
class ProductSearchKeyTest {

  // ==================== ТЕСТЫ КОНСТРУКТОРА ====================

  @Nested
  @DisplayName("Тесты конструктора")
  class ConstructorTests {

    @Test
    @DisplayName("[УСПЕХ] Создание ProductSearchKey со всеми полями")
    void constructor_shouldCreateValidRecord() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      assertThat(key.categoryName()).isEqualTo("Electronics");
      assertThat(key.minPrice()).isEqualByComparingTo("100");
      assertThat(key.maxPrice()).isEqualByComparingTo("1000");
      assertThat(key.pageNumber()).isEqualTo(0);
      assertThat(key.pageSize()).isEqualTo(10);
      assertThat(key.useNativeQuery()).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание ProductSearchKey с null значениями")
    void constructor_shouldHandleNullValues() {
      ProductSearchKey key = new ProductSearchKey(
          null,
          null,
          null,
          0,
          10,
          false
      );

      assertThat(key.categoryName()).isNull();
      assertThat(key.minPrice()).isNull();
      assertThat(key.maxPrice()).isNull();
      assertThat(key.pageNumber()).isEqualTo(0);
      assertThat(key.pageSize()).isEqualTo(10);
      assertThat(key.useNativeQuery()).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание ProductSearchKey с пустой категорией")
    void constructor_shouldHandleEmptyCategory() {
      ProductSearchKey key = new ProductSearchKey(
          "",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      assertThat(key.categoryName()).isEmpty();
      assertThat(key.minPrice()).isEqualByComparingTo("100");
      assertThat(key.maxPrice()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("[УСПЕХ] Создание ProductSearchKey с useNativeQuery = true")
    void constructor_shouldHandleUseNativeQueryTrue() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          1,
          20,
          true
      );

      assertThat(key.useNativeQuery()).isTrue();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание ProductSearchKey с нулевыми значениями")
    void constructor_shouldHandleZeroValues() {
      ProductSearchKey key = new ProductSearchKey(
          "",
          BigDecimal.ZERO,
          BigDecimal.ZERO,
          0,
          0,
          false
      );

      assertThat(key.categoryName()).isEmpty();
      assertThat(key.minPrice()).isZero();
      assertThat(key.maxPrice()).isZero();
      assertThat(key.pageNumber()).isEqualTo(0);
      assertThat(key.pageSize()).isEqualTo(0);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Создание ProductSearchKey с отрицательными значениями")
    void constructor_shouldHandleNegativeValues() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("-100"),
          new BigDecimal("-50"),
          -1,
          -5,
          false
      );

      assertThat(key.minPrice()).isNegative();
      assertThat(key.maxPrice()).isNegative();
      assertThat(key.pageNumber()).isNegative();
      assertThat(key.pageSize()).isNegative();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Создание ProductSearchKey с очень большими значениями")
    void constructor_shouldHandleLargeValues() {
      ProductSearchKey key = new ProductSearchKey(
          "A".repeat(1000),
          new BigDecimal("999999999999.99"),
          new BigDecimal("999999999999.99"),
          Integer.MAX_VALUE,
          Integer.MAX_VALUE,
          true
      );

      assertThat(key.categoryName()).hasSize(1000);
      assertThat(key.minPrice()).isEqualByComparingTo("999999999999.99");
      assertThat(key.maxPrice()).isEqualByComparingTo("999999999999.99");
      assertThat(key.pageNumber()).isEqualTo(Integer.MAX_VALUE);
      assertThat(key.pageSize()).isEqualTo(Integer.MAX_VALUE);
    }
  }

  // ==================== ТЕСТЫ МЕТОДОВ RECORD ====================

  @Nested
  @DisplayName("Тесты record методов")
  class RecordMethodTests {

    @Test
    @DisplayName("[УСПЕХ] Record методы equals и hashCode для одинаковых объектов")
    void equals_shouldReturnTrue_forEqualRecords() {
      ProductSearchKey key1 = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      ProductSearchKey key2 = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      assertThat(key1).isEqualTo(key2);
      assertThat(key1.hashCode()).isEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] Record методы equals и hashCode для разных объектов")
    void equals_shouldReturnFalse_forDifferentRecords() {
      ProductSearchKey key1 = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      ProductSearchKey key2 = new ProductSearchKey(
          "Books",
          new BigDecimal("10"),
          new BigDecimal("50"),
          1,
          5,
          true
      );

      assertThat(key1).isNotEqualTo(key2);
      assertThat(key1.hashCode()).isNotEqualTo(key2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] Record equals с null и другим типом")
    void equals_shouldReturnFalse_forNullAndDifferentType() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      assertThat(key).isNotEqualTo(null);
      assertThat(key).isNotEqualTo("some string");
    }

    @Test
    @DisplayName("[УСПЕХ] Record метод toString")
    void toString_shouldReturnFormattedString() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      String toString = key.toString();

      assertThat(toString).contains("ProductSearchKey");
      assertThat(toString).contains("categoryName=Electronics");
      assertThat(toString).contains("minPrice=100");
      assertThat(toString).contains("maxPrice=1000");
      assertThat(toString).contains("pageNumber=0");
      assertThat(toString).contains("pageSize=10");
      assertThat(toString).contains("useNativeQuery=false");
    }

    @Test
    @DisplayName("[УСПЕХ] Record метод toString с null значениями")
    void toString_shouldHandleNullValues() {
      ProductSearchKey key = new ProductSearchKey(
          null,
          null,
          null,
          0,
          10,
          false
      );

      String toString = key.toString();

      assertThat(toString).contains("ProductSearchKey");
      assertThat(toString).contains("categoryName=null");
      assertThat(toString).contains("minPrice=null");
      assertThat(toString).contains("maxPrice=null");
    }
  }

  // ==================== ТЕСТЫ ДОСТУПА К ПОЛЯМ ====================

  @Nested
  @DisplayName("Тесты доступа к полям record")
  class FieldAccessTests {

    @Test
    @DisplayName("[УСПЕХ] Доступ к полям через геттеры record")
    void fieldAccess_shouldReturnCorrectValues() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      assertThat(key.categoryName()).isEqualTo("Electronics");
      assertThat(key.minPrice()).isEqualByComparingTo("100");
      assertThat(key.maxPrice()).isEqualByComparingTo("1000");
      assertThat(key.pageNumber()).isEqualTo(0);
      assertThat(key.pageSize()).isEqualTo(10);
      assertThat(key.useNativeQuery()).isFalse();
    }
  }

  // ==================== ТЕСТЫ НА НЕИЗМЕНЯЕМОСТЬ ====================

  @Nested
  @DisplayName("Тесты на неизменяемость record")
  class ImmutabilityTests {

    @Test
    @DisplayName("[УСПЕХ] Record является неизменяемым")
    void record_shouldBeImmutable() {
      ProductSearchKey key = new ProductSearchKey(
          "Electronics",
          new BigDecimal("100"),
          new BigDecimal("1000"),
          0,
          10,
          false
      );

      // Проверяем, что значения остаются исходными
      assertThat(key.categoryName()).isEqualTo("Electronics");
      assertThat(key.minPrice()).isEqualByComparingTo("100");
      assertThat(key.maxPrice()).isEqualByComparingTo("1000");
      assertThat(key.pageNumber()).isEqualTo(0);
      assertThat(key.pageSize()).isEqualTo(10);
      assertThat(key.useNativeQuery()).isFalse();
    }
  }

  // ==================== КОМБИНИРОВАННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Комбинированные тесты")
  class CombinationTests {

    @Test
    @DisplayName("[КОМБИНАЦИИ] Создание ключа с разными комбинациями параметров")
    void combination_shouldHandleVariousCombinations() {
      // Комбинация 1: минимальные значения
      ProductSearchKey key1 = new ProductSearchKey("A", new BigDecimal("0.01"), new BigDecimal("0.01"), 0, 1, false);
      assertThat(key1.categoryName()).isEqualTo("A");
      assertThat(key1.pageSize()).isEqualTo(1);

      // Комбинация 2: максимальные значения
      ProductSearchKey key2 = new ProductSearchKey("Z".repeat(500), new BigDecimal("999999"), new BigDecimal("999999"), 1000, 1000, true);
      assertThat(key2.categoryName()).hasSize(500);
      assertThat(key2.useNativeQuery()).isTrue();

      // Комбинация 3: null значения
      ProductSearchKey key3 = new ProductSearchKey(null, null, null, 5, 15, false);
      assertThat(key3.categoryName()).isNull();
      assertThat(key3.minPrice()).isNull();
      assertThat(key3.maxPrice()).isNull();
    }

    @Test
    @DisplayName("[КОМБИНАЦИИ] Сравнение ключей с одинаковыми значениями")
    void combination_equalKeysShouldBeEqual() {
      ProductSearchKey key1 = new ProductSearchKey("Test", new BigDecimal("50"), new BigDecimal("150"), 2, 20, true);
      ProductSearchKey key2 = new ProductSearchKey("Test", new BigDecimal("50"), new BigDecimal("150"), 2, 20, true);
      ProductSearchKey key3 = new ProductSearchKey("Test", new BigDecimal("50"), new BigDecimal("150"), 2, 20, false);

      assertThat(key1).isEqualTo(key2);
      assertThat(key1).isNotEqualTo(key3);
    }
  }
}