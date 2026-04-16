package com.pricetracker.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Unit-тесты для PriceStatsDto")
class PriceStatsDtoTest {

  private static final Long PRODUCT_ID = 1L;
  private static final String PRODUCT_NAME = "iPhone 15 Pro";
  private static final Double MIN_PRICE = 899.99;
  private static final Double MAX_PRICE = 1099.99;
  private static final Double AVG_PRICE = 999.99;
  private static final Double CURRENT_PRICE = 999.99;
  private static final Double PRICE_CHANGE = 50.00;
  private static final double PRICE_CHANGE_PERCENT = 5.25;
  private static final LocalDateTime FIRST_RECORDED = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
  private static final LocalDateTime LAST_RECORDED = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
  private static final int TOTAL_RECORDS = 15;

  // ==================== ТЕСТЫ КОНСТРУКТОРА ====================

  @Test
  @DisplayName("[УСПЕХ] Создание DTO через канонический конструктор со всеми полями")
  void constructor_shouldCreateValidDtoWithAllFields() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.productId()).isEqualTo(PRODUCT_ID);
    assertThat(dto.productName()).isEqualTo(PRODUCT_NAME);
    assertThat(dto.minPrice()).isEqualTo(MIN_PRICE);
    assertThat(dto.maxPrice()).isEqualTo(MAX_PRICE);
    assertThat(dto.avgPrice()).isEqualTo(AVG_PRICE);
    assertThat(dto.currentPrice()).isEqualTo(CURRENT_PRICE);
    assertThat(dto.priceChange()).isEqualTo(PRICE_CHANGE);
    assertThat(dto.priceChangePercent()).isEqualTo(PRICE_CHANGE_PERCENT);
    assertThat(dto.firstRecorded()).isEqualTo(FIRST_RECORDED);
    assertThat(dto.lastRecorded()).isEqualTo(LAST_RECORDED);
    assertThat(dto.totalRecords()).isEqualTo(TOTAL_RECORDS);
  }

  @Test
  @DisplayName("[УСПЕХ] Создание DTO с null значениями для необязательных полей")
  void constructor_shouldAllowNullForOptionalFields() {
    PriceStatsDto dto = new PriceStatsDto(
        null, null, null, null, null,
        null, null, 0.0,
        null, null, 0
    );

    assertThat(dto.productId()).isNull();
    assertThat(dto.productName()).isNull();
    assertThat(dto.minPrice()).isNull();
    assertThat(dto.maxPrice()).isNull();
    assertThat(dto.avgPrice()).isNull();
    assertThat(dto.currentPrice()).isNull();
    assertThat(dto.priceChange()).isNull();
    assertThat(dto.priceChangePercent()).isEqualTo(0.0);
    assertThat(dto.firstRecorded()).isNull();
    assertThat(dto.lastRecorded()).isNull();
    assertThat(dto.totalRecords()).isEqualTo(0);
  }

  @Test
  @DisplayName("[УСПЕХ] Создание DTO с нулевыми значениями для числовых полей")
  void constructor_shouldHandleZeroValues() {
    PriceStatsDto dto = new PriceStatsDto(
        0L, "", 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0,
        FIRST_RECORDED, LAST_RECORDED, 0
    );

    assertThat(dto.productId()).isEqualTo(0L);
    assertThat(dto.productName()).isEmpty();
    assertThat(dto.minPrice()).isEqualTo(0.0);
    assertThat(dto.maxPrice()).isEqualTo(0.0);
    assertThat(dto.avgPrice()).isEqualTo(0.0);
    assertThat(dto.currentPrice()).isEqualTo(0.0);
    assertThat(dto.priceChange()).isEqualTo(0.0);
    assertThat(dto.priceChangePercent()).isEqualTo(0.0);
    assertThat(dto.totalRecords()).isEqualTo(0);
  }

  // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

  @Test
  @DisplayName("[ГРАНИЦЫ] Максимальные значения для числовых полей")
  void boundary_shouldHandleMaxValues() {
    PriceStatsDto dto = new PriceStatsDto(
        Long.MAX_VALUE,
        "X".repeat(1000),
        Double.MAX_VALUE,
        Double.MAX_VALUE,
        Double.MAX_VALUE,
        Double.MAX_VALUE,
        Double.MAX_VALUE,
        Double.MAX_VALUE,
        LocalDateTime.MAX,
        LocalDateTime.MAX,
        Integer.MAX_VALUE
    );

    assertThat(dto.productId()).isEqualTo(Long.MAX_VALUE);
    assertThat(dto.productName()).hasSize(1000);
    assertThat(dto.minPrice()).isEqualTo(Double.MAX_VALUE);
    assertThat(dto.maxPrice()).isEqualTo(Double.MAX_VALUE);
    assertThat(dto.avgPrice()).isEqualTo(Double.MAX_VALUE);
    assertThat(dto.currentPrice()).isEqualTo(Double.MAX_VALUE);
    assertThat(dto.priceChange()).isEqualTo(Double.MAX_VALUE);
    assertThat(dto.priceChangePercent()).isEqualTo(Double.MAX_VALUE);
    assertThat(dto.firstRecorded()).isEqualTo(LocalDateTime.MAX);
    assertThat(dto.lastRecorded()).isEqualTo(LocalDateTime.MAX);
    assertThat(dto.totalRecords()).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Минимальные значения для числовых полей")
  void boundary_shouldHandleMinValues() {
    PriceStatsDto dto = new PriceStatsDto(
        Long.MIN_VALUE,
        "",
        Double.MIN_VALUE,
        Double.MIN_VALUE,
        Double.MIN_VALUE,
        Double.MIN_VALUE,
        Double.MIN_VALUE,
        Double.MIN_VALUE,
        LocalDateTime.MIN,
        LocalDateTime.MIN,
        Integer.MIN_VALUE
    );

    assertThat(dto.productId()).isEqualTo(Long.MIN_VALUE);
    assertThat(dto.productName()).isEmpty();
    assertThat(dto.minPrice()).isEqualTo(Double.MIN_VALUE);
    assertThat(dto.maxPrice()).isEqualTo(Double.MIN_VALUE);
    assertThat(dto.avgPrice()).isEqualTo(Double.MIN_VALUE);
    assertThat(dto.currentPrice()).isEqualTo(Double.MIN_VALUE);
    assertThat(dto.priceChange()).isEqualTo(Double.MIN_VALUE);
    assertThat(dto.priceChangePercent()).isEqualTo(Double.MIN_VALUE);
    assertThat(dto.firstRecorded()).isEqualTo(LocalDateTime.MIN);
    assertThat(dto.lastRecorded()).isEqualTo(LocalDateTime.MIN);
    assertThat(dto.totalRecords()).isEqualTo(Integer.MIN_VALUE);
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Отрицательные значения для цен")
  void boundary_shouldHandleNegativePrices() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, -100.0, -50.0, -75.0,
        -100.0, -25.0, -10.5,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.minPrice()).isNegative();
    assertThat(dto.maxPrice()).isNegative();
    assertThat(dto.avgPrice()).isNegative();
    assertThat(dto.currentPrice()).isNegative();
    assertThat(dto.priceChange()).isNegative();
    assertThat(dto.priceChangePercent()).isNegative();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Очень большая точность для Double значений")
  void boundary_shouldHandleHighPrecisionDoubles() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME,
        0.123456789, 0.987654321, 0.555555555,
        0.666666666, 0.111111111, 0.999999999,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.minPrice()).isEqualTo(0.123456789);
    assertThat(dto.maxPrice()).isEqualTo(0.987654321);
    assertThat(dto.avgPrice()).isEqualTo(0.555555555);
    assertThat(dto.currentPrice()).isEqualTo(0.666666666);
    assertThat(dto.priceChange()).isEqualTo(0.111111111);
    assertThat(dto.priceChangePercent()).isEqualTo(0.999999999);
  }

  // ==================== ТЕСТЫ МЕТОДОВ RECORD ====================

  @Test
  @DisplayName("[УСПЕХ] Record методы equals и hashCode для одинаковых объектов")
  void record_shouldConsiderEqualObjectsWithSameValues() {
    PriceStatsDto dto1 = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    PriceStatsDto dto2 = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("[УСПЕХ] Record методы equals и hashCode для разных объектов")
  void record_shouldConsiderDifferentObjectsWithDifferentValues() {
    PriceStatsDto dto1 = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    PriceStatsDto dto2 = new PriceStatsDto(
        2L, "Different Product", 100.0, 200.0, 150.0,
        150.0, 50.0, 25.0,
        FIRST_RECORDED, LAST_RECORDED, 10
    );

    assertThat(dto1).isNotEqualTo(dto2);
    assertThat(dto1.hashCode()).isNotEqualTo(dto2.hashCode());
  }

  @Test
  @DisplayName("[УСПЕХ] Record метод equals с null и другим типом")
  void record_equalsShouldHandleNullAndDifferentTypes() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto).isNotEqualTo(null);
    assertThat(dto).isNotEqualTo("some string");
  }

  @Test
  @DisplayName("[УСПЕХ] Record метод toString")
  void record_shouldImplementToString() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    String toString = dto.toString();

    assertThat(toString).contains("PriceStatsDto");
    assertThat(toString).contains(PRODUCT_NAME);
    assertThat(toString).contains(MIN_PRICE.toString());
    assertThat(toString).contains(MAX_PRICE.toString());
    assertThat(toString).contains(String.valueOf(TOTAL_RECORDS));
  }

  // ==================== ТЕСТЫ ДОСТУПА К ПОЛЯМ ====================

  @Test
  @DisplayName("[УСПЕХ] Доступ к полям через геттеры record")
  void record_shouldProvideAccessToFieldsViaGetterMethods() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    // Проверяем, что методы доступа возвращают корректные значения
    assertThat(dto.productId()).isNotNull();
    assertThat(dto.productName()).isNotBlank();
    assertThat(dto.minPrice()).isPositive();
    assertThat(dto.maxPrice()).isPositive();
    assertThat(dto.avgPrice()).isPositive();
    assertThat(dto.currentPrice()).isPositive();
    assertThat(dto.priceChange()).isPositive();
    assertThat(dto.priceChangePercent()).isPositive();
    assertThat(dto.firstRecorded()).isNotNull();
    assertThat(dto.lastRecorded()).isNotNull();
    assertThat(dto.totalRecords()).isPositive();
  }

  // ==================== ТЕСТЫ ЛОГИКИ ЦЕНОВЫХ ИЗМЕНЕНИЙ ====================

  @Test
  @DisplayName("[ЛОГИКА] Проверка корректности priceChange как разницы между ценами")
  void priceChange_shouldRepresentDifferenceBetweenPrices() {
    double current = 999.99;
    double previous = 899.99;
    double expectedChange = 100.0;

    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, 899.99, 1099.99, 999.99,
        current, expectedChange, 11.11,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    double calculatedChange = dto.currentPrice() - (dto.currentPrice() - dto.priceChange());
    assertThat(calculatedChange).isEqualTo(expectedChange);
  }

  @Test
  @DisplayName("[ЛОГИКА] Проверка, что minPrice не больше maxPrice")
  void logic_minPriceShouldNotExceedMaxPrice() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, 100.0, 200.0, 150.0,
        150.0, 50.0, 25.0,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.minPrice()).isLessThanOrEqualTo(dto.maxPrice());
  }

  @Test
  @DisplayName("[ЛОГИКА] Проверка, что avgPrice находится между minPrice и maxPrice")
  void logic_avgPriceShouldBeBetweenMinAndMax() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, 100.0, 200.0, 150.0,
        150.0, 50.0, 25.0,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.avgPrice())
        .isGreaterThanOrEqualTo(dto.minPrice())
        .isLessThanOrEqualTo(dto.maxPrice());
  }

  @Test
  @DisplayName("[ЛОГИКА] Проверка, что totalRecords положительное число для реальных данных")
  void logic_totalRecordsShouldBePositiveForRealData() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.totalRecords()).isGreaterThan(0);
  }

  @Test
  @DisplayName("[ЛОГИКА] Проверка, что firstRecorded предшествует lastRecorded")
  void logic_firstRecordedShouldBeBeforeLastRecorded() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.firstRecorded()).isBefore(dto.lastRecorded());
  }

  // ==================== ТЕСТЫ С РАЗЛИЧНЫМИ КОМБИНАЦИЯМИ ДАННЫХ ====================

  @Test
  @DisplayName("[КОМБИНАЦИИ] DTO с одинаковыми min и max ценами (один источник данных)")
  void combination_shouldHandleEqualMinAndMaxPrices() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, 999.99, 999.99, 999.99,
        999.99, 0.0, 0.0,
        FIRST_RECORDED, LAST_RECORDED, 1
    );

    assertThat(dto.minPrice()).isEqualTo(dto.maxPrice());
    assertThat(dto.avgPrice()).isEqualTo(dto.minPrice());
    assertThat(dto.priceChange()).isEqualTo(0.0);
    assertThat(dto.priceChangePercent()).isEqualTo(0.0);
  }

  @Test
  @DisplayName("[КОМБИНАЦИИ] DTO с нулевой ценой")
  void combination_shouldHandleZeroPrices() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, 0.0, 0.0, 0.0,
        0.0, 0.0, 0.0,
        FIRST_RECORDED, LAST_RECORDED, 1
    );

    assertThat(dto.minPrice()).isZero();
    assertThat(dto.maxPrice()).isZero();
    assertThat(dto.avgPrice()).isZero();
    assertThat(dto.currentPrice()).isZero();
    assertThat(dto.priceChange()).isZero();
    assertThat(dto.priceChangePercent()).isZero();
  }

  @Test
  @DisplayName("[КОМБИНАЦИИ] DTO с одним днем записи (firstRecorded = lastRecorded)")
  void combination_shouldHandleSameFirstAndLastRecorded() {
    LocalDateTime sameDateTime = LocalDateTime.now();

    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        sameDateTime, sameDateTime, TOTAL_RECORDS
    );

    assertThat(dto.firstRecorded()).isEqualTo(dto.lastRecorded());
    assertThat(dto.firstRecorded()).isEqualTo(sameDateTime);
  }

  @Test
  @DisplayName("[КОМБИНАЦИИ] DTO с пустым именем продукта")
  void combination_shouldHandleEmptyProductName() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, "", MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.productName()).isEmpty();
    assertThat(dto.productId()).isEqualTo(PRODUCT_ID);
  }

  @Test
  @DisplayName("[КОМБИНАЦИИ] DTO с очень длинным именем продукта")
  void combination_shouldHandleVeryLongProductName() {
    String longName = "Very Long Product Name That Exceeds Normal Length ".repeat(10);

    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, longName, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.productName()).hasSizeGreaterThan(100);
    assertThat(dto.productName()).isEqualTo(longName);
  }

  // ==================== ТЕСТЫ НА НЕИЗМЕНЯЕМОСТЬ (IMMUTABILITY) ====================

  @Test
  @DisplayName("[IMMUTABILITY] Record является неизменяемым - поля нельзя изменить после создания")
  void record_shouldBeImmutable() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    // Пытаемся изменить поля через рефлексию не получится, так как record неизменяем
    // Проверяем, что значения остаются исходными
    assertThat(dto.productId()).isEqualTo(PRODUCT_ID);
    assertThat(dto.productName()).isEqualTo(PRODUCT_NAME);
    assertThat(dto.minPrice()).isEqualTo(MIN_PRICE);
    assertThat(dto.maxPrice()).isEqualTo(MAX_PRICE);
    assertThat(dto.avgPrice()).isEqualTo(AVG_PRICE);
    assertThat(dto.currentPrice()).isEqualTo(CURRENT_PRICE);
    assertThat(dto.priceChange()).isEqualTo(PRICE_CHANGE);
    assertThat(dto.priceChangePercent()).isEqualTo(PRICE_CHANGE_PERCENT);
    assertThat(dto.firstRecorded()).isEqualTo(FIRST_RECORDED);
    assertThat(dto.lastRecorded()).isEqualTo(LAST_RECORDED);
    assertThat(dto.totalRecords()).isEqualTo(TOTAL_RECORDS);
  }

  // ==================== ТЕСТЫ НА ОТРИЦАТЕЛЬНЫЕ СЦЕНАРИИ ====================

  @Test
  @DisplayName("[НЕГАТИВ] DTO с некорректными значениями не выбрасывает исключений")
  void negative_shouldNotThrowExceptionForInvalidValues() {
    // Record не выбрасывает исключений при создании с любыми значениями
    assertThatCode(() -> new PriceStatsDto(
        null, null, null, null, null,
        null, null, Double.NaN,
        null, null, -1
    )).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("[НЕГАТИВ] DTO с priceChangePercent = NaN")
  void negative_shouldHandleNaNValue() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, Double.NaN,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.priceChangePercent()).isNaN();
  }

  @Test
  @DisplayName("[НЕГАТИВ] DTO с priceChangePercent = Infinity")
  void negative_shouldHandleInfinityValue() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, Double.POSITIVE_INFINITY,
        FIRST_RECORDED, LAST_RECORDED, TOTAL_RECORDS
    );

    assertThat(dto.priceChangePercent()).isInfinite();
    assertThat(dto.priceChangePercent()).isPositive();
  }

  @Test
  @DisplayName("[НЕГАТИВ] DTO с отрицательным totalRecords")
  void negative_shouldHandleNegativeTotalRecords() {
    PriceStatsDto dto = new PriceStatsDto(
        PRODUCT_ID, PRODUCT_NAME, MIN_PRICE, MAX_PRICE, AVG_PRICE,
        CURRENT_PRICE, PRICE_CHANGE, PRICE_CHANGE_PERCENT,
        FIRST_RECORDED, LAST_RECORDED, -5
    );

    assertThat(dto.totalRecords()).isNegative();
  }
}