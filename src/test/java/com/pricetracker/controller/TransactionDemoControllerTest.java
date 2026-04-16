package com.pricetracker.controller;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для TransactionDemoController")
class TransactionDemoControllerTest {

  @Mock
  private ProductService productService;

  @InjectMocks
  private TransactionDemoController transactionDemoController;

  // ==================== ТЕСТЫ ДЛЯ testWithTransaction ====================

  @Nested
  @DisplayName("Тесты метода testWithTransaction()")
  class TestWithTransactionTests {

    @Test
    @DisplayName("[УСПЕХ] Успешное выполнение с транзакцией")
    void testWithTransaction_shouldReturnSuccess_whenNoException() {
      // Arrange
      List<ProductDto> expectedResults = List.of(
          new ProductDto(1L, "Product 1", new BigDecimal("100.00"), "First product", "Electronics"),
          new ProductDto(2L, "Product 2", new BigDecimal("200.00"), "Second product", "Electronics"),
          new ProductDto(4L, "Product 4", new BigDecimal("400.00"), "Fourth product", "Electronics")
      );
      when(productService.createProductsBulk(any(BulkProductCreateDto.class))).thenReturn(expectedResults);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(
          "SUCCESS WITH TRANSACTION: Created 3 products. All saved or none!"
      );

      ArgumentCaptor<BulkProductCreateDto> captor = ArgumentCaptor.forClass(BulkProductCreateDto.class);
      verify(productService).createProductsBulk(captor.capture());

      BulkProductCreateDto capturedDto = captor.getValue();
      assertThat(capturedDto.products()).hasSize(4);
      assertThat(capturedDto.products().get(0).name()).isEqualTo("Product 1");
      assertThat(capturedDto.products().get(1).name()).isEqualTo("Product 2");
      assertThat(capturedDto.products().get(2).name()).isEqualTo("P3");
      assertThat(capturedDto.products().get(3).name()).isEqualTo("Product 4");
    }

    @Test
    @DisplayName("[ОШИБКА] Ошибка при выполнении с транзакцией - откат")
    void testWithTransaction_shouldReturnError_whenExceptionThrown() {
      // Arrange
      RuntimeException exception = new RuntimeException("Duplicate product name");
      when(productService.createProductsBulk(any(BulkProductCreateDto.class))).thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(
          "TRANSACTION ROLLBACK: Error occurred, no products were saved. Error: Duplicate product name"
      );
      verify(productService).createProductsBulk(any(BulkProductCreateDto.class));
    }

    @Test
    @DisplayName("[ОШИБКА] Ошибка валидации при выполнении с транзакцией")
    void testWithTransaction_shouldReturnError_whenValidationExceptionThrown() {
      // Arrange
      IllegalArgumentException exception = new IllegalArgumentException("Product name must be at least 3 characters");
      when(productService.createProductsBulk(any(BulkProductCreateDto.class))).thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).contains("TRANSACTION ROLLBACK:");
      assertThat(response.getBody()).contains("Product name must be at least 3 characters");
      verify(productService).createProductsBulk(any(BulkProductCreateDto.class));
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Ошибка с null сообщением при транзакции")
    void testWithTransaction_shouldHandleNullExceptionMessage() {
      // Arrange
      RuntimeException exception = new RuntimeException((String) null);
      when(productService.createProductsBulk(any(BulkProductCreateDto.class))).thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(
          "TRANSACTION ROLLBACK: Error occurred, no products were saved. Error: null"
      );
      verify(productService).createProductsBulk(any(BulkProductCreateDto.class));
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Ошибка с пустым сообщением при транзакции")
    void testWithTransaction_shouldHandleEmptyExceptionMessage() {
      // Arrange
      RuntimeException exception = new RuntimeException("");
      when(productService.createProductsBulk(any(BulkProductCreateDto.class))).thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(
          "TRANSACTION ROLLBACK: Error occurred, no products were saved. Error: "
      );
      verify(productService).createProductsBulk(any(BulkProductCreateDto.class));
    }
  }

  // ==================== ТЕСТЫ ДЛЯ testWithoutTransaction ====================

  @Nested
  @DisplayName("Тесты метода testWithoutTransaction()")
  class TestWithoutTransactionTests {

    @Test
    @DisplayName("[УСПЕХ] Успешное выполнение без транзакции")
    void testWithoutTransaction_shouldReturnSuccess_whenNoException() {
      // Arrange
      List<ProductDto> expectedResults = List.of(
          new ProductDto(1L, "Product 1", new BigDecimal("100.00"), "First product", "Electronics"),
          new ProductDto(2L, "Product 2", new BigDecimal("200.00"), "Second product", "Electronics"),
          new ProductDto(4L, "Product 4", new BigDecimal("400.00"), "Fourth product", "Electronics")
      );
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class)))
          .thenReturn(expectedResults);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithoutTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(
          "WITHOUT TRANSACTION: Created 3 products BUT some may be partially saved!"
      );

      ArgumentCaptor<BulkProductCreateDto> captor = ArgumentCaptor.forClass(BulkProductCreateDto.class);
      verify(productService).createProductsBulkWithoutTransaction(captor.capture());

      BulkProductCreateDto capturedDto = captor.getValue();
      assertThat(capturedDto.products()).hasSize(4);
    }

    @Test
    @DisplayName("[ОШИБКА] Ошибка при выполнении без транзакции - частичное сохранение")
    void testWithoutTransaction_shouldReturnError_whenExceptionThrown() {
      // Arrange
      RuntimeException exception = new RuntimeException("Duplicate product name");
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class)))
          .thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithoutTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(
          "PARTIAL SAVE: Some products may have been saved before the error! Error: Duplicate product name"
      );
      verify(productService).createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class));
    }

    @Test
    @DisplayName("[ОШИБКА] Ошибка валидации при выполнении без транзакции")
    void testWithoutTransaction_shouldReturnError_whenValidationExceptionThrown() {
      // Arrange
      IllegalArgumentException exception = new IllegalArgumentException("Product name must be at least 3 characters");
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class)))
          .thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithoutTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).contains("PARTIAL SAVE:");
      assertThat(response.getBody()).contains("Product name must be at least 3 characters");
      verify(productService).createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class));
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Ошибка с null сообщением без транзакции")
    void testWithoutTransaction_shouldHandleNullExceptionMessage() {
      // Arrange
      RuntimeException exception = new RuntimeException((String) null);
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class)))
          .thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithoutTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(
          "PARTIAL SAVE: Some products may have been saved before the error! Error: null"
      );
      verify(productService).createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class));
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Ошибка с пустым сообщением без транзакции")
    void testWithoutTransaction_shouldHandleEmptyExceptionMessage() {
      // Arrange
      RuntimeException exception = new RuntimeException("");
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class)))
          .thenThrow(exception);

      // Act
      ResponseEntity<String> response = transactionDemoController.testWithoutTransaction();

      // Assert
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
      assertThat(response.getBody()).isEqualTo(
          "PARTIAL SAVE: Some products may have been saved before the error! Error: "
      );
      verify(productService).createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class));
    }
  }

  // ==================== ТЕСТЫ ДЛЯ createTestBulkData (приватный метод) ====================

  @Nested
  @DisplayName("Тесты приватного метода createTestBulkData()")
  class CreateTestBulkDataTests {

    @Test
    @DisplayName("[УСПЕХ] Создание тестовых данных для bulk операции")
    void createTestBulkData_shouldReturnCorrectBulkData() throws Exception {
      // Используем рефлексию для вызова приватного метода
      java.lang.reflect.Method method = TransactionDemoController.class.getDeclaredMethod("createTestBulkData");
      method.setAccessible(true);

      BulkProductCreateDto result = (BulkProductCreateDto) method.invoke(transactionDemoController);

      assertThat(result).isNotNull();
      assertThat(result.products()).hasSize(4);

      // Проверяем первый продукт
      ProductDto product1 = result.products().get(0);
      assertThat(product1.id()).isNull();
      assertThat(product1.name()).isEqualTo("Product 1");
      assertThat(product1.price()).isEqualByComparingTo("100.00");
      assertThat(product1.description()).isEqualTo("First product");
      assertThat(product1.category()).isEqualTo("Electronics");

      // Проверяем второй продукт
      ProductDto product2 = result.products().get(1);
      assertThat(product2.name()).isEqualTo("Product 2");
      assertThat(product2.price()).isEqualByComparingTo("200.00");
      assertThat(product2.description()).isEqualTo("Second product");

      // Проверяем третий продукт (с коротким именем - вызовет ошибку)
      ProductDto product3 = result.products().get(2);
      assertThat(product3.name()).isEqualTo("P3");
      assertThat(product3.price()).isEqualByComparingTo("300.00");
      assertThat(product3.description()).isEqualTo("Invalid name");

      // Проверяем четвертый продукт
      ProductDto product4 = result.products().get(3);
      assertThat(product4.name()).isEqualTo("Product 4");
      assertThat(product4.price()).isEqualByComparingTo("400.00");
      assertThat(product4.description()).isEqualTo("Fourth product");
    }
  }

  // ==================== СРАВНИТЕЛЬНЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Сравнительные тесты (с транзакцией vs без транзакции)")
  class ComparisonTests {

    @Test
    @DisplayName("[СРАВНЕНИЕ] Оба метода вызывают сервис с одинаковыми данными")
    void bothMethods_shouldUseSameBulkData() throws Exception {
      // Получаем данные из приватного метода
      java.lang.reflect.Method method = TransactionDemoController.class.getDeclaredMethod("createTestBulkData");
      method.setAccessible(true);
      BulkProductCreateDto expectedBulkData = (BulkProductCreateDto) method.invoke(transactionDemoController);

      // Мокируем оба метода сервиса
      when(productService.createProductsBulk(any(BulkProductCreateDto.class)))
          .thenReturn(List.of());
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class)))
          .thenReturn(List.of());

      // Вызываем оба метода контроллера
      transactionDemoController.testWithTransaction();
      transactionDemoController.testWithoutTransaction();

      // Проверяем, что оба метода сервиса получили одинаковые данные
      ArgumentCaptor<BulkProductCreateDto> captorWithTx = ArgumentCaptor.forClass(BulkProductCreateDto.class);
      ArgumentCaptor<BulkProductCreateDto> captorWithoutTx = ArgumentCaptor.forClass(BulkProductCreateDto.class);

      verify(productService).createProductsBulk(captorWithTx.capture());
      verify(productService).createProductsBulkWithoutTransaction(captorWithoutTx.capture());

      BulkProductCreateDto bulkWithTx = captorWithTx.getValue();
      BulkProductCreateDto bulkWithoutTx = captorWithoutTx.getValue();

      assertThat(bulkWithTx.products()).hasSize(4);
      assertThat(bulkWithoutTx.products()).hasSize(4);
      assertThat(bulkWithTx.products().get(0).name()).isEqualTo(bulkWithoutTx.products().get(0).name());
      assertThat(bulkWithTx.products().get(1).name()).isEqualTo(bulkWithoutTx.products().get(1).name());
      assertThat(bulkWithTx.products().get(2).name()).isEqualTo(bulkWithoutTx.products().get(2).name());
      assertThat(bulkWithTx.products().get(3).name()).isEqualTo(bulkWithoutTx.products().get(3).name());
    }

    @Test
    @DisplayName("[СРАВНЕНИЕ] Разные сообщения об ошибках для транзакционного и нетранзакционного режимов")
    void errorMessages_shouldBeDifferentForBothModes() {
      // Arrange
      RuntimeException exception = new RuntimeException("Test error");
      when(productService.createProductsBulk(any(BulkProductCreateDto.class))).thenThrow(exception);
      when(productService.createProductsBulkWithoutTransaction(any(BulkProductCreateDto.class))).thenThrow(exception);

      // Act
      ResponseEntity<String> responseWithTx = transactionDemoController.testWithTransaction();
      ResponseEntity<String> responseWithoutTx = transactionDemoController.testWithoutTransaction();

      // Assert
      assertThat(responseWithTx.getBody()).contains("TRANSACTION ROLLBACK");
      assertThat(responseWithoutTx.getBody()).contains("PARTIAL SAVE");
      assertThat(responseWithTx.getBody()).doesNotContain("PARTIAL SAVE");
      assertThat(responseWithoutTx.getBody()).doesNotContain("TRANSACTION ROLLBACK");
    }
  }
}