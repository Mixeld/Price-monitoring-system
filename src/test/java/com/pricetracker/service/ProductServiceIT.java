package com.pricetracker.service;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ProductServiceIT {

  @Autowired
  private ProductService productService;

  @Autowired
  private ProductRepository productRepository;

  @BeforeEach
  void cleanupDatabase() {
    productRepository.deleteAll();
  }

  @Test
  @DisplayName("С @Transactional: при ошибке вся bulk-операция должна откатиться")
  void createProductsBulk_withTransaction_shouldRollbackOnFailure() {
    // Arrange
    // --- ИСПРАВЛЕНИЕ: Добавляем цену ---
    Product duplicateProduct = new Product();
    duplicateProduct.setName("Дубликат");
    duplicateProduct.setPrice(new BigDecimal("99.99")); // <-- ВОТ ОНО
    productRepository.save(duplicateProduct);
    // ---------------------------------

    List<ProductDto> dtos = List.of(
        new ProductDto(null, "Уникальный Продукт 1", new BigDecimal("100"), null, null),
        new ProductDto(null, "Дубликат", new BigDecimal("200"), null, null) // Ошибка!
    );
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(dtos);

    // Act & Assert
    assertThatThrownBy(() -> productService.createProductsBulk(bulkDto))
        .isInstanceOf(DuplicateResourceException.class);

    // Assert
    assertThat(productRepository.count()).isEqualTo(1);
    assertThat(productRepository.existsByName("Уникальный Продукт 1")).isFalse();
  }

  @Test
  @DisplayName("БЕЗ @Transactional: при ошибке первая часть должна сохраниться")
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void createProductsBulk_withoutTransaction_shouldPartiallySaveOnFailure() {
    // Arrange
    // --- ИСПРАВЛЕНИЕ: Добавляем цену ---
    Product duplicateProduct = new Product();
    duplicateProduct.setName("Дубликат 2");
    duplicateProduct.setPrice(new BigDecimal("99.99")); // <-- И ЗДЕСЬ
    productRepository.save(duplicateProduct);
    // ---------------------------------

    List<ProductDto> dtos = List.of(
        new ProductDto(null, "Уникальный Продукт 2", new BigDecimal("100"), null, null),
        new ProductDto(null, "Дубликат 2", new BigDecimal("200"), null, null) // Ошибка!
    );
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(dtos);

    // Act & Assert
    assertThatThrownBy(() -> productService.createProductsBulkWithoutTransaction(bulkDto))
        .isInstanceOf(DuplicateResourceException.class);

    // Assert
    assertThat(productRepository.count()).isEqualTo(2);
    assertThat(productRepository.existsByName("Уникальный Продукт 2")).isTrue();
  }
}