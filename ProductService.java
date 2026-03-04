package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Сервис, реализующий бизнес-логику работы с товарами.
 * Класс final для предотвращения наследования (DesignForExtension).
 */
@Service
@RequiredArgsConstructor
public final class ProductService {

  /**
   * Репозиторий доступа к БД.
   */
  private final ProductRepository productRepository;

  /**
   * Маппер объектов.
   */
  private final ProductMapper productMapper;

  /**
   * Получить товар по ID.
   *
   * @param id идентификатор товара
   * @return DTO товара
   * @throws RuntimeException если товар не найден
   */
  public ProductDto getProductById(final Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new RuntimeException(
            "Product not found: " + id));
  }

  /**
   * Получить список товаров с фильтрацией.
   *
   * @param category категория для фильтрации (может быть null)
   * @return список DTO
   */
  public List<ProductDto> getProducts(final String category) {
    List<Product> products;
    if (category != null && !category.isBlank()) {
      products = productRepository.findByCategory(category);
    } else {
      products = productRepository.findAll();
    }

    // ИСПРАВЛЕНО: .toList() вместо Collectors.toList()
    // Это удовлетворяет SonarQube (java:S6204) в Java 17+
    return products.stream()
        .map(productMapper::toDto)
        .toList();
  }

  /**
   * Сохранить новый товар.
   *
   * @param dto данные сохраняемого товара
   * @return сохраненный товар в виде DTO
   */
  public ProductDto saveProduct(final ProductDto dto) {
    Product product = productMapper.toEntity(dto);
    Product saved = productRepository.save(product);
    return productMapper.toDto(saved);
  }
}