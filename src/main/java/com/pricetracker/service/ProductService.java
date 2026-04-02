package com.pricetracker.service;

import com.pricetracker.service.cache.SearchCache;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.Category;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.repository.CategoryRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;
  private final SearchCache searchCache;

  @Transactional(readOnly = true)
  public ProductDto getProductById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    return productMapper.toDto(product);
  }

  @Transactional(readOnly = true)
  public List<ProductDto> getProducts(String category) {
    List<Product> products;
    if (category != null && !category.isEmpty()) {
      products = productRepository.findByCategoryName(category);
    } else {
      products = productRepository.findAll();
    }
    return products.stream()
        .map(productMapper::toDto)
        .toList();
  }

  @Transactional
  public ProductDto saveProduct(ProductDto productDto) {
    Product product = productMapper.toEntity(productDto);
    Product saved = productRepository.save(product);

    searchCache.invalidateAll();
    log.info("Cache invalidated after product creation");

    return productMapper.toDto(saved);
  }

  @Transactional
  public ProductDto updateProduct(Long id, ProductDto productDto) {
    Product existing = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    productMapper.updateEntity(existing, productDto);
    Product updated = productRepository.save(existing);

    searchCache.invalidateAll();
    log.info("Cache invalidated after product update");

    return productMapper.toDto(updated);
  }

  @Transactional
  public void deleteProduct(Long id) {
    productRepository.deleteById(id);

    searchCache.invalidateAll();
    log.info("Cache invalidated after product deletion");
  }

  @Transactional(readOnly = true)
  public Page<ProductDto> searchProducts(
      String category,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Pageable pageable,
      boolean useNative
  ) {

    SearchCache.SearchKey cacheKey = SearchCache.SearchKey.builder()
        .category(category)
        .minPrice(minPrice)
        .maxPrice(maxPrice)
        .page(pageable.getPageNumber())
        .size(pageable.getPageSize())
        .sort(pageable.getSort().toString())
        .useNative(useNative)
        .build();

    Optional<Page<ProductDto>> cached = searchCache.get(cacheKey);
    if (cached.isPresent()) {
      log.info("Cache HIT for key: {}", cacheKey);
      return cached.get();
    }

    log.info("Cache MISS for key: {}", cacheKey);

    Page<Product> productPage;

    if (useNative) {
      log.info("Executing NATIVE query with filters: category={}, minPrice={}, maxPrice={}, sort={}",
          category, minPrice, maxPrice, pageable.getSort());
      productPage = productRepository.searchProductsNative(
          category, minPrice, maxPrice, pageable
      );
    } else {
      log.info("Executing JPQL query with filters: category={}, minPrice={}, maxPrice={}, sort={}",
          category, minPrice, maxPrice, pageable.getSort());
      productPage = productRepository.searchProductsJpql(
          category, minPrice, maxPrice, pageable
      );
    }

    Page<ProductDto> dtoPage = productPage.map(productMapper::toDto);

    searchCache.put(cacheKey, dtoPage);
    log.info("Result cached with key: {}", cacheKey);
    log.info("Cache size after save: {}", searchCache.getSize());

    return dtoPage;
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getCacheStats() {
    return Map.of(
        "cacheSize", searchCache.getSize(),
        "cacheKeys", searchCache.getKeys()
    );
  }


  @Transactional
  public void clearCache() {
    searchCache.invalidateAll();
    log.info("Cache manually cleared");
  }
}