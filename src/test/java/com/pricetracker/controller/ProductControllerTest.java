package com.pricetracker.controller;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для ProductController")
class ProductControllerTest {

  @Mock
  private ProductService productService;

  @InjectMocks
  private ProductController productController;

  private ProductDto productDto;
  private List<ProductDto> productDtoList;
  private BulkProductCreateDto bulkProductCreateDto;

  @BeforeEach
  void setUp() {
    productDto = new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Latest iPhone", "Electronics");
    productDtoList = List.of(productDto);
    bulkProductCreateDto = new BulkProductCreateDto(List.of(productDto));
  }

  // ==================== ТЕСТЫ ДЛЯ getProductById ====================

  @Nested
  @DisplayName("Тесты метода getProductById(Long id)")
  class GetProductByIdTests {

    @Test
    @DisplayName("[УСПЕХ] Получение продукта по ID")
    void getProductById_shouldReturnProduct_whenExists() {
      when(productService.getProductById(1L)).thenReturn(productDto);

      ResponseEntity<ProductDto> response = productController.getProductById(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(productDto);
      verify(productService).getProductById(1L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getProducts ====================

  @Nested
  @DisplayName("Тесты метода getProducts(String category)")
  class GetProductsTests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех продуктов без фильтрации")
    void getProducts_shouldReturnAllProducts_whenNoCategory() {
      when(productService.getProducts(null)).thenReturn(productDtoList);

      ResponseEntity<List<ProductDto>> response = productController.getProducts(null);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      assertThat(response.getBody().get(0)).isEqualTo(productDto);
      verify(productService).getProducts(null);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение продуктов с фильтрацией по категории")
    void getProducts_shouldReturnProductsByCategory_whenCategoryProvided() {
      when(productService.getProducts("Electronics")).thenReturn(productDtoList);

      ResponseEntity<List<ProductDto>> response = productController.getProducts("Electronics");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      verify(productService).getProducts("Electronics");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка когда продуктов нет")
    void getProducts_shouldReturnEmptyList_whenNoProducts() {
      when(productService.getProducts("Electronics")).thenReturn(List.of());

      ResponseEntity<List<ProductDto>> response = productController.getProducts("Electronics");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ createProduct ====================

  @Nested
  @DisplayName("Тесты метода createProduct(ProductDto productDto)")
  class CreateProductTests {

    @Test
    @DisplayName("[УСПЕХ] Создание продукта")
    void createProduct_shouldCreateAndReturnCreatedProduct() {
      ProductDto createdProduct = new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Latest iPhone", "Electronics");
      when(productService.saveProduct(any(ProductDto.class))).thenReturn(createdProduct);

      ResponseEntity<ProductDto> response = productController.createProduct(productDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(createdProduct);
      assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/products/1"));
      verify(productService).saveProduct(productDto);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ createProductsBulk ====================

  @Nested
  @DisplayName("Тесты метода createProductsBulk(BulkProductCreateDto bulkDto, boolean useTransaction)")
  class CreateProductsBulkTests {

    @Test
    @DisplayName("[УСПЕХ] Массовое создание продуктов С транзакцией")
    void createProductsBulk_shouldCreateWithTransaction_whenUseTransactionTrue() {
      when(productService.createProductsBulk(bulkProductCreateDto)).thenReturn(productDtoList);

      ResponseEntity<List<ProductDto>> response = productController.createProductsBulk(bulkProductCreateDto, true);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      verify(productService).createProductsBulk(bulkProductCreateDto);
      verify(productService, never()).createProductsBulkWithoutTransaction(any());
    }

    @Test
    @DisplayName("[УСПЕХ] Массовое создание продуктов БЕЗ транзакции")
    void createProductsBulk_shouldCreateWithoutTransaction_whenUseTransactionFalse() {
      when(productService.createProductsBulkWithoutTransaction(bulkProductCreateDto)).thenReturn(productDtoList);

      ResponseEntity<List<ProductDto>> response = productController.createProductsBulk(bulkProductCreateDto, false);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      verify(productService).createProductsBulkWithoutTransaction(bulkProductCreateDto);
      verify(productService, never()).createProductsBulk(any());
    }

    @Test
    @DisplayName("[УСПЕХ] Массовое создание с пустым списком продуктов")
    void createProductsBulk_shouldHandleEmptyList() {
      BulkProductCreateDto emptyBulkDto = new BulkProductCreateDto(List.of());
      when(productService.createProductsBulk(emptyBulkDto)).thenReturn(List.of());

      ResponseEntity<List<ProductDto>> response = productController.createProductsBulk(emptyBulkDto, true);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ updateProduct ====================

  @Nested
  @DisplayName("Тесты метода updateProduct(Long id, ProductDto productDto)")
  class UpdateProductTests {

    @Test
    @DisplayName("[УСПЕХ] Обновление продукта")
    void updateProduct_shouldUpdateAndReturnUpdatedProduct() {
      ProductDto updatedProduct = new ProductDto(1L, "iPhone 15 Pro", new BigDecimal("1299.99"), "Updated description", "Electronics");
      when(productService.updateProduct(eq(1L), any(ProductDto.class))).thenReturn(updatedProduct);

      ResponseEntity<ProductDto> response = productController.updateProduct(1L, productDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(updatedProduct);
      verify(productService).updateProduct(1L, productDto);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ deleteProduct ====================

  @Nested
  @DisplayName("Тесты метода deleteProduct(Long id)")
  class DeleteProductTests {

    @Test
    @DisplayName("[УСПЕХ] Удаление продукта")
    void deleteProduct_shouldDeleteAndReturnNoContent() {
      doNothing().when(productService).deleteProduct(1L);

      ResponseEntity<Void> response = productController.deleteProduct(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(productService).deleteProduct(1L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ searchProducts ====================

  @Nested
  @DisplayName("Тесты метода searchProducts")
  class SearchProductsTests {

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с параметрами по умолчанию")
    void searchProducts_shouldSearchWithDefaultParameters() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      Page<ProductDto> result = productController.searchProducts(null, null, null, 0, 10, "id,asc", false);

      assertThat(result).isEqualTo(expectedPage);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      Pageable capturedPageable = pageableCaptor.getValue();
      assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
      assertThat(capturedPageable.getPageSize()).isEqualTo(10);
      assertThat(capturedPageable.getSort()).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с фильтрами")
    void searchProducts_shouldSearchWithFilters() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      Page<ProductDto> result = productController.searchProducts(
          "Electronics", new BigDecimal("100"), new BigDecimal("1000"),
          2, 20, "price,desc", true
      );

      assertThat(result).isEqualTo(expectedPage);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          eq("Electronics"), eq(new BigDecimal("100")), eq(new BigDecimal("1000")),
          pageableCaptor.capture(),
          eq(true)
      );

      Pageable capturedPageable = pageableCaptor.getValue();
      assertThat(capturedPageable.getPageNumber()).isEqualTo(2);
      assertThat(capturedPageable.getPageSize()).isEqualTo(20);
      assertThat(capturedPageable.getSort()).isEqualTo(Sort.by(Sort.Order.desc("price")));
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с сортировкой по одному полю по возрастанию")
    void searchProducts_shouldHandleSingleFieldAscSort() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, "name,asc", false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by("name").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с сортировкой по одному полю без указания направления")
    void searchProducts_shouldHandleSingleFieldWithoutDirection() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, "name", false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by("name").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с множественной сортировкой")
    void searchProducts_shouldHandleMultipleSortFields() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, "price,desc,name,asc", false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name")
      ));
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с нечетным количеством параметров сортировки")
    void searchProducts_shouldHandleOddNumberOfSortParams() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, "price,desc,name", false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name")
      ));
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с пустой строкой сортировки")
    void searchProducts_shouldUseDefaultSort_whenSortIsEmpty() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, "", false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с null строкой сортировки")
    void searchProducts_shouldUseDefaultSort_whenSortIsNull() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, null, false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Поиск продуктов с пробельной строкой сортировки")
    void searchProducts_shouldUseDefaultSort_whenSortIsBlank() {
      Page<ProductDto> expectedPage = new PageImpl<>(productDtoList);
      when(productService.searchProducts(any(), any(), any(), any(Pageable.class), anyBoolean()))
          .thenReturn(expectedPage);

      productController.searchProducts(null, null, null, 0, 10, "   ", false);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(productService).searchProducts(
          isNull(), isNull(), isNull(),
          pageableCaptor.capture(),
          eq(false)
      );

      assertThat(pageableCaptor.getValue().getSort()).isEqualTo(Sort.by("id").ascending());
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getCacheStats ====================

  @Nested
  @DisplayName("Тесты метода getCacheStats()")
  class GetCacheStatsTests {

    @Test
    @DisplayName("[УСПЕХ] Получение статистики кэша")
    void getCacheStats_shouldReturnCacheStatistics() {
      Map<String, Object> stats = Map.of("cacheSize", 5, "cacheKeys", List.of());
      when(productService.getCacheStats()).thenReturn(stats);

      ResponseEntity<Map<String, Object>> response = productController.getCacheStats();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(stats);
      verify(productService).getCacheStats();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ clearCache ====================

  @Nested
  @DisplayName("Тесты метода clearCache()")
  class ClearCacheTests {

    @Test
    @DisplayName("[УСПЕХ] Очистка кэша")
    void clearCache_shouldClearCacheAndReturnSuccessMessage() {
      doNothing().when(productService).clearCache();

      ResponseEntity<String> response = productController.clearCache();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo("Cache cleared successfully");
      verify(productService).clearCache();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ parseSortParameter (приватный метод) ====================

  @Nested
  @DisplayName("Тесты приватного метода parseSortParameter(String sort)")
  class ParseSortParameterTests {

    @Test
    @DisplayName("[УСПЕХ] Парсинг null - возвращает сортировку по id asc")
    void parseSortParameter_shouldReturnDefault_whenSortIsNull() {
      Sort result = invokeParseSortParameter(null);
      assertThat(result).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг пустой строки - возвращает сортировку по id asc")
    void parseSortParameter_shouldReturnDefault_whenSortIsEmpty() {
      Sort result = invokeParseSortParameter("");
      assertThat(result).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг строки из пробелов - возвращает сортировку по id asc")
    void parseSortParameter_shouldReturnDefault_whenSortIsBlank() {
      Sort result = invokeParseSortParameter("   ");
      assertThat(result).isEqualTo(Sort.by("id").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг одного поля без направления - asc по умолчанию")
    void parseSortParameter_shouldParseSingleFieldWithoutDirection() {
      Sort result = invokeParseSortParameter("name");
      assertThat(result).isEqualTo(Sort.by("name").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг одного поля с asc")
    void parseSortParameter_shouldParseSingleFieldWithAsc() {
      Sort result = invokeParseSortParameter("name,asc");
      assertThat(result).isEqualTo(Sort.by("name").ascending());
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг одного поля с desc")
    void parseSortParameter_shouldParseSingleFieldWithDesc() {
      Sort result = invokeParseSortParameter("name,desc");
      assertThat(result).isEqualTo(Sort.by(Sort.Order.desc("name")));
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг двух полей с направлениями")
    void parseSortParameter_shouldParseTwoFieldsWithDirections() {
      Sort result = invokeParseSortParameter("price,desc,name,asc");
      assertThat(result).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name")
      ));
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг двух полей без направления для второго")
    void parseSortParameter_shouldParseTwoFieldsWithMissingDirectionForSecond() {
      Sort result = invokeParseSortParameter("price,desc,name");
      assertThat(result).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name")
      ));
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг с uppercase направлениями")
    void parseSortParameter_shouldHandleUppercaseDirections() {
      Sort result = invokeParseSortParameter("price,DESC,name,ASC");
      assertThat(result).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name")
      ));
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг с mixed case направлениями")
    void parseSortParameter_shouldHandleMixedCaseDirections() {
      Sort result = invokeParseSortParameter("price,Desc,name,Asc");
      assertThat(result).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name")
      ));
    }

    @Test
    @DisplayName("[УСПЕХ] Парсинг трех полей")
    void parseSortParameter_shouldParseThreeFields() {
      Sort result = invokeParseSortParameter("price,desc,name,asc,id,desc");
      assertThat(result).isEqualTo(Sort.by(
          Sort.Order.desc("price"),
          Sort.Order.asc("name"),
          Sort.Order.desc("id")
      ));
    }
  }

  // Вспомогательный метод для вызова приватного метода parseSortParameter через рефлексию
  private Sort invokeParseSortParameter(String sort) {
    try {
      java.lang.reflect.Method method = ProductController.class.getDeclaredMethod("parseSortParameter", String.class);
      method.setAccessible(true);
      return (Sort) method.invoke(productController, sort);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}