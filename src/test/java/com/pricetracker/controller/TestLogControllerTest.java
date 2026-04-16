package com.pricetracker.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для TestLogController")
class TestLogControllerTest {

  @InjectMocks
  private TestLogController testLogController;

  // ==================== ТЕСТЫ ДЛЯ test400 ====================

  @Nested
  @DisplayName("Тесты метода test400()")
  class Test400Tests {

    @Test
    @DisplayName("[ОШИБКА] Выброс IllegalArgumentException при вызове test400")
    void test400_shouldThrowIllegalArgumentException() {
      assertThatThrownBy(() -> testLogController.test400())
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Тестовая 400 ошибка - проверка логирования");
    }
  }

  // ==================== ТЕСТЫ ДЛЯ test500 ====================

  @Nested
  @DisplayName("Тесты метода test500()")
  class Test500Tests {

    @Test
    @DisplayName("[ОШИБКА] Выброс RuntimeException при вызове test500")
    void test500_shouldThrowRuntimeException() {
      assertThatThrownBy(() -> testLogController.test500())
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Тестовая 500 ошибка - проверка логирования");
    }
  }
}