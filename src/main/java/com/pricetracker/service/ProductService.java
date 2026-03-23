package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.service.cache.ProductSearchKey;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  private final Map<ProductSearchKey, Page<ProductDto>> searchCache = new ConcurrentHashMap<>();


  @Transactional(readOnly = true)
  public ProductDto getProductById(final Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "Product not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<ProductDto> getProducts(final String categoryName) {
    if (categoryName != null && !categoryName.isBlank()) {
      return productRepository.findByCategoryName(categoryName)
          .stream()
          .map(productMapper::toDto)
          .toList();
    }
    return productRepository.findAll().stream()
        .map(productMapper::toDto)
        .toList();
  }

  @Transactional
  public ProductDto saveProduct(final ProductDto dto) {
    Product product = productMapper.toEntity(dto);

    if (dto.categoryName() != null) {
      Category category = categoryRepository.findByName(dto.categoryName())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.categoryName());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    invalidateCache();
    return productMapper.toDto(savedProduct);
  }



  @Transactional
  public ProductDto updateProduct(final Long id, final ProductDto dto) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

    product.setName(dto.name());
    product.setCurrentPrice(dto.currentPrice());
    product.setDescription(dto.description());

    if (dto.categoryName() != null) {
      Category category = categoryRepository.findByName(dto.categoryName())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.categoryName());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    }

    Product updatedProduct = productRepository.save(product);
    invalidateCache();
    return productMapper.toDto(updatedProduct);
  }


  @Transactional
  public void deleteProduct(final Long id) {
    if (!productRepository.existsById(id)) {
      throw new EntityNotFoundException("Cannot delete. Product not found: " + id);
    }
    productRepository.deleteById(id);
    invalidateCache();
  }

  @Transactional
  public void demoTransaction(final boolean triggerError) {
    Category cat = new Category();
    cat.setName("ROLLBACK_TEST_CATEGORY");
    categoryRepository.save(cat);

    if (triggerError) {
      throw new RuntimeException("ИСКУССТВЕННАЯ ОШИБКА ДЛЯ ТЕСТА ТРАНЗАКЦИИ");
    }
  }

  @Transactional (readOnly = true)
  public Page<ProductDto> searchProducts(
      final String categoryName,
      final BigDecimal minPrice,
      final BigDecimal maxPrice,
      final Pageable pageable,
      final boolean useNative){

    ProductSearchKey key = new ProductSearchKey(
        categoryName, minPrice, maxPrice,
        pageable.getPageNumber(), pageable.getPageSize(), useNative
    );

    if (searchCache.containsKey(key)) {
      log.info ("Возврат КЭШ-данных: {}", key);
      return searchCache.get(key);
    }

    log.info ("Запрос к БД (Native: {})...", useNative);
    Page<Product> pageResult;

    if(useNative) {
      pageResult = productRepository.searchProductsNative(
          categoryName, minPrice, maxPrice, pageable);
    } else {
      pageResult = productRepository.searchProductsJpql(
          categoryName, minPrice, maxPrice, pageable);
      }


    Page<ProductDto> dtoPage = pageResult.map(productMapper::toDto);

    searchCache.put(key, dtoPage);
    return dtoPage;
  }

  private void invalidateCache() {
    log.info("Данные изменились. Кэш очищен.");
    searchCache.clear();
  }

}