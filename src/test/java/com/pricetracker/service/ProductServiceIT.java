package com.pricetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.repository.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException; // Важно для отлова ошибок уникальности

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

    List<ProductDto> dtos = List.of(
        new ProductDto(null, "Уникальный Продукт 1", null, null, null),
        new ProductDto(null, "Уникальный Продукт 1", null, null, null)
    );
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(dtos);

    assertThatThrownBy(() -> productService.createProductsBulk(bulkDto))
        .isInstanceOf(DataIntegrityViolationException.class);

    assertThat(productRepository.count()).isZero();
  }

  @Test
  @DisplayName("БЕЗ @Transactional: при ошибке первая часть bulk-операции должна сохраниться")
  void createProductsBulk_withoutTransaction_shouldPartiallySaveOnFailure() {
    // Arrange: тот же ошибочный DTO
    List<ProductDto> dtos = List.of(
        new ProductDto(null, "Уникальный Продукт 2", null, null, null),
        new ProductDto(null, "Уникальный Продукт 2", null, null, null)
    );
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(dtos);

    assertThatThrownBy(() -> productService.createProductsBulkWithoutTransaction(bulkDto))
        .isInstanceOf(DataIntegrityViolationException.class);

    assertThat(productRepository.count()).isEqualTo(1);
    assertThat(productRepository.findAll().get(0).getName()).isEqualTo("Уникальный Продукт 2");
  }
}