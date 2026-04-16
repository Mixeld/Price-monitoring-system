package com.pricetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для Category entity")
class CategoryTest {

  private final Long ID = 1L;
  private final String NAME = "Electronics";
  private final Product product1 = new Product();
  private final Product product2 = new Product();


  // ==================== ТЕСТЫ КОНСТРУКТОРОВ И БАЗОВЫХ СВОЙСТВ ====================

  @Nested
  @DisplayName("Тесты конструкторов и базовых свойств")
  class BasicTests {

    @Test
    @DisplayName("Конструктор по умолчанию создает пустой объект с инициализированным списком")
    void defaultConstructor_shouldCreateEmptyCategory() {
      Category emptyCategory = new Category();

      assertThat(emptyCategory.getId()).isNull();
      assertThat(emptyCategory.getName()).isNull();
      assertThat(emptyCategory.getProducts()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("Конструктор со всеми аргументами корректно устанавливает поля")
    void allArgsConstructor_shouldCreateCategoryWithAllFields() {
      List<Product> products = List.of(product1, product2);
      Category allArgsCategory = new Category(ID, NAME, products);

      assertThat(allArgsCategory.getId()).isEqualTo(ID);
      assertThat(allArgsCategory.getName()).isEqualTo(NAME);
      assertThat(allArgsCategory.getProducts()).isEqualTo(products);
    }

    @Test
    @DisplayName("Сеттеры и геттеры работают корректно")
    void settersAndGetters_shouldWork() {
      Category category = new Category();
      category.setId(ID);
      category.setName(NAME);
      category.setProducts(new ArrayList<>());

      assertThat(category.getId()).isEqualTo(ID);
      assertThat(category.getName()).isEqualTo(NAME);
      assertThat(category.getProducts()).isEmpty();
    }
  }

  // ======================================================================
  // ===== БЛОК ДЛЯ ДОСТИЖЕНИЯ 100% ПОКРЫТИЯ КОДА, СГЕНЕРИРОВАННОГО LOMBOK =====
  // ======================================================================

  @Nested
  @DisplayName("Тесты для 100% покрытия Lombok (equals/hashCode/canEqual)")
  class FullLombokCoverageTests {

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
      // Создаем два идентичных объекта
      category1 = new Category(ID, NAME, new ArrayList<>(List.of(product1)));
      category2 = new Category(ID, NAME, new ArrayList<>(List.of(product1)));
    }

    // --- 1. Базовые тесты equals ---

    @Test
    @DisplayName("[EQUALS] true для идентичных объектов")
    void equals_isTrue_forIdenticalObjects() {
      assertThat(category1).isEqualTo(category2);
    }

    @Test
    @DisplayName("[EQUALS] false для null и других типов")
    void equals_isFalse_forNullAndOtherTypes() {
      assertThat(category1.equals(null)).isFalse();
      assertThat(category1.equals("a string")).isFalse();
    }

    @Test
    @DisplayName("[EQUALS] false при разных значениях полей")
    void equals_isFalse_forDifferentFieldValues() {
      category2.setId(2L);
      assertThat(category1).isNotEqualTo(category2);
    }

    // --- 2. Тесты на асимметричный null (одно поле null, другое нет) ---

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у одного id=null")
    void equals_isFalse_whenOneIdIsNull() {
      category1.setId(null);
      assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у другого id=null")
    void equals_isFalse_whenOtherIdIsNull() {
      category2.setId(null);
      assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у одного name=null")
    void equals_isFalse_whenOneNameIsNull() {
      category1.setName(null);
      assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у другого name=null")
    void equals_isFalse_whenOtherNameIsNull() {
      category2.setName(null);
      assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у одного products=null")
    void equals_isFalse_whenOneProductsIsNull() {
      category1.setProducts(null);
      assertThat(category1).isNotEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false, если у другого products=null")
    void equals_isFalse_whenOtherProductsIsNull() {
      category2.setProducts(null);
      assertThat(category1).isNotEqualTo(category2);
    }

    // --- 3. Тесты на симметричный null (оба поля null) ---

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если у обоих id = null")
    void equals_isTrue_whenBothIdsAreNull() {
      category1.setId(null);
      category2.setId(null);
      assertThat(category1).isEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если у обоих name = null")
    void equals_isTrue_whenBothNamesAreNull() {
      category1.setName(null);
      category2.setName(null);
      assertThat(category1).isEqualTo(category2);
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: true, если у обоих products = null")
    void equals_isTrue_whenBothProductsAreNull() {
      category1.setProducts(null);
      category2.setProducts(null);
      assertThat(category1).isEqualTo(category2);
    }

    // --- 4. Тест на наследование для `canEqual` ---

    @lombok.EqualsAndHashCode(callSuper = true)
    class SubCategory extends Category { }

    @Test
    @DisplayName("[ПОКРЫТИЕ] equals: false при сравнении с дочерним классом")
    void equals_isFalse_forSubclassInstance() {
      SubCategory subCategory = new SubCategory();
      assertThat(category1.equals(subCategory)).isFalse();
    }

    // --- 5. Тесты для `hashCode` ---

    @Test
    @DisplayName("[HASHCODE] контракт: равные объекты имеют равные хэш-коды")
    void hashCode_isEqual_forEqualObjects() {
      assertThat(category1.hashCode()).isEqualTo(category2.hashCode());
    }

    @Test
    @DisplayName("[ПОКРЫТИЕ] hashCode: работает с полностью null объектом")
    void hashCode_worksWithAllNullFields() {
      Category allNullCategory = new Category(null, null, null);
      assertThat(allNullCategory.hashCode()).isNotZero(); // Просто вызываем
    }

    // --- 6. Тесты для `toString` ---

    @Test
    @DisplayName("[TOSTRING] корректно работает для полного и пустого объекта")
    void toString_worksForFullAndEmptyObject() {
      assertThat(category1.toString()).contains("name=Electronics");

      Category emptyCategory = new Category(null, null, null);
      assertThat(emptyCategory.toString()).contains("name=null");
    }
  }
}