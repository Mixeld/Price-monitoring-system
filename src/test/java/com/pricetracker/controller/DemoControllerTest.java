package com.pricetracker.controller;

import com.pricetracker.service.DemoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для DemoController")
class DemoControllerTest {

  @Mock
  private DemoService demoService;

  @InjectMocks
  private DemoController demoController;

  // ==================== ТЕСТЫ ДЛЯ testTransaction С useTransaction = true ====================

  @Nested
  @DisplayName("Тесты метода testTransaction с useTransaction = true")
  class WithTransactionTests {

    @Test
    @DisplayName("[УСПЕХ] Обработка исключения при saveWithTransaction")
    void testTransaction_withTransaction_shouldCatchException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Ошибка! Но транзакция есть, поэтому данные откатятся.");
      doThrow(expectedException).when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: Ошибка! Но транзакция есть, поэтому данные откатятся.");
      verify(demoService).saveWithTransaction();
      verify(demoService, never()).saveWithoutTransaction();
    }

    @Test
    @DisplayName("[УСПЕХ] Обработка DataIntegrityViolationException при saveWithTransaction")
    void testTransaction_withTransaction_shouldCatchDataIntegrityException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Не удалось сохранить категорию с транзакцией");
      doThrow(expectedException).when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: Не удалось сохранить категорию с транзакцией");
      verify(demoService).saveWithTransaction();
      verify(demoService, never()).saveWithoutTransaction();
    }

    @Test
    @DisplayName("[УСПЕХ] Обработка любой RuntimeException при saveWithTransaction")
    void testTransaction_withTransaction_shouldCatchAnyRuntimeException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Любая другая ошибка");
      doThrow(expectedException).when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: Любая другая ошибка");
      verify(demoService).saveWithTransaction();
      verify(demoService, never()).saveWithoutTransaction();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ testTransaction С useTransaction = false ====================

  @Nested
  @DisplayName("Тесты метода testTransaction с useTransaction = false")
  class WithoutTransactionTests {

    @Test
    @DisplayName("[УСПЕХ] Обработка исключения при saveWithoutTransaction")
    void testTransaction_withoutTransaction_shouldCatchException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Ошибка! Но транзакции нет, поэтому данные не откатятся.");
      doThrow(expectedException).when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: Ошибка! Но транзакции нет, поэтому данные не откатятся.");
      verify(demoService).saveWithoutTransaction();
      verify(demoService, never()).saveWithTransaction();
    }

    @Test
    @DisplayName("[УСПЕХ] Обработка DataIntegrityViolationException при saveWithoutTransaction")
    void testTransaction_withoutTransaction_shouldCatchDataIntegrityException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Не удалось сохранить категорию без транзакции");
      doThrow(expectedException).when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: Не удалось сохранить категорию без транзакции");
      verify(demoService).saveWithoutTransaction();
      verify(demoService, never()).saveWithTransaction();
    }

    @Test
    @DisplayName("[УСПЕХ] Обработка любой RuntimeException при saveWithoutTransaction")
    void testTransaction_withoutTransaction_shouldCatchAnyRuntimeException() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("Любая другая ошибка без транзакции");
      doThrow(expectedException).when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: Любая другая ошибка без транзакции");
      verify(demoService).saveWithoutTransaction();
      verify(demoService, never()).saveWithTransaction();
    }
  }

  // ==================== ТЕСТЫ НА СЛУЧАЙ УСПЕХА (хотя по логике не должно происходить) ====================

  @Nested
  @DisplayName("Тесты на случай успешного выполнения (не должно происходить по логике)")
  class SuccessTests {

    @Test
    @DisplayName("[КРАЙНИЙ СЛУЧАЙ] Успешное выполнение с транзакцией")
    void testTransaction_withTransaction_success() {
      // Arrange
      doNothing().when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("Успех (не должно случиться)");
      verify(demoService).saveWithTransaction();
      verify(demoService, never()).saveWithoutTransaction();
    }

    @Test
    @DisplayName("[КРАЙНИЙ СЛУЧАЙ] Успешное выполнение без транзакции")
    void testTransaction_withoutTransaction_success() {
      // Arrange
      doNothing().when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("Успех (не должно случиться)");
      verify(demoService).saveWithoutTransaction();
      verify(demoService, never()).saveWithTransaction();
    }
  }

  // ==================== ТЕСТЫ НА РАЗЛИЧНЫЕ ТИПЫ ИСКЛЮЧЕНИЙ ====================

  @Nested
  @DisplayName("Тесты на различные типы исключений")
  class ExceptionTypeTests {

    @Test
    @DisplayName("[ГРАНИЦЫ] Исключение с null сообщением (с транзакцией)")
    void testTransaction_withTransaction_nullMessage() {
      // Arrange
      RuntimeException expectedException = new RuntimeException((String) null);
      doThrow(expectedException).when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: null");
      verify(demoService).saveWithTransaction();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Исключение с null сообщением (без транзакции)")
    void testTransaction_withoutTransaction_nullMessage() {
      // Arrange
      RuntimeException expectedException = new RuntimeException((String) null);
      doThrow(expectedException).when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: null");
      verify(demoService).saveWithoutTransaction();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Исключение с пустым сообщением (с транзакцией)")
    void testTransaction_withTransaction_emptyMessage() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("");
      doThrow(expectedException).when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: ");
      verify(demoService).saveWithTransaction();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Исключение с пустым сообщением (без транзакции)")
    void testTransaction_withoutTransaction_emptyMessage() {
      // Arrange
      RuntimeException expectedException = new RuntimeException("");
      doThrow(expectedException).when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: ");
      verify(demoService).saveWithoutTransaction();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Исключение с очень длинным сообщением (с транзакцией)")
    void testTransaction_withTransaction_veryLongMessage() {
      // Arrange
      String longMessage = "A".repeat(10000);
      RuntimeException expectedException = new RuntimeException(longMessage);
      doThrow(expectedException).when(demoService).saveWithTransaction();

      // Act
      String result = demoController.testTransaction(true);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: " + longMessage);
      verify(demoService).saveWithTransaction();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Исключение с очень длинным сообщением (без транзакции)")
    void testTransaction_withoutTransaction_veryLongMessage() {
      // Arrange
      String longMessage = "A".repeat(10000);
      RuntimeException expectedException = new RuntimeException(longMessage);
      doThrow(expectedException).when(demoService).saveWithoutTransaction();

      // Act
      String result = demoController.testTransaction(false);

      // Assert
      assertThat(result).isEqualTo("ПОЙМАНА ОШИБКА: " + longMessage);
      verify(demoService).saveWithoutTransaction();
    }
  }
}