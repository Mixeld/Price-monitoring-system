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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
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
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

    productMapper.updateEntity(existing, productDto);
    Product updated = productRepository.save(existing);

    searchCache.invalidateAll();
    log.info("Cache invalidated after product update");

    return productMapper.toDto(updated);
  }

  @Transactional
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product", "id", id);
    }
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

  @Transactional
  public List<ProductDto> createProductsBulk(BulkProductCreateDto bulkDto) {
    log.info("Starting bulk product creation for {} products", bulkDto.products().size());

    List<ProductDto> createdProducts = bulkDto.products().stream()
        .map(this::processProductWithCategory)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());

    log.info("Bulk creation completed. Created: {} products", createdProducts.size());

    searchCache.invalidateAll();

    return createdProducts;
  }

  private Optional<ProductDto> processProductWithCategory(ProductDto dto) {
    if (dto.name() != null && productRepository.existsByName(dto.name())) {
      throw new DuplicateResourceException("Product", "name", dto.name());
    }

    Optional<String> categoryName = Optional.ofNullable(dto.category());

    categoryName.ifPresentOrElse(
        name -> log.debug("Processing product '{}' with category '{}'", dto.name(), name),
        () -> log.debug("Processing product '{}' without category", dto.name())
    );

    Product product = productMapper.toEntity(dto);

    Optional.ofNullable(dto.category())
        .filter(name -> !name.isBlank())
        .flatMap(categoryRepository::findByName)
        .ifPresent(product::setCategory);

    Product saved = productRepository.save(product);
    log.debug("Product '{}' saved with id: {}", saved.getName(), saved.getId());

    return Optional.of(productMapper.toDto(saved));
  }

  public List<ProductDto> createProductsBulkWithoutTransaction(BulkProductCreateDto bulkDto) {
    log.warn("!!! EXECUTING BULK OPERATION WITHOUT TRANSACTION !!!");

    List<ProductDto> createdProducts = new ArrayList<>();

    for (ProductDto dto : bulkDto.products()) {
      if (dto.name() != null && productRepository.existsByName(dto.name())) {
        throw new DuplicateResourceException("Product", "name", dto.name());
      }

      Product product = productMapper.toEntity(dto);

      Optional.ofNullable(dto.category())
          .filter(name -> !name.isBlank())
          .flatMap(categoryRepository::findByName)
          .ifPresent(product::setCategory);

      Product saved = productRepository.save(product);
      createdProducts.add(productMapper.toDto(saved));
    }

    return createdProducts;
  }
}