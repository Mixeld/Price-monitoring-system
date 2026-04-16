package com.pricetracker.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Unit-тесты для ProductDto")
class ProductDtoTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  // ==================== ТЕСТЫ КОНСТРУКТОРА И RECORD МЕТОДОВ ====================

  @Test
  @DisplayName("[УСПЕХ] Создание DTO через канонический конструктор")
  void constructor_shouldCreateValidDto() {
    ProductDto dto = new ProductDto(1L, "Test Product", new BigDecimal("99.99"), "Description", "Category");

    assertThat(dto.id()).isEqualTo(1L);
    assertThat(dto.name()).isEqualTo("Test Product");
    assertThat(dto.price()).isEqualByComparingTo("99.99");
    assertThat(dto.description()).isEqualTo("Description");
    assertThat(dto.category()).isEqualTo("Category");
  }

  @Test
  @DisplayName("[УСПЕХ] Поддержка null значений для необязательных полей")
  void constructor_shouldAllowNullForOptionalFields() {
    ProductDto dto = new ProductDto(null, null, null, null, null);

    assertThat(dto.id()).isNull();
    assertThat(dto.name()).isNull();
    assertThat(dto.price()).isNull();
    assertThat(dto.description()).isNull();
    assertThat(dto.category()).isNull();
  }

  // ==================== ТЕСТЫ BUILDER ====================

  @Test
  @DisplayName("[УСПЕХ] Создание DTO через Builder со всеми полями")
  void builder_shouldCreateDtoWithAllFields() {
    ProductDto dto = ProductDto.builder()
        .id(1L)
        .name("Builder Product")
        .price(new BigDecimal("199.99"))
        .description("Builder description")
        .category("Builder Category")
        .build();

    assertThat(dto.id()).isEqualTo(1L);
    assertThat(dto.name()).isEqualTo("Builder Product");
    assertThat(dto.price()).isEqualByComparingTo("199.99");
    assertThat(dto.description()).isEqualTo("Builder description");
    assertThat(dto.category()).isEqualTo("Builder Category");
  }

  @Test
  @DisplayName("[УСПЕХ] Создание DTO через Builder с null значениями")
  void builder_shouldCreateDtoWithNullValues() {
    ProductDto dto = ProductDto.builder().build();

    assertThat(dto.id()).isNull();
    assertThat(dto.name()).isNull();
    assertThat(dto.price()).isNull();
    assertThat(dto.description()).isNull();
    assertThat(dto.category()).isNull();
  }

  @Test
  @DisplayName("[УСПЕХ] Builder с методом price(String) - конвертация строки в BigDecimal")
  void builder_shouldConvertStringPriceToBigDecimal() {
    ProductDto dto = ProductDto.builder()
        .name("Product")
        .price("299.99")
        .build();

    assertThat(dto.price()).isEqualByComparingTo("299.99");
  }

  @Test
  @DisplayName("[УСПЕХ] Builder с price(String) на null значении")
  void builder_shouldHandleNullStringPrice() {
    ProductDto dto = ProductDto.builder()
        .name("Product")
        .price((String) null)
        .build();

    assertThat(dto.price()).isNull();
  }

  @Test
  @DisplayName("[УСПЕХ] Builder с цепочкой вызовов (fluent interface)")
  void builder_shouldSupportFluentInterface() {
    ProductDto dto = ProductDto.builder()
        .id(5L)
        .name("Fluent Product")
        .price(new BigDecimal("49.99"))
        .description("Fluent description")
        .category("Fluent Cat")
        .build();

    assertThat(dto.name()).isEqualTo("Fluent Product");
  }

  @Test
  @DisplayName("[УСПЕХ] Builder с price(String) и некорректным форматом")
  void builder_shouldThrowExceptionWhenInvalidStringPrice() {
    assertThatThrownBy(() -> ProductDto.builder().price("invalid_number").build())
        .isInstanceOf(NumberFormatException.class);
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - @NotBlank ДЛЯ name ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - корректное имя продукта")
  void validation_shouldPass_whenNameIsValid() {
    ProductDto dto = new ProductDto(null, "Valid Product Name", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - имя равно null (только @NotBlank)")
  void validation_shouldFail_whenNameIsNull() {
    ProductDto dto = new ProductDto(null, null, new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - имя пустая строка (две ошибки: @NotBlank и @Size)")
  void validation_shouldFail_whenNameIsEmpty() {
    ProductDto dto = new ProductDto(null, "", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(2);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Product name is required",
        "Product name must be between 3 and 200 characters"
    );
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - имя состоит только из пробелов (только @NotBlank, т.к. длина 3 пробела проходит @Size)")
  void validation_shouldFail_whenNameIsBlank() {
    ProductDto dto = new ProductDto(null, "   ", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - имя из 2 пробелов (две ошибки: @NotBlank и @Size)")
  void validation_shouldFail_whenNameIsTwoSpaces() {
    ProductDto dto = new ProductDto(null, "  ", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(2);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Product name is required",
        "Product name must be between 3 and 200 characters"
    );
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - @Size ДЛЯ name ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - имя минимальной длины (3 символа)")
  void validation_shouldPass_whenNameHasMinLength() {
    ProductDto dto = new ProductDto(null, "ABC", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - имя короче 3 символов")
  void validation_shouldFail_whenNameIsTooShort() {
    ProductDto dto = new ProductDto(null, "AB", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name must be between 3 and 200 characters");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - имя максимальной длины (200 символов)")
  void validation_shouldPass_whenNameHasMaxLength() {
    String longName = "A".repeat(200);
    ProductDto dto = new ProductDto(null, longName, new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - имя длиннее 200 символов")
  void validation_shouldFail_whenNameIsTooLong() {
    String tooLongName = "A".repeat(201);
    ProductDto dto = new ProductDto(null, tooLongName, new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name must be between 3 and 200 characters");
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - @NotNull И @DecimalMin ДЛЯ price ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - корректная цена (положительное число)")
  void validation_shouldPass_whenPriceIsValid() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("0.01"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - цена с большим количеством знаков после запятой")
  void validation_shouldPass_whenPriceHasManyDecimalPlaces() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("99.9999"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - цена равна null")
  void validation_shouldFail_whenPriceIsNull() {
    ProductDto dto = new ProductDto(null, "Product", null, null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Product price is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - цена равна 0")
  void validation_shouldFail_whenPriceIsZero() {
    ProductDto dto = new ProductDto(null, "Product", BigDecimal.ZERO, null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - цена отрицательная")
  void validation_shouldFail_whenPriceIsNegative() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("-10.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Price must be greater than 0");
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - @Size ДЛЯ description ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - описание максимальной длины (1000 символов)")
  void validation_shouldPass_whenDescriptionHasMaxLength() {
    String longDescription = "D".repeat(1000);
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), longDescription, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - описание длиннее 1000 символов")
  void validation_shouldFail_whenDescriptionIsTooLong() {
    String tooLongDescription = "D".repeat(1001);
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), tooLongDescription, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Description cannot exceed 1000 characters");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - описание равно null")
  void validation_shouldPass_whenDescriptionIsNull() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - описание пустая строка")
  void validation_shouldPass_whenDescriptionIsEmpty() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), "", null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - category (без аннотаций) ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - категория может быть null")
  void validation_shouldPass_whenCategoryIsNull() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - категория может быть пустой строкой")
  void validation_shouldPass_whenCategoryIsEmpty() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), null, "");
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - категория может быть произвольной строкой")
  void validation_shouldPass_whenCategoryIsAnyString() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), null, "Any @#$% Category");
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  // ==================== КОМБИНИРОВАННЫЕ ТЕСТЫ ВАЛИДАЦИИ ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Множественные ошибки валидации")
  void validation_shouldCollectMultipleViolations() {
    ProductDto dto = new ProductDto(null, "AB", BigDecimal.ZERO, "D".repeat(1001), null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(3);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Product name must be between 3 and 200 characters",
        "Price must be greater than 0",
        "Description cannot exceed 1000 characters"
    );
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - полностью корректный DTO")
  void validation_shouldPass_forCompleteValidDto() {
    ProductDto dto = new ProductDto(
        1L,
        "Complete Product",
        new BigDecimal("999.99"),
        "This is a complete product description that is within limits.",
        "Electronics"
    );
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  // ==================== ТЕСТЫ МЕТОДОВ RECORD ====================

  @Test
  @DisplayName("[УСПЕХ] Record методы equals и hashCode")
  void record_shouldImplementEqualsAndHashCodeCorrectly() {
    ProductDto dto1 = new ProductDto(1L, "Product", new BigDecimal("100.00"), "Desc", "Cat");
    ProductDto dto2 = new ProductDto(1L, "Product", new BigDecimal("100.00"), "Desc", "Cat");
    ProductDto dto3 = new ProductDto(2L, "Different", new BigDecimal("200.00"), "Diff", "Diff");

    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    assertThat(dto1).isNotEqualTo(dto3);
  }

  @Test
  @DisplayName("[УСПЕХ] Record метод toString")
  void record_shouldImplementToString() {
    ProductDto dto = new ProductDto(1L, "Test Product", new BigDecimal("99.99"), "Test Desc", "Test Cat");
    String toString = dto.toString();

    assertThat(toString).contains("Test Product");
    assertThat(toString).contains("99.99");
    assertThat(toString).contains("ProductDto");
  }

  // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

  @Test
  @DisplayName("[ГРАНИЦЫ] Имя из 3 символов - минимальная граница")
  void boundary_nameWithMinLength_shouldPass() {
    ProductDto dto = new ProductDto(null, "Min", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Имя из 2 символов - ниже минимальной границы")
  void boundary_nameBelowMinLength_shouldFail() {
    ProductDto dto = new ProductDto(null, "Mi", new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Product name must be between 3 and 200 characters");
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Имя из 200 символов - максимальная граница")
  void boundary_nameWithMaxLength_shouldPass() {
    String name = "A".repeat(200);
    ProductDto dto = new ProductDto(null, name, new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Имя из 201 символа - выше максимальной границы")
  void boundary_nameAboveMaxLength_shouldFail() {
    String name = "A".repeat(201);
    ProductDto dto = new ProductDto(null, name, new BigDecimal("100.00"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Цена с минимальным положительным значением")
  void boundary_priceWithMinimumPositiveValue_shouldPass() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("0.0001"), null, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Описание из 1000 символов - максимальная граница")
  void boundary_descriptionWithMaxLength_shouldPass() {
    String description = "D".repeat(1000);
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), description, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Описание из 1001 символа - выше максимальной границы")
  void boundary_descriptionAboveMaxLength_shouldFail() {
    String description = "D".repeat(1001);
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("100.00"), description, null);
    Set<ConstraintViolation<ProductDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
  }
}