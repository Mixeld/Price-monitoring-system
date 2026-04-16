package com.pricetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для Product entity (100% @Data покрытие)")
class ProductTest {

  private final Long ID = 1L;
  private final String NAME = "iPhone 15";
  private final BigDecimal PRICE = new BigDecimal("999.99");
  private final String DESCRIPTION = "Latest iPhone";
  private final Category category = new Category(10L, "Electronics", null);
  private final PriceHistory priceHistory1 = new PriceHistory();
  private final LocalDateTime CREATED_AT = LocalDateTime.now();
  private final LocalDateTime UPDATED_AT = LocalDateTime.now();

  // ======================================================================
  // ===== ФИНАЛЬНЫЙ БЛОК ДЛЯ 100% ПОКРЫТИЯ @Data =====
  // ======================================================================

  @Nested
  @DisplayName("Тесты для 100% покрытия @Data")
  class FullLombokCoverageTests {

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
      product1 = new Product(ID, NAME, PRICE, DESCRIPTION, category, new ArrayList<>(List.of(priceHistory1)), CREATED_AT, UPDATED_AT);
      product2 = new Product(ID, NAME, PRICE, DESCRIPTION, category, new ArrayList<>(List.of(priceHistory1)), CREATED_AT, UPDATED_AT);
    }

    // --- 1. Базовые тесты ---

    @Test
    @DisplayName("[БАЗА] Конструкторы и геттеры/сеттеры")
    void basicMethodsWork() {
      Product p1 = new Product();
      p1.setId(ID);
      assertThat(p1.getId()).isEqualTo(ID);
      assertThat(p1.getPriceHistories()).isNotNull().isEmpty();

      Product p2 = new Product(ID, NAME, PRICE, null, null, null, null, null);
      assertThat(p2.getName()).isEqualTo(NAME);
    }

    @Test
    @DisplayName("[EQUALS] Базовые проверки: идентичность, null, другой тип, canEqual")
    void equals_basicChecks() {
      assertThat(product1).isEqualTo(product2);
      assertThat(product1).isNotEqualTo(null);
      assertThat(product1).isNotEqualTo("a string");

      @lombok.EqualsAndHashCode(callSuper = true)
      class SubProduct extends Product {}
      assertThat(product1.equals(new SubProduct())).isFalse();
    }

    // --- Покрытие веток с разными значениями для КАЖДОГО поля ---

    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном ID") void equals_falseOnId() { product2.setId(99L); assertThat(product1).isNotEqualTo(product2); }
    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном Name") void equals_falseOnName() { product2.setName("Other"); assertThat(product1).isNotEqualTo(product2); }
    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном Price") void equals_falseOnPrice() { product2.setPrice(BigDecimal.ONE); assertThat(product1).isNotEqualTo(product2); }
    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном Description") void equals_falseOnDescription() { product2.setDescription("Other"); assertThat(product1).isNotEqualTo(product2); }

    // ИСПРАВЛЕННЫЙ ТЕСТ
    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разной Category")
    void equals_falseOnCategory() {
      Category differentCategory = new Category(99L, "Books", null); // Создаем ГАРАНТИРОВАННО другую категорию
      product2.setCategory(differentCategory);
      assertThat(product1).isNotEqualTo(product2);
    }

    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном PriceHistories") void equals_falseOnPriceHistories() { product2.setPriceHistories(new ArrayList<>()); assertThat(product1).isNotEqualTo(product2); }
    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном CreatedAt") void equals_falseOnCreatedAt() { product2.setCreatedAt(LocalDateTime.MIN); assertThat(product1).isNotEqualTo(product2); }
    @Test @DisplayName("[ПОКРЫТИЕ] equals: false при разном UpdatedAt") void equals_falseOnUpdatedAt() { product2.setUpdatedAt(LocalDateTime.MIN); assertThat(product1).isNotEqualTo(product2); }

    // --- Покрытие веток с null-значениями ---

    @Test @DisplayName("[ПОКРЫТИЕ] equals: false, если поле null у первого объекта")
    void equals_isFalse_whenFirstHasNull() {
      product1.setId(null);
      assertThat(product1).isNotEqualTo(product2);
    }

    @Test @DisplayName("[ПОКРЫТИЕ] equals: false, если поле null у второго объекта")
    void equals_isFalse_whenSecondHasNull() {
      product2.setId(null);
      assertThat(product1).isNotEqualTo(product2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, когда оба поля null")
    void equals_isTrue_whenBothFieldsAreNull() {
      product1.setId(null);
      product2.setId(null);
      assertThat(product1).isEqualTo(product2);
    }

    // --- 3. Тесты для `hashCode` и `toString` ---

    @Test
    @DisplayName("[ПОКРЫТИЕ] Контракт hashCode и работа с null")
    void hashCode_contractAndNulls() {
      assertThat(product1.hashCode()).isEqualTo(product2.hashCode());

      Product allNull = new Product(null, null, null, null, null, null, null, null);
      assertThat(allNull.hashCode()).isNotZero();
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] toString работает с null и не-null полями")
    void toString_worksWithNullAndNonNulls() {
      assertThat(product1.toString()).contains(NAME);

      Product allNull = new Product(null, null, null, null, null, null, null, null);
      assertThat(allNull.toString()).contains("id=null");
    }
  }
}