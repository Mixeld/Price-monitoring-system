package com.pricetracker.service;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.service.cache.SearchCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для ProductService (100% покрытие)")
class ProductServiceTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private ProductMapper productMapper;

  @Mock
  private SearchCache searchCache;

  @InjectMocks
  private ProductService productService;

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
    validProduct.setDescription("Latest iPhone");
    validProduct.setCategory(electronicsCategory);

    validProductDto = new ProductDto(1L, "iPhone 15", new BigDecimal("999.99"), "Latest iPhone", "Electronics");
  }

  // ==================== ТЕСТЫ ДЛЯ getProductById ====================

  @Test
  @DisplayName("[УСПЕХ] Получение продукта по ID")
  void getProductById_shouldReturnProduct_whenExists() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(validProduct));
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    ProductDto result = productService.getProductById(1L);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.name()).isEqualTo("iPhone 15");
    verify(productRepository).findById(1L);
    verify(productMapper).toDto(validProduct);
  }

  @Test
  @DisplayName("[НЕГАТИВ] Выброс исключения при поиске несуществующего продукта")
  void getProductById_shouldThrowException_whenNotFound() {
    Long nonExistentId = 999L;
    when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.getProductById(nonExistentId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Product not found with id: '999'");
  }

  // ==================== ТЕСТЫ ДЛЯ getProducts ====================

  @Test
  @DisplayName("[УСПЕХ] Получение всех продуктов без фильтрации по категории")
  void getProducts_shouldReturnAllProducts_whenNoCategoryProvided() {
    List<Product> products = List.of(validProduct);
    when(productRepository.findAll()).thenReturn(products);
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    List<ProductDto> result = productService.getProducts(null);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("iPhone 15");
    verify(productRepository).findAll();
    verify(productRepository, never()).findByCategoryName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Получение продуктов с пустой строкой категории (ветка else)")
  void getProducts_shouldReturnAllProducts_whenCategoryIsEmpty() {
    List<Product> products = List.of(validProduct);
    when(productRepository.findAll()).thenReturn(products);
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    List<ProductDto> result = productService.getProducts("");

    assertThat(result).hasSize(1);
    verify(productRepository).findAll();
    verify(productRepository, never()).findByCategoryName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Получение продуктов с фильтрацией по категории")
  void getProducts_shouldReturnProductsByCategory_whenCategoryProvided() {
    List<Product> products = List.of(validProduct);
    when(productRepository.findByCategoryName("Electronics")).thenReturn(products);
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    List<ProductDto> result = productService.getProducts("Electronics");

    assertThat(result).hasSize(1);
    assertThat(result.get(0).category()).isEqualTo("Electronics");
    verify(productRepository).findByCategoryName("Electronics");
    verify(productRepository, never()).findAll();
  }

  @Test
  @DisplayName("[УСПЕХ] Получение пустого списка когда продуктов нет")
  void getProducts_shouldReturnEmptyList_whenNoProducts() {
    when(productRepository.findAll()).thenReturn(List.of());

    List<ProductDto> result = productService.getProducts(null);

    assertThat(result).isEmpty();
  }

  // ==================== ТЕСТЫ ДЛЯ saveProduct ====================

  @Test
  @DisplayName("[УСПЕХ] Создание продукта")
  void saveProduct_shouldCreateSuccessfully() {
    ProductDto createDto = new ProductDto(null, "iPhone 15", new BigDecimal("999.99"), "Latest iPhone", "Electronics");
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
      Product p = invocation.getArgument(0);
      p.setId(1L);
      return p;
    });
    when(productMapper.toEntity(createDto)).thenReturn(new Product());
    when(productMapper.toDto(any(Product.class))).thenReturn(new ProductDto(1L, "iPhone 15", null, null, null));

    ProductDto result = productService.saveProduct(createDto);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(1L);
    verify(productRepository).save(any(Product.class));
    verify(searchCache).invalidateAll();
  }

  // ==================== ТЕСТЫ ДЛЯ updateProduct ====================

  @Test
  @DisplayName("[УСПЕХ] Обновление продукта")
  void updateProduct_shouldUpdateSuccessfully() {
    Long productId = 1L;
    ProductDto updateDto = new ProductDto(productId, "iPhone 15 Pro", new BigDecimal("1299.99"), "Updated description", "Electronics");

    when(productRepository.findById(productId)).thenReturn(Optional.of(validProduct));
    doAnswer(invocation -> {
      Product entity = invocation.getArgument(0);
      ProductDto dto = invocation.getArgument(1);
      entity.setName(dto.name());
      entity.setPrice(dto.price());
      entity.setDescription(dto.description());
      return null;
    }).when(productMapper).updateEntity(any(Product.class), any(ProductDto.class));
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    productService.updateProduct(productId, updateDto);

    ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
    verify(productRepository).save(productCaptor.capture());
    Product capturedProduct = productCaptor.getValue();
    assertThat(capturedProduct.getName()).isEqualTo("iPhone 15 Pro");
    assertThat(capturedProduct.getPrice()).isEqualByComparingTo("1299.99");
    verify(searchCache).invalidateAll();
  }

  @Test
  @DisplayName("[НЕГАТИВ] Обновление несуществующего продукта")
  void updateProduct_shouldThrowException_whenProductNotFound() {
    Long nonExistentId = 999L;
    ProductDto updateDto = new ProductDto(nonExistentId, "Updated", new BigDecimal("100"), null, null);

    when(productRepository.findById(nonExistentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productService.updateProduct(nonExistentId, updateDto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Product not found with id: '999'");

    verify(productRepository, never()).save(any(Product.class));
  }

  // ==================== ТЕСТЫ ДЛЯ deleteProduct ====================

  @Test
  @DisplayName("[УСПЕХ] Удаление продукта")
  void deleteProduct_shouldDeleteSuccessfully() {
    Long productId = 1L;
    when(productRepository.existsById(productId)).thenReturn(true);
    doNothing().when(productRepository).deleteById(productId);

    productService.deleteProduct(productId);

    verify(productRepository, times(1)).deleteById(productId);
    verify(searchCache, times(1)).invalidateAll();
  }

  @Test
  @DisplayName("[НЕГАТИВ] Удаление несуществующего продукта")
  void deleteProduct_shouldThrowException_whenProductNotFound() {
    Long nonExistentId = 999L;
    when(productRepository.existsById(nonExistentId)).thenReturn(false);

    assertThatThrownBy(() -> productService.deleteProduct(nonExistentId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Product not found with id: '999'");

    verify(productRepository, never()).deleteById(anyLong());
    verify(searchCache, never()).invalidateAll();
  }

  // ==================== ТЕСТЫ ДЛЯ searchProducts ====================

  @Test
  @DisplayName("[УСПЕХ] Поиск продуктов с кэшированием - Cache HIT")
  void searchProducts_shouldReturnFromCache_whenCacheHit() {
    String category = "Electronics";
    BigDecimal minPrice = new BigDecimal("100");
    BigDecimal maxPrice = new BigDecimal("1000");
    Pageable pageable = PageRequest.of(0, 10);
    boolean useNative = false;

    Page<ProductDto> cachedPage = new PageImpl<>(List.of(validProductDto));

    when(searchCache.get(any(SearchCache.SearchKey.class))).thenReturn(Optional.of(cachedPage));

    Page<ProductDto> result = productService.searchProducts(category, minPrice, maxPrice, pageable, useNative);

    assertThat(result).isSameAs(cachedPage);
    verify(searchCache, atLeastOnce()).get(any(SearchCache.SearchKey.class));
    verify(productRepository, never()).searchProductsJpql(any(), any(), any(), any());
    verify(productRepository, never()).searchProductsNative(any(), any(), any(), any());
  }

  @Test
  @DisplayName("[УСПЕХ] Поиск продуктов - Cache MISS с JPQL")
  void searchProducts_shouldUseJpqlAndCache_whenCacheMissAndUseNativeFalse() {
    String category = "Electronics";
    BigDecimal minPrice = new BigDecimal("100");
    BigDecimal maxPrice = new BigDecimal("1000");
    Pageable pageable = PageRequest.of(0, 10);
    boolean useNative = false;

    Page<Product> productPage = new PageImpl<>(List.of(validProduct));

    when(searchCache.get(any(SearchCache.SearchKey.class))).thenReturn(Optional.empty());
    when(productRepository.searchProductsJpql(category, minPrice, maxPrice, pageable))
        .thenReturn(productPage);
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    Page<ProductDto> result = productService.searchProducts(category, minPrice, maxPrice, pageable, useNative);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(productRepository).searchProductsJpql(category, minPrice, maxPrice, pageable);
    verify(productRepository, never()).searchProductsNative(any(), any(), any(), any());
    verify(searchCache).put(any(SearchCache.SearchKey.class), any(Page.class));
  }

  @Test
  @DisplayName("[УСПЕХ] Поиск продуктов - Cache MISS с Native query")
  void searchProducts_shouldUseNativeQueryAndCache_whenCacheMissAndUseNativeTrue() {
    String category = "Electronics";
    BigDecimal minPrice = new BigDecimal("100");
    BigDecimal maxPrice = new BigDecimal("1000");
    Pageable pageable = PageRequest.of(0, 10);
    boolean useNative = true;

    Page<Product> productPage = new PageImpl<>(List.of(validProduct));

    when(searchCache.get(any(SearchCache.SearchKey.class))).thenReturn(Optional.empty());
    when(productRepository.searchProductsNative(category, minPrice, maxPrice, pageable))
        .thenReturn(productPage);
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    Page<ProductDto> result = productService.searchProducts(category, minPrice, maxPrice, pageable, useNative);

    assertThat(result).isNotNull();
    verify(productRepository).searchProductsNative(category, minPrice, maxPrice, pageable);
    verify(productRepository, never()).searchProductsJpql(any(), any(), any(), any());
    verify(searchCache).put(any(SearchCache.SearchKey.class), any(Page.class));
  }

  @Test
  @DisplayName("[УСПЕХ] Поиск продуктов с null фильтрами")
  void searchProducts_shouldHandleNullFilters() {
    Pageable pageable = PageRequest.of(0, 10);
    boolean useNative = false;
    Page<Product> productPage = new PageImpl<>(List.of(validProduct));

    when(searchCache.get(any(SearchCache.SearchKey.class))).thenReturn(Optional.empty());
    when(productRepository.searchProductsJpql(null, null, null, pageable))
        .thenReturn(productPage);
    when(productMapper.toDto(validProduct)).thenReturn(validProductDto);

    Page<ProductDto> result = productService.searchProducts(null, null, null, pageable, useNative);

    assertThat(result).isNotNull();
    verify(productRepository).searchProductsJpql(null, null, null, pageable);
  }

  // ==================== ТЕСТЫ ДЛЯ getCacheStats ====================

  @Test
  @DisplayName("[УСПЕХ] Получение статистики кэша")
  void getCacheStats_shouldReturnCacheStatistics() {
    when(searchCache.getSize()).thenReturn(5);
    when(searchCache.getKeys()).thenReturn(Set.of());

    Map<String, Object> stats = productService.getCacheStats();

    assertThat(stats).containsEntry("cacheSize", 5);
    assertThat(stats).containsKey("cacheKeys");
    verify(searchCache).getSize();
    verify(searchCache).getKeys();
  }

  // ==================== ТЕСТЫ ДЛЯ clearCache ====================

  @Test
  @DisplayName("[УСПЕХ] Очистка кэша")
  void clearCache_shouldInvalidateAllCache() {
    productService.clearCache();
    verify(searchCache).invalidateAll();
  }

  // ==================== ТЕСТЫ ДЛЯ createProductsBulk ====================

  @Test
  @DisplayName("[УСПЕХ] Bulk-операция по созданию продуктов")
  void createProductsBulk_shouldCreateMultipleProducts() {
    ProductDto product1Dto = new ProductDto(null, "iPhone 15", new BigDecimal("999.99"), null, "Electronics");
    ProductDto product2Dto = new ProductDto(null, "Samsung Galaxy", new BigDecimal("899.99"), null, "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(product1Dto, product2Dto));

    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));
    when(productRepository.existsByName(anyString())).thenReturn(false);
    when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
      Product p = invocation.getArgument(0);
      p.setId(System.nanoTime());
      return p;
    });
    when(productMapper.toEntity(any(ProductDto.class))).thenAnswer(inv -> {
      ProductDto dto = inv.getArgument(0);
      Product p = new Product();
      p.setName(dto.name());
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenAnswer(inv -> new ProductDto(null, inv.getArgument(0, Product.class).getName(), null, null, null));

    List<ProductDto> results = productService.createProductsBulk(bulkDto);

    assertThat(results).hasSize(2);
    assertThat(results.get(0).name()).isEqualTo("iPhone 15");
    assertThat(results.get(1).name()).isEqualTo("Samsung Galaxy");
    verify(productRepository, times(2)).save(any(Product.class));
    verify(searchCache).invalidateAll();
  }

  @Test
  @DisplayName("[НЕГАТИВ] Bulk создание с дублирующимся именем")
  void createProductsBulk_shouldThrowException_whenDuplicateName() {
    ProductDto product1Dto = new ProductDto(null, "iPhone 15", new BigDecimal("999.99"), null, "Electronics");
    ProductDto product2Dto = new ProductDto(null, "iPhone 15", new BigDecimal("899.99"), null, "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(product1Dto, product2Dto));

    when(productRepository.existsByName("iPhone 15")).thenReturn(true);

    assertThatThrownBy(() -> productService.createProductsBulk(bulkDto))
        .isInstanceOf(DuplicateResourceException.class);

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  @DisplayName("[УСПЕХ] Создание продукта с null именем (покрытие проверки name != null)")
  void createProductsBulk_shouldHandleNullProductName() {
    ProductDto dtoWithNullName = new ProductDto(null, null, new BigDecimal("99.99"), "Description", "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(dtoWithNullName));

    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));
    when(productMapper.toEntity(dtoWithNullName)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(10L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(10L, null, new BigDecimal("99.99"), "Description", "Electronics")
    );

    List<ProductDto> results = productService.createProductsBulk(bulkDto);

    assertThat(results).hasSize(1);
    verify(productRepository, never()).existsByName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Создание продукта с пустой категорией")
  void createProductsBulk_shouldHandleEmptyCategory() {
    ProductDto dto = new ProductDto(null, "Product Empty Cat", new BigDecimal("99.99"), null, "");

    when(productRepository.existsByName("Product Empty Cat")).thenReturn(false);
    when(productMapper.toEntity(dto)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(5L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(5L, "Product Empty Cat", new BigDecimal("99.99"), null, null)
    );

    List<ProductDto> results = productService.createProductsBulk(
        new BulkProductCreateDto(List.of(dto))
    );

    assertThat(results).hasSize(1);
    verify(categoryRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Создание продукта с null категорией (покрытие лямбды без категории)")
  void createProductsBulk_shouldHandleNullCategoryAndCallLambda() {
    ProductDto dtoWithNullCategory = new ProductDto(null, "Product With Null Category", new BigDecimal("49.99"), "Test", null);
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(dtoWithNullCategory));

    when(productRepository.existsByName("Product With Null Category")).thenReturn(false);
    when(productMapper.toEntity(dtoWithNullCategory)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(11L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(11L, "Product With Null Category", new BigDecimal("49.99"), "Test", null)
    );

    List<ProductDto> results = productService.createProductsBulk(bulkDto);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).category()).isNull();
    verify(categoryRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Создание продукта с несуществующей категорией")
  void createProductsBulk_shouldCreateProductWithNullCategory_whenCategoryNotFound() {
    String nonExistentCategory = "NonExistentCategory";
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("99.99"), "Description", nonExistentCategory);

    when(productRepository.existsByName("Product")).thenReturn(false);
    when(categoryRepository.findByName(nonExistentCategory)).thenReturn(Optional.empty());
    when(productMapper.toEntity(dto)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(3L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(3L, "Product", new BigDecimal("99.99"), "Description", null)
    );

    List<ProductDto> results = productService.createProductsBulk(
        new BulkProductCreateDto(List.of(dto))
    );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).category()).isNull();
    verify(categoryRepository).findByName(nonExistentCategory);
  }

  @Test
  @DisplayName("[УСПЕХ] Создание продукта с существующей категорией")
  void createProductsBulk_shouldSetCategory_whenCategoryExists() {
    ProductDto dto = new ProductDto(null, "Product With Cat", new BigDecimal("99.99"), "Description", "Electronics");

    when(productRepository.existsByName("Product With Cat")).thenReturn(false);
    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));
    when(productMapper.toEntity(dto)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(6L);
      p.setCategory(electronicsCategory);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(6L, "Product With Cat", new BigDecimal("99.99"), "Description", "Electronics")
    );

    List<ProductDto> results = productService.createProductsBulk(
        new BulkProductCreateDto(List.of(dto))
    );

    assertThat(results).hasSize(1);
    assertThat(results.get(0).category()).isEqualTo("Electronics");
    verify(categoryRepository).findByName("Electronics");
  }

  // ==================== ТЕСТЫ ДЛЯ createProductsBulkWithoutTransaction ====================

  @Test
  @DisplayName("[УСПЕХ] Массовое создание продуктов без транзакции")
  void createProductsBulkWithoutTransaction_shouldCreateAllProducts() {
    ProductDto product1Dto = new ProductDto(null, "Product 1", new BigDecimal("100"), null, "Electronics");
    ProductDto product2Dto = new ProductDto(null, "Product 2", new BigDecimal("200"), null, "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(product1Dto, product2Dto));

    when(productRepository.existsByName("Product 1")).thenReturn(false);
    when(productRepository.existsByName("Product 2")).thenReturn(false);
    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));
    when(productMapper.toEntity(any(ProductDto.class))).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(System.nanoTime());
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenAnswer(inv ->
        new ProductDto(null, inv.getArgument(0, Product.class).getName(), null, null, null));

    List<ProductDto> results = productService.createProductsBulkWithoutTransaction(bulkDto);

    assertThat(results).hasSize(2);
    verify(productRepository, times(2)).save(any(Product.class));
    verify(searchCache, never()).invalidateAll();
  }

  @Test
  @DisplayName("[НЕГАТИВ] Массовое создание без транзакции - дубликат имени на втором продукте")
  void createProductsBulkWithoutTransaction_shouldThrowException_whenDuplicateNameOnSecondProduct() {
    ProductDto product1Dto = new ProductDto(null, "Product 1", new BigDecimal("100"), null, "Electronics");
    ProductDto product2Dto = new ProductDto(null, "Product 1", new BigDecimal("200"), null, "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(product1Dto, product2Dto));

    when(productRepository.existsByName("Product 1"))
        .thenReturn(false)
        .thenReturn(true);
    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));
    when(productMapper.toEntity(any(ProductDto.class))).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(1L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(validProductDto);

    assertThatThrownBy(() -> productService.createProductsBulkWithoutTransaction(bulkDto))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Product with name 'Product 1' already exists");

    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  @DisplayName("[НЕГАТИВ] Массовое создание без транзакции - дубликат имени на первом продукте")
  void createProductsBulkWithoutTransaction_shouldThrowException_whenDuplicateNameOnFirstProduct() {
    ProductDto product1Dto = new ProductDto(null, "Duplicate Product", new BigDecimal("100"), null, "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(product1Dto));

    when(productRepository.existsByName("Duplicate Product")).thenReturn(true);

    assertThatThrownBy(() -> productService.createProductsBulkWithoutTransaction(bulkDto))
        .isInstanceOf(DuplicateResourceException.class);

    verify(productRepository, never()).save(any(Product.class));
  }

  @Test
  @DisplayName("[УСПЕХ] Массовое создание без транзакции с null именем")
  void createProductsBulkWithoutTransaction_shouldHandleNullProductName() {
    ProductDto dtoWithNullName = new ProductDto(null, null, new BigDecimal("99.99"), "Description", "Electronics");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(dtoWithNullName));

    when(categoryRepository.findByName("Electronics")).thenReturn(Optional.of(electronicsCategory));
    when(productMapper.toEntity(dtoWithNullName)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(12L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(12L, null, new BigDecimal("99.99"), "Description", "Electronics")
    );

    List<ProductDto> results = productService.createProductsBulkWithoutTransaction(bulkDto);

    assertThat(results).hasSize(1);
    verify(productRepository, never()).existsByName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Массовое создание без транзакции с пустой категорией")
  void createProductsBulkWithoutTransaction_shouldHandleEmptyCategory() {
    ProductDto dto = new ProductDto(null, "Product No Category", new BigDecimal("99.99"), null, "");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(dto));

    when(productRepository.existsByName("Product No Category")).thenReturn(false);
    when(productMapper.toEntity(dto)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(7L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(7L, "Product No Category", new BigDecimal("99.99"), null, null)
    );

    List<ProductDto> results = productService.createProductsBulkWithoutTransaction(bulkDto);

    assertThat(results).hasSize(1);
    verify(categoryRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Массовое создание без транзакции с null категорией")
  void createProductsBulkWithoutTransaction_shouldHandleNullCategory() {
    ProductDto dto = new ProductDto(null, "Product Null Category", new BigDecimal("99.99"), null, null);
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(dto));

    when(productRepository.existsByName("Product Null Category")).thenReturn(false);
    when(productMapper.toEntity(dto)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(13L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(13L, "Product Null Category", new BigDecimal("99.99"), null, null)
    );

    List<ProductDto> results = productService.createProductsBulkWithoutTransaction(bulkDto);

    assertThat(results).hasSize(1);
    verify(categoryRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("[УСПЕХ] Массовое создание без транзакции с несуществующей категорией")
  void createProductsBulkWithoutTransaction_shouldHandleNonExistentCategory() {
    ProductDto dto = new ProductDto(null, "Product", new BigDecimal("99.99"), null, "NonExistent");
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(List.of(dto));

    when(productRepository.existsByName("Product")).thenReturn(false);
    when(categoryRepository.findByName("NonExistent")).thenReturn(Optional.empty());
    when(productMapper.toEntity(dto)).thenReturn(new Product());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
      Product p = inv.getArgument(0);
      p.setId(8L);
      return p;
    });
    when(productMapper.toDto(any(Product.class))).thenReturn(
        new ProductDto(8L, "Product", new BigDecimal("99.99"), null, null)
    );

    List<ProductDto> results = productService.createProductsBulkWithoutTransaction(bulkDto);

    assertThat(results).hasSize(1);
    assertThat(results.get(0).category()).isNull();
    verify(categoryRepository).findByName("NonExistent");
  }
}