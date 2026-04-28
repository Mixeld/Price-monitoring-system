package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для LoadTestService (100% Coverage)")
class LoadTestServiceTest {

  @Mock private ProductService productService;
  @Mock private ProductRepository productRepository;
  @Mock private AsyncTaskService asyncTaskService;

  @InjectMocks private LoadTestService loadTestService;

  @BeforeEach
  void setUp() {
    loadTestService.resetStats();
  }

  @Test
  @DisplayName("Покрытие всех веток: Успех, Ошибки, Ретраи")
  void runLoadTest_FullCoverage() throws InterruptedException {
    // Настройка для всех веток j%3
    when(productService.getProducts(null)).thenReturn(Collections.emptyList());
    when(productService.saveProduct(any(ProductDto.class))).thenReturn(new ProductDto(1L, "T", BigDecimal.ONE, "D", "C"));

    // Настройка для j%3 == 2: первый вызов заставит зайти в while и sleep, второй - завершить
    when(asyncTaskService.startDataExport(anyString(), anyLong())).thenReturn("task-1");
    when(asyncTaskService.getTaskStatus("task-1"))
        .thenReturn("WAITING")
        .thenReturn(AsyncTaskService.STATUS_COMPLETED);

    // Запуск (3 запроса пройдут все ветки if-else)
    loadTestService.runLoadTest(1, 3);

    // Проверка статистики
    Map<String, Object> stats = loadTestService.getLoadTestStats();
    assertThat(stats.get("totalRequests")).isEqualTo(3);
    assertThat(stats.get("errorRate")).isEqualTo("0.00%");

    // Покрытие ветки STATUS_FAILED
    reset(asyncTaskService);
    when(asyncTaskService.startDataExport(anyString(), anyLong())).thenReturn("task-2");
    when(asyncTaskService.getTaskStatus("task-2")).thenReturn(AsyncTaskService.STATUS_FAILED);
    loadTestService.runLoadTest(1, 3); // Запрос j=2 (индекс 2) попадет в FAILED

    // Покрытие Exception в catch
    reset(productService);
    when(productService.getProducts(null)).thenThrow(new RuntimeException("Error"));
    loadTestService.runLoadTest(1, 1);
  }

  @Test
  @DisplayName("Статистика при 0 запросов")
  void getLoadTestStats_Zero() {
    loadTestService.resetStats();
    Map<String, Object> stats = loadTestService.getLoadTestStats();
    assertThat(stats.get("totalRequests")).isEqualTo(0);
    assertThat(stats.get("errorRate")).isEqualTo("0.00%");
  }
}