package com.pricetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для PriceHistory entity")
class PriceHistoryTest {

  private final Long ID = 100L;
  private final BigDecimal PRICE = new BigDecimal("899.99");
  private final LocalDateTime DATE_RECORDED = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
  private final Product product = new Product();
  private final Store store = new Store();

  // ==================== ТЕСТЫ КОНСТРУКТОРОВ И БАЗОВЫХ СВОЙСТВ ====================

  @Nested
  @DisplayName("Тесты конструкторов и базовых свойств")
  class BasicTests {
    @Test
    @DisplayName("Конструкторы и билдер корректно создают объект")
    void constructorsAndBuilder_shouldCreateObject() {
      PriceHistory builderHistory = PriceHistory.builder().id(ID).price(PRICE).build();
      assertThat(builderHistory.getId()).isEqualTo(ID);

      PriceHistory allArgsHistory = new PriceHistory(ID, product, store, PRICE, DATE_RECORDED);
      assertThat(allArgsHistory.getProduct()).isEqualTo(product);

      PriceHistory noArgsHistory = new PriceHistory();
      assertThat(noArgsHistory.getId()).isNull();
    }
  }

  // ======================================================================
  // ===== БЛОК ДЛЯ ДОСТИЖЕНИЯ 100% ПОКРЫТИЯ КОДА, СГЕНЕРИРОВАННОГО LOMBOK =====
  // ======================================================================

  @Nested
  @DisplayName("Тесты для 100% покрытия Lombok")
  class FullLombokCoverageTests {

    private PriceHistory history1;
    private PriceHistory history2;

    @BeforeEach
    void setUp() {
      history1 = new PriceHistory(ID, product, store, PRICE, DATE_RECORDED);
      history2 = new PriceHistory(ID, product, store, PRICE, DATE_RECORDED);
    }

    // --- 1. Базовые тесты equals ---

    @Test
    @DisplayName("[EQUALS] true для идентичных объектов")
    void equals_isTrue_forIdenticalObjects() {
      assertThat(history1).isEqualTo(history2);
    }

    @Test
    @DisplayName("[EQUALS] false для null и других типов")
    void equals_isFalse_forNullAndOtherTypes() {
      assertThat(history1.equals(null)).isFalse();
      assertThat(history1.equals("a string")).isFalse();
    }

    // --- 2. Тесты на асимметричный null (одно поле null, другое нет) ---

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у одного объекта поле null")
    void equals_isFalse_whenOneObjectFieldIsNull() {
      history1.setId(null);
      assertThat(history1).isNotEqualTo(history2);
      history1.setId(ID); // Возвращаем обратно

      history1.setProduct(null);
      assertThat(history1).isNotEqualTo(history2);
      history1.setProduct(product);

      history1.setStore(null);
      assertThat(history1).isNotEqualTo(history2);
      history1.setStore(store);

      history1.setPrice(null);
      assertThat(history1).isNotEqualTo(history2);
      history1.setPrice(PRICE);

      history1.setDateRecorded(null);
      assertThat(history1).isNotEqualTo(history2);
    }

    // --- 3. Тесты на симметричный null (оба поля null) ---

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если оба id null")
    void equals_isTrue_whenBothIdsAreNull() {
      history1.setId(null);
      history2.setId(null);
      assertThat(history1).isEqualTo(history2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если оба product null")
    void equals_isTrue_whenBothProductsAreNull() {
      history1.setProduct(null);
      history2.setProduct(null);
      assertThat(history1).isEqualTo(history2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если оба store null")
    void equals_isTrue_whenBothStoresAreNull() {
      history1.setStore(null);
      history2.setStore(null);
      assertThat(history1).isEqualTo(history2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если оба price null")
    void equals_isTrue_whenBothPricesAreNull() {
      history1.setPrice(null);
      history2.setPrice(null);
      assertThat(history1).isEqualTo(history2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если оба dateRecorded null")
    void equals_isTrue_whenBothDatesAreNull() {
      history1.setDateRecorded(null);
      history2.setDateRecorded(null);
      assertThat(history1).isEqualTo(history2);
    }

    // --- 4. Тест на наследование для `canEqual` ---

    @lombok.EqualsAndHashCode(callSuper = true)
    class SubPriceHistory extends PriceHistory {

    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false при сравнении с дочерним классом")
    void equals_isFalse_forSubclassInstance() {
      SubPriceHistory subHistory = new SubPriceHistory();
      assertThat(history1.equals(subHistory)).isFalse();
    }

    // --- 5. Тесты для `hashCode`, `toString` и `Builder.toString` ---

    @Test
    @DisplayName("[ПОКРЫТИЕ] Контракт hashCode: равные объекты имеют равные хэш-коды")
    void hashCode_isEqual_forEqualObjects() {
      assertThat(history1.hashCode()).isEqualTo(history2.hashCode());
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] hashCode и toString работают с полностью null объектом")
    void hashCodeAndToString_workWithAllNullFields() {
      PriceHistory allNull = new PriceHistory(null, null, null, null, null);

      // Покрываем `null`-ветки в hashCode
      assertThat(allNull.hashCode()).isNotZero();

      // Покрываем `null`-ветки в toString
      assertThat(allNull.toString()).contains("id=null", "product=null", "store=null");
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] toString() билдера работает")
    void builderToString_works() {
      String builderStr = PriceHistory.builder().id(1L).price(PRICE).toString();
      assertThat(builderStr).contains("PriceHistory.PriceHistoryBuilder", "id=1", "price=899.99");
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у второго объекта поле null")
    void equals_isFalse_whenSecondObjectFieldIsNull() {
      // history1 имеет все не-null поля из @BeforeEach

      history2.setId(null);
      assertThat(history1).isNotEqualTo(history2);
      history2.setId(ID); // Возвращаем обратно

      history2.setProduct(null);
      assertThat(history1).isNotEqualTo(history2);
      history2.setProduct(product);

      history2.setStore(null);
      assertThat(history1).isNotEqualTo(history2);
      history2.setStore(store);

      history2.setPrice(null);
      assertThat(history1).isNotEqualTo(history2);
      history2.setPrice(PRICE);

      history2.setDateRecorded(null);
      assertThat(history1).isNotEqualTo(history2);
    }
  }
}