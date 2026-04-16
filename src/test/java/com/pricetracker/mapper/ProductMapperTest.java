package com.pricetracker.mapper;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для ProductMapper")
class ProductMapperTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private ProductMapper productMapper;

  private Product validProduct;
  private Category electronicsCategory;
  private ProductDto validProductDto;

  @BeforeEach
  void setUp() {
    electronicsCategory = new Category();
    electronicsCategory.setId(1L);
    electronicsCategory.setName("Electronics");

    validProduct = new Product();
    validProduct.setId(1L);
    validProduct.setName("iPhone 15");
    validProduct.setPrice(new BigDecimal("999.99"));
    validProduct.setDescription("Latest iPhone with A17 chip");
    validProduct.setCategory(electronicsCategory);

    validProductDto = new ProductDto(
        1L,
        "iPhone 15",
        new BigDecimal("999.99"),
        "Latest iPhone with A17 chip",
        "Electronics"
    );
  }

  // ==================== ТЕСТЫ ДЛЯ toDto ====================

  @Nested
  @DisplayName("Тесты метода toDto(Product)")
  class ToDtoTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация Product в ProductDto с существующей категорией")
    void toDto_shouldConvertProductToDto_whenCategoryExists() {
      ProductDto result = productMapper.toDto(validProduct);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.name()).isEqualTo("iPhone 15");
      assertThat(result.price()).isEqualByComparingTo("999.99");
      assertThat(result.description()).isEqualTo("Latest iPhone with A17 chip");
      assertThat(result.category()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Product в ProductDto без категории")
    void toDto_shouldConvertProductToDto_whenCategoryIsNull() {
      validProduct.setCategory(null);

      ProductDto result = productMapper.toDto(validProduct);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.name()).isEqualTo("iPhone 15");
      assertThat(result.category()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация Product в ProductDto с категорией без имени")
    void toDto_shouldConvertProductToDto_whenCategoryHasNullName() {
      Category emptyCategory = new Category();
      emptyCategory.setId(2L);
      emptyCategory.setName(null);
      validProduct.setCategory(emptyCategory);

      ProductDto result = productMapper.toDto(validProduct);

      assertThat(result).isNotNull();
      assertThat(result.category()).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null Product в null DTO")
    void toDto_shouldReturnNull_whenProductIsNull() {
      ProductDto result = productMapper.toDto(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация Product с null полями")
    void toDto_shouldHandleNullFields() {
      Product productWithNulls = new Product();
      productWithNulls.setId(null);
      productWithNulls.setName(null);
      productWithNulls.setPrice(null);
      productWithNulls.setDescription(null);
      productWithNulls.setCategory(null);

      ProductDto result = productMapper.toDto(productWithNulls);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.name()).isNull();
      assertThat(result.price()).isNull();
      assertThat(result.description()).isNull();
      assertThat(result.category()).isNull();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ toEntity ====================

  @Nested
  @DisplayName("Тесты метода toEntity(ProductDto)")
  class ToEntityTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация ProductDto в Product с существующей категорией")
    void toEntity_shouldConvertDtoToProduct_whenCategoryExists() {
      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      Product result = productMapper.toEntity(validProductDto);

      assertThat(result).isNotNull();
      // ID устанавливается из DTO согласно реализации маппера
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getName()).isEqualTo("iPhone 15");
      assertThat(result.getPrice()).isEqualByComparingTo("999.99");
      assertThat(result.getDescription()).isEqualTo("Latest iPhone with A17 chip");
      assertThat(result.getCategory()).isEqualTo(electronicsCategory);

      verify(categoryRepository).findByName("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация ProductDto в Product без категории")
    void toEntity_shouldConvertDtoToProduct_whenCategoryIsNull() {
      ProductDto dtoWithoutCategory = new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Description", null);

      Product result = productMapper.toEntity(dtoWithoutCategory);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getCategory()).isNull();
      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация ProductDto в Product с пустой строкой категории")
    void toEntity_shouldConvertDtoToProduct_whenCategoryIsEmpty() {
      ProductDto dtoWithEmptyCategory = new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Description", "");

      Product result = productMapper.toEntity(dtoWithEmptyCategory);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getCategory()).isNull();
      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация ProductDto в Product с категорией из пробелов")
    void toEntity_shouldConvertDtoToProduct_whenCategoryIsBlank() {
      ProductDto dtoWithBlankCategory = new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Description", "   ");

      Product result = productMapper.toEntity(dtoWithBlankCategory);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getCategory()).isNull();
      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[ОШИБКА] Выброс исключения при несуществующей категории")
    void toEntity_shouldThrowException_whenCategoryNotFound() {
      when(categoryRepository.findByName("NonExistentCategory")).thenReturn(Optional.empty());

      ProductDto dtoWithInvalidCategory = new ProductDto(1L, "Product", new BigDecimal("100"), "Desc", "NonExistentCategory");

      assertThatThrownBy(() -> productMapper.toEntity(dtoWithInvalidCategory))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Category not found: NonExistentCategory");

      verify(categoryRepository).findByName("NonExistentCategory");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null DTO в null Entity")
    void toEntity_shouldReturnNull_whenDtoIsNull() {
      Product result = productMapper.toEntity(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация DTO с null полями")
    void toEntity_shouldHandleNullFields() {
      ProductDto dtoWithNulls = new ProductDto(null, null, null, null, null);

      Product result = productMapper.toEntity(dtoWithNulls);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getName()).isNull();
      assertThat(result.getPrice()).isNull();
      assertThat(result.getDescription()).isNull();
      assertThat(result.getCategory()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация DTO с id (ID копируется в Entity согласно реализации)")
    void toEntity_shouldCopyIdFromDtoToEntity() {
      ProductDto dtoWithId = new ProductDto(99L, "Product", new BigDecimal("100"), "Desc", null);

      Product result = productMapper.toEntity(dtoWithId);

      // ID копируется из DTO в Entity согласно реализации маппера
      assertThat(result.getId()).isEqualTo(99L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ updateEntity ====================

  @Nested
  @DisplayName("Тесты метода updateEntity(Product, ProductDto)")
  class UpdateEntityTests {

    private Product existingProduct;

    @BeforeEach
    void setUp() {
      existingProduct = new Product();
      existingProduct.setId(1L);
      existingProduct.setName("Old Name");
      existingProduct.setPrice(new BigDecimal("99.99"));
      existingProduct.setDescription("Old description");
      existingProduct.setCategory(electronicsCategory);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление всех полей продукта")
    void updateEntity_shouldUpdateAllFields_whenAllFieldsAreProvided() {
      ProductDto updateDto = new ProductDto(1L, "New Name", new BigDecimal("199.99"), "New description", "Electronics");

      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("New Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("199.99");
      assertThat(existingProduct.getDescription()).isEqualTo("New description");
      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);

      verify(categoryRepository).findByName("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление только имени")
    void updateEntity_shouldUpdateOnlyName_whenOnlyNameProvided() {
      ProductDto updateDto = new ProductDto(1L, "New Name", null, null, null);

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("New Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("99.99");
      assertThat(existingProduct.getDescription()).isEqualTo("Old description");
      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);

      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление только цены")
    void updateEntity_shouldUpdateOnlyPrice_whenOnlyPriceProvided() {
      ProductDto updateDto = new ProductDto(1L, null, new BigDecimal("299.99"), null, null);

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("299.99");
      assertThat(existingProduct.getDescription()).isEqualTo("Old description");
      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление только описания")
    void updateEntity_shouldUpdateOnlyDescription_whenOnlyDescriptionProvided() {
      ProductDto updateDto = new ProductDto(1L, null, null, "Brand new description", null);

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("99.99");
      assertThat(existingProduct.getDescription()).isEqualTo("Brand new description");
      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление только категории")
    void updateEntity_shouldUpdateOnlyCategory_whenOnlyCategoryProvided() {
      Category newCategory = new Category();
      newCategory.setId(2L);
      newCategory.setName("Books");

      ProductDto updateDto = new ProductDto(1L, null, null, null, "Books");

      when(categoryRepository.findByName("Books")).thenReturn(Optional.of(newCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("99.99");
      assertThat(existingProduct.getDescription()).isEqualTo("Old description");
      assertThat(existingProduct.getCategory()).isEqualTo(newCategory);

      verify(categoryRepository).findByName("Books");
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления имени при null")
    void updateEntity_shouldSkipName_whenNameIsNull() {
      ProductDto updateDto = new ProductDto(1L, null, new BigDecimal("199.99"), "New desc", "Electronics");

      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("199.99");
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления имени при пустой строке")
    void updateEntity_shouldSkipName_whenNameIsEmpty() {
      ProductDto updateDto = new ProductDto(1L, "", new BigDecimal("199.99"), "New desc", "Electronics");

      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("199.99");
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления имени при строке из пробелов")
    void updateEntity_shouldSkipName_whenNameIsBlank() {
      ProductDto updateDto = new ProductDto(1L, "   ", new BigDecimal("199.99"), "New desc", "Electronics");

      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("199.99");
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления цены при null")
    void updateEntity_shouldSkipPrice_whenPriceIsNull() {
      ProductDto updateDto = new ProductDto(1L, "New Name", null, "New desc", "Electronics");

      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getPrice()).isEqualByComparingTo("99.99");
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления описания при null")
    void updateEntity_shouldSkipDescription_whenDescriptionIsNull() {
      ProductDto updateDto = new ProductDto(1L, "New Name", new BigDecimal("199.99"), null, "Electronics");

      when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getDescription()).isEqualTo("Old description");
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления категории при null")
    void updateEntity_shouldSkipCategory_whenCategoryIsNull() {
      ProductDto updateDto = new ProductDto(1L, "New Name", new BigDecimal("199.99"), "New desc", null);

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);
      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления категории при пустой строке")
    void updateEntity_shouldSkipCategory_whenCategoryIsEmpty() {
      ProductDto updateDto = new ProductDto(1L, "New Name", new BigDecimal("199.99"), "New desc", "");

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);
      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Пропуск обновления категории при строке из пробелов")
    void updateEntity_shouldSkipCategory_whenCategoryIsBlank() {
      ProductDto updateDto = new ProductDto(1L, "New Name", new BigDecimal("199.99"), "New desc", "   ");

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);
      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[ОШИБКА] Выброс исключения при обновлении на несуществующую категорию")
    void updateEntity_shouldThrowException_whenNewCategoryNotFound() {
      ProductDto updateDto = new ProductDto(1L, null, null, null, "NonExistentCategory");

      when(categoryRepository.findByName("NonExistentCategory")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> productMapper.updateEntity(existingProduct, updateDto))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Category not found: NonExistentCategory");

      verify(categoryRepository).findByName("NonExistentCategory");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Обновление при null существующем продукте (метод ничего не делает)")
    void updateEntity_shouldDoNothing_whenExistingProductIsNull() {
      ProductDto updateDto = new ProductDto(1L, "New Name", new BigDecimal("199.99"), "New desc", "Electronics");

      productMapper.updateEntity(null, updateDto);

      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Обновление при null DTO (метод ничего не делает)")
    void updateEntity_shouldDoNothing_whenDtoIsNull() {
      productMapper.updateEntity(existingProduct, null);

      assertThat(existingProduct.getName()).isEqualTo("Old Name");
      assertThat(existingProduct.getPrice()).isEqualByComparingTo("99.99");
      assertThat(existingProduct.getDescription()).isEqualTo("Old description");
      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);

      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Обновление при null существующем продукте и null DTO")
    void updateEntity_shouldDoNothing_whenBothAreNull() {
      productMapper.updateEntity(null, null);

      verify(categoryRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление с сохранением существующей категории (если новая не указана)")
    void updateEntity_shouldKeepExistingCategory_whenNoNewCategoryProvided() {
      ProductDto updateDto = new ProductDto(1L, "New Name", null, null, null);

      productMapper.updateEntity(existingProduct, updateDto);

      assertThat(existingProduct.getCategory()).isEqualTo(electronicsCategory);
      verify(categoryRepository, never()).findByName(anyString());
    }
  }
}