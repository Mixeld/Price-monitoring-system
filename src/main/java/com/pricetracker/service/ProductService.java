package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public final class ProductService {


  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;


  public ProductDto getProductById(final Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "Product not found with id: " + id));
  }

  public List<ProductDto> getProducts(final String categoryName) {
    List<Product> products;

    if (categoryName != null && !categoryName.isBlank()) {
      // Используем метод с @EntityGraph для загрузки категорий одним запросом
      products = productRepository.findByCategoryName(categoryName);
      log.info("Загружено {} продуктов с категориями (используя EntityGraph)", products.size());
    } else {
      // Используем переопределенный findAll с @EntityGraph
      products = productRepository.findAll();
      log.info("Загружено {} всех продуктов с категориями (используя EntityGraph)", products.size());
    }

    return products.stream()
        .map(productMapper::toDto)
        .toList();
  }

  @Transactional
  public ProductDto saveProduct(final ProductDto dto) {
    Product product = productMapper.toEntity(dto);

    if (dto.category() != null) {
      Category category = findOrCreateCategory(dto.category());
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    return productMapper.toDto(savedProduct);
  }

  @Transactional
  public ProductDto updateProduct(final Long id, final ProductDto dto) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Product not found with id: " + id));

    product.setName(dto.name());
    product.setCurrentPrice(dto.price());

    if (dto.category() != null) {
      Category category = findOrCreateCategory(dto.category());
      product.setCategory(category);
    } else {
      product.setCategory(null);
    }

    return productMapper.toDto(product); // save не нужен, т.к. внутри транзакции
  }

  @Transactional
  public void deleteProduct(final Long id) {
    if (!productRepository.existsById(id)) {
      throw new EntityNotFoundException(
          "Cannot delete. Product not found with id: " + id);
    }
    productRepository.deleteById(id);
  }

  private Category findOrCreateCategory(String categoryName) {
    return categoryRepository.findByName(categoryName)
        .orElseGet(() -> {
          Category newCat = new Category();
          newCat.setName(categoryName);
          return categoryRepository.save(newCat);
        });
  }
}
