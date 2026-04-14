package com.pricetracker.service;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException; // Важно для отлова ошибок уникальности

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest // Поднимает полный Spring-контекст и БД
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
    // Arrange: создаем DTO со списком, где есть дубликат имени, который вызовет ошибку
    List<ProductDto> dtos = List.of(
        new ProductDto(null, "Уникальный Продукт 1", null, null, null),
        new ProductDto(null, "Уникальный Продукт 1", null, null, null) // Ошибка!
    );
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(dtos);

    // Act & Assert: ожидаем ошибку (скорее всего, от БД из-за unique constraint)
    assertThatThrownBy(() -> productService.createProductsBulk(bulkDto))
        .isInstanceOf(DataIntegrityViolationException.class); // Или твоя кастомная ошибка, если ты проверяешь дубликаты заранее

    // Assert: Проверяем, что в БД не осталось НИЧЕГО. Транзакция откатила даже первый "успешный" продукт.
    assertThat(productRepository.count()).isZero();
  }

  @Test
  @DisplayName("БЕЗ @Transactional: при ошибке первая часть bulk-операции должна сохраниться")
  void createProductsBulk_withoutTransaction_shouldPartiallySaveOnFailure() {
    // Arrange: тот же ошибочный DTO
    List<ProductDto> dtos = List.of(
        new ProductDto(null, "Уникальный Продукт 2", null, null, null),
        new ProductDto(null, "Уникальный Продукт 2", null, null, null) // Ошибка!
    );
    BulkProductCreateDto bulkDto = new BulkProductCreateDto(dtos);

    // Act & Assert
    assertThatThrownBy(() -> productService.createProductsBulkWithoutTransaction(bulkDto))
        .isInstanceOf(DataIntegrityViolationException.class);

    // Assert: Проверяем, что первый продукт УСПЕЛ сохраниться до ошибки, а второй - нет.
    assertThat(productRepository.count()).isEqualTo(1);
    assertThat(productRepository.findAll().get(0).getName()).isEqualTo("Уникальный Продукт 2");
  }
}