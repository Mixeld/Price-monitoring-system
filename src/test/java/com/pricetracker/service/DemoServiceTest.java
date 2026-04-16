package com.pricetracker.service;

import com.pricetracker.entity.Category;
import com.pricetracker.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для DemoService")
class DemoServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private DemoService demoService;

  @Test
  @DisplayName("saveWithoutTransaction должен вызвать save и бросить исключение")
  void saveWithoutTransaction_shouldCallSaveAndThrowException() {
    // Arrange
    // Моделируем успешное сохранение
    when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

    // Act & Assert
    // Проверяем, что метод бросает именно то исключение, которое стоит в конце
    assertThatThrownBy(() -> demoService.saveWithoutTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Ошибка! Но транзакции нет, поэтому данные не откатятся.");

    // Verify
    // Убеждаемся, что метод save был вызван ровно один раз
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("saveWithoutTransaction должен обрабатывать ошибку сохранения")
  void saveWithoutTransaction_shouldHandleSaveException() {
    // Arrange
    // Моделируем ошибку при сохранении
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new DataIntegrityViolationException("DB error"));

    // Act & Assert
    // Проверяем, что метод перехватывает ошибку и бросает свою
    assertThatThrownBy(() -> demoService.saveWithoutTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Не удалось сохранить категорию без транзакции");

    // Verify
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("saveWithTransaction должен вызвать save и бросить исключение")
  void saveWithTransaction_shouldCallSaveAndThrowException() {
    // Arrange
    when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

    // Act & Assert
    assertThatThrownBy(() -> demoService.saveWithTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Ошибка! Но транзакция есть, поэтому данные откатятся.");

    // Verify
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("saveWithTransaction должен обрабатывать ошибку сохранения")
  void saveWithTransaction_shouldHandleSaveException() {
    // Arrange
    when(categoryRepository.save(any(Category.class)))
        .thenThrow(new DataIntegrityViolationException("DB error"));

    // Act & Assert
    assertThatThrownBy(() -> demoService.saveWithTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Не удалось сохранить категорию с транзакцией");

    // Verify
    verify(categoryRepository, times(1)).save(any(Category.class));
  }
}