package com.pricetracker.service;

import com.pricetracker.entity.Category;
import com.pricetracker.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для DemoService")
class DemoServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private DemoService demoService;

  @Test
  @DisplayName("Покрытие saveWithoutTransaction: успешное сохранение")
  void testSaveWithoutTransactionSuccess() {
    // Настройка - save не выбрасывает исключение
    when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

    // Вызов метода и проверка, что выбрасывается последнее исключение
    assertThatThrownBy(() -> demoService.saveWithoutTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Ошибка! Но транзакции нет, поэтому данные не откатятся.");

    // Проверка, что save был вызван ровно один раз
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("Покрытие saveWithoutTransaction: DataAccessException при сохранении")
  void testSaveWithoutTransactionDataAccessException() {
    // Настройка - save выбрасывает DataAccessException
    DataAccessException dataAccessException = new DataAccessException("DB connection failed") {};
    when(categoryRepository.save(any(Category.class))).thenThrow(dataAccessException);

    // Вызов метода и проверка, что исключение оборачивается в DataIntegrityViolationException
    assertThatThrownBy(() -> demoService.saveWithoutTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Не удалось сохранить категорию без транзакции")
        .hasCause(dataAccessException);

    // Проверка, что save был вызван ровно один раз
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("Покрытие saveWithTransaction: успешное сохранение")
  void testSaveWithTransactionSuccess() {
    // Настройка - save не выбрасывает исключение
    when(categoryRepository.save(any(Category.class))).thenReturn(new Category());

    // Вызов метода и проверка, что выбрасывается последнее исключение
    assertThatThrownBy(() -> demoService.saveWithTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Ошибка! Но транзакция есть, поэтому данные откатятся.");

    // Проверка, что save был вызван ровно один раз
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("Покрытие saveWithTransaction: DataAccessException при сохранении")
  void testSaveWithTransactionDataAccessException() {
    // Настройка - save выбрасывает DataAccessException
    DataAccessException dataAccessException = new DataAccessException("DB connection failed") {};
    when(categoryRepository.save(any(Category.class))).thenThrow(dataAccessException);

    // Вызов метода и проверка, что исключение оборачивается в DataIntegrityViolationException
    assertThatThrownBy(() -> demoService.saveWithTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Не удалось сохранить категорию с транзакцией")
        .hasCause(dataAccessException);

    // Проверка, что save был вызван ровно один раз
    verify(categoryRepository, times(1)).save(any(Category.class));
  }

  @Test
  @DisplayName("Покрытие обоих методов в одном тесте для проверки интеграции")
  void testBothMethodsCoverage() {
    // Тест для saveWithoutTransaction - успешное сохранение
    when(categoryRepository.save(any(Category.class)))
        .thenReturn(new Category())
        .thenThrow(new DataAccessException("DB error") {});

    // Первый вызов - saveWithoutTransaction успешно сохраняет, но выбрасывает последнее исключение
    assertThatThrownBy(() -> demoService.saveWithoutTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Ошибка! Но транзакции нет, поэтому данные не откатятся.");

    // Второй вызов - saveWithTransaction выбрасывает DataAccessException
    assertThatThrownBy(() -> demoService.saveWithTransaction())
        .isInstanceOf(DataIntegrityViolationException.class)
        .hasMessage("Не удалось сохранить категорию с транзакцией");

    // Проверка, что save был вызван 2 раза
    verify(categoryRepository, times(2)).save(any(Category.class));
  }
}