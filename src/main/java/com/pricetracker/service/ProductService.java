package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для бизнес-логики работы с товарами.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  /**
   * Получить товар по ID.
   */
  public ProductDto getProductById(final Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new RuntimeException("Product not found: " + id));
  }

  /**
   * Получить все товары или отфильтровать по категории.
   */
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

  /**
   * Сохранить новый товар (с проверкой категории).
   */
  @Transactional
  public ProductDto saveProduct(final ProductDto dto) {
    Product product = productMapper.toEntity(dto);

    if (dto.category() != null) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.category());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    return productMapper.toDto(savedProduct);
  }

  /**
   * Обновить существующий товар.
   */
  @Transactional
  public ProductDto updateProduct(final Long id, final ProductDto dto) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found: " + id));

    // Обновляем простые поля
    product.setName(dto.name());
    product.setCurrentPrice(dto.price());

    // Обновляем категорию (ищем или создаем)
    if (dto.category() != null) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.category());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    }

    Product updatedProduct = productRepository.save(product);
    return productMapper.toDto(updatedProduct);
  }

  /**
   * Удалить товар по ID.
   */
  public void deleteProduct(final Long id) {
    if (!productRepository.existsById(id)) {
      throw new RuntimeException("Product not found: " + id);
    }
    productRepository.deleteById(id);
  }
}