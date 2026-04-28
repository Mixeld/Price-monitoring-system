package com.pricetracker.service;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.entity.Product;
import com.pricetracker.exception.ResourceNotFoundException;
import com.pricetracker.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для AsyncTaskService")
class AsyncTaskServiceTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private PriceHistoryService priceHistoryService;

  @InjectMocks
  private AsyncTaskService asyncTaskService;

  private static final String TEST_CATEGORY = "Electronics";
  private static final BigDecimal TEST_PRICE = new BigDecimal("999.99");
  private static final Long TEST_PRODUCT_ID = 1L;

  @BeforeEach
  void setUp() {
    asyncTaskService = new AsyncTaskService(productRepository, priceHistoryService);
  }

  @Test
  @DisplayName("getTaskInfo - задача не найдена")
  void testGetTaskInfoNotFound() {
    Map<String, Object> info = asyncTaskService.getTaskInfo("invalid");
    assertThat(info).containsKey("error");
    assertThat(info.get("error")).isEqualTo("Задача не найдена");
  }

  @Test
  @DisplayName("getTaskInfo - статус COMPLETED")
  void testGetTaskInfoCompleted() {
    List<Product> products = createProductList(1);
    when(productRepository.findByCategoryName(TEST_CATEGORY)).thenReturn(products);
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    String taskId = asyncTaskService.startBulkPriceUpdate(TEST_CATEGORY, TEST_PRICE);

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      Map<String, Object> info = asyncTaskService.getTaskInfo(taskId);
      assertThat(info.get("status")).isEqualTo(AsyncTaskService.STATUS_COMPLETED);
      assertThat(info.get("statusDescription")).isEqualTo("Завершена успешно");
      assertThat(info.get("progress")).isEqualTo(100);
      assertThat(info.get("result")).isNotNull();
      assertThat(info.get("startTime")).isNotNull();
      assertThat(info.get("durationSeconds")).isNotNull();
    });
  }

  @Test
  @DisplayName("getTaskInfo - статус FAILED")
  void testGetTaskInfoFailed() {
    when(productRepository.findByCategoryName("ERROR")).thenThrow(new RuntimeException("System Error"));

    String taskId = asyncTaskService.startBulkPriceUpdate("ERROR", TEST_PRICE);

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      Map<String, Object> info = asyncTaskService.getTaskInfo(taskId);
      assertThat(info.get("status")).isEqualTo(AsyncTaskService.STATUS_FAILED);
      assertThat(info.get("statusDescription")).isEqualTo("Завершена с ошибкой");
      assertThat(info.get("error")).isNotNull();
    });
  }

  @Test
  @DisplayName("executeBulkPriceUpdate - успешное выполнение")
  void testExecuteBulkPriceUpdateSuccess() throws Exception {
    List<Product> products = createProductList(3);
    when(productRepository.findByCategoryName(TEST_CATEGORY)).thenReturn(products);
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    CompletableFuture<String> future = asyncTaskService.executeBulkPriceUpdate(
        UUID.randomUUID().toString(), TEST_CATEGORY, TEST_PRICE
    );

    String result = future.get(3, TimeUnit.SECONDS);
    assertThat(result).contains("Обновлено 3 продуктов");

    verify(priceHistoryService, times(3)).recordPrice(any(PriceHistoryDto.class));
    verify(productRepository, times(3)).save(any(Product.class));
  }

  @Test
  @DisplayName("executeBulkPriceUpdate - категория не найдена")
  void testExecuteBulkPriceUpdateCategoryNotFound() {
    when(productRepository.findByCategoryName(TEST_CATEGORY)).thenReturn(Collections.emptyList());

    CompletableFuture<String> future = asyncTaskService.executeBulkPriceUpdate(
        UUID.randomUUID().toString(), TEST_CATEGORY, TEST_PRICE
    );

    assertThatThrownBy(() -> future.get(2, TimeUnit.SECONDS))
        .hasCauseInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("executeBulkPriceUpdate - общая ошибка")
  void testExecuteBulkPriceUpdateGeneralError() {
    when(productRepository.findByCategoryName(TEST_CATEGORY)).thenThrow(new RuntimeException("DB Error"));

    CompletableFuture<String> future = asyncTaskService.executeBulkPriceUpdate(
        UUID.randomUUID().toString(), TEST_CATEGORY, TEST_PRICE
    );

    assertThatThrownBy(() -> future.get(2, TimeUnit.SECONDS))
        .hasCauseInstanceOf(RuntimeException.class)
        .hasMessageContaining("DB Error");
  }

  @Test
  @DisplayName("executeDataExport - экспорт продукта успешно")
  void testExecuteDataExportProductSuccess() throws Exception {
    Product product = new Product();
    product.setId(TEST_PRODUCT_ID);
    product.setName("Test Product");
    product.setPrice(TEST_PRICE);
    when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(product));

    CompletableFuture<String> future = asyncTaskService.executeDataExport(
        UUID.randomUUID().toString(), "product", TEST_PRODUCT_ID
    );

    String result = future.get(3, TimeUnit.SECONDS);
    assertThat(result).contains("Экспорт продукта");
    assertThat(result).contains("Test Product");
  }

  @Test
  @DisplayName("executeDataExport - продукт не найден")
  void testExecuteDataExportProductNotFound() {
    when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.empty());

    CompletableFuture<String> future = asyncTaskService.executeDataExport(
        UUID.randomUUID().toString(), "product", TEST_PRODUCT_ID
    );

    assertThatThrownBy(() -> future.get(2, TimeUnit.SECONDS))
        .hasCauseInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("executeDataExport - экспорт категории")
  void testExecuteDataExportCategory() throws Exception {
    CompletableFuture<String> future = asyncTaskService.executeDataExport(
        UUID.randomUUID().toString(), "category", TEST_PRODUCT_ID
    );

    String result = future.get(3, TimeUnit.SECONDS);
    assertThat(result).contains("Экспорт категории");
  }

  @Test
  @DisplayName("executeDataExport - неизвестный тип")
  void testExecuteDataExportUnknownType() throws Exception {
    CompletableFuture<String> future = asyncTaskService.executeDataExport(
        UUID.randomUUID().toString(), "unknown", TEST_PRODUCT_ID
    );

    String result = future.get(3, TimeUnit.SECONDS);
    assertThat(result).contains("Экспорт unknown");
  }

  @Test
  @DisplayName("executeBatchProductUpdate - полный успех")
  void testExecuteBatchProductUpdateFullSuccess() throws Exception {
    Map<Long, BigDecimal> updates = Map.of(1L, new BigDecimal("100.00"), 2L, new BigDecimal("200.00"));

    Product product1 = new Product();
    product1.setId(1L);
    Product product2 = new Product();
    product2.setId(2L);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
    when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    CompletableFuture<String> future = asyncTaskService.executeBatchProductUpdate(
        UUID.randomUUID().toString(), updates
    );

    String result = future.get(3, TimeUnit.SECONDS);
    assertThat(result).contains("успешно=2, ошибок=0");
  }

  @Test
  @DisplayName("executeBatchProductUpdate - частичный успех")
  void testExecuteBatchProductUpdatePartialSuccess() throws Exception {
    Map<Long, BigDecimal> updates = Map.of(1L, new BigDecimal("100.00"), 2L, new BigDecimal("200.00"));

    Product product1 = new Product();
    product1.setId(1L);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
    when(productRepository.findById(2L)).thenReturn(Optional.empty());
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    CompletableFuture<String> future = asyncTaskService.executeBatchProductUpdate(
        UUID.randomUUID().toString(), updates
    );

    String result = future.get(3, TimeUnit.SECONDS);
    assertThat(result).contains("успешно=1, ошибок=1");
  }

  @Test
  @DisplayName("getAllActiveTasks - пустой результат")
  void testGetAllActiveTasksEmpty() {
    Map<String, Map<String, Object>> activeTasks = asyncTaskService.getAllActiveTasks();
    assertThat(activeTasks).isEmpty();
  }

  @Test
  @DisplayName("getTaskStatistics - базовая проверка")
  void testGetTaskStatistics() {
    Map<String, Integer> stats = asyncTaskService.getTaskStatistics();
    assertThat(stats).containsKeys("pending", "running", "completed", "failed", "total");
    assertThat(stats.get("total")).isEqualTo(0);
  }

  @Test
  @DisplayName("cleanupOldTasks - не удаляет новые задачи")
  void testCleanupOldTasksNoRemoval() {
    when(productRepository.findByCategoryName(TEST_CATEGORY)).thenReturn(Collections.emptyList());
    asyncTaskService.startBulkPriceUpdate(TEST_CATEGORY, TEST_PRICE);

    int cleaned = asyncTaskService.cleanupOldTasks();
    assertThat(cleaned).isEqualTo(0);
  }

  @Test
  @DisplayName("cleanupOldTasks - удаляет старые задачи через рефлексию")
  void testCleanupOldTasksWithRemoval() throws Exception {
    String oldTaskId = UUID.randomUUID().toString();

    // Используем рефлексию для добавления старой задачи
    java.lang.reflect.Field startTimeMapField = AsyncTaskService.class.getDeclaredField("taskStartTimeMap");
    startTimeMapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, LocalDateTime> startTimeMap = (Map<String, LocalDateTime>) startTimeMapField.get(asyncTaskService);
    startTimeMap.put(oldTaskId, LocalDateTime.now().minusHours(2));

    java.lang.reflect.Field statusMapField = AsyncTaskService.class.getDeclaredField("taskStatusMap");
    statusMapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, String> statusMap = (Map<String, String>) statusMapField.get(asyncTaskService);
    statusMap.put(oldTaskId, AsyncTaskService.STATUS_COMPLETED);

    int cleaned = asyncTaskService.cleanupOldTasks();
    assertThat(cleaned).isEqualTo(1);
    assertThat(asyncTaskService.getTaskStatus(oldTaskId)).isNull();
  }

  @Test
  @DisplayName("Геттеры для несуществующих задач")
  void testGettersForNonExistentTasks() {
    assertThat(asyncTaskService.getTaskStatus("nonexistent")).isNull();
    assertThat(asyncTaskService.getTaskResult("nonexistent")).isNull();
    assertThat(asyncTaskService.getTaskError("nonexistent")).isNull();
    assertThat(asyncTaskService.getTaskProgress("nonexistent")).isEqualTo(0);
    assertThat(asyncTaskService.isTaskCompleted("nonexistent")).isFalse();
    assertThat(asyncTaskService.isTaskFailed("nonexistent")).isFalse();
  }

  @Test
  @DisplayName("startBulkPriceUpdate - успешный запуск")
  void testStartBulkPriceUpdateSuccess() {
    List<Product> products = createProductList(1);
    when(productRepository.findByCategoryName(TEST_CATEGORY)).thenReturn(products);
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    String taskId = asyncTaskService.startBulkPriceUpdate(TEST_CATEGORY, TEST_PRICE);
    assertThat(taskId).isNotNull();

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      assertThat(asyncTaskService.getTaskStatus(taskId)).isEqualTo(AsyncTaskService.STATUS_COMPLETED);
    });
  }

  @Test
  @DisplayName("startDataExport - успешный запуск")
  void testStartDataExportSuccess() {
    Product product = new Product();
    product.setId(TEST_PRODUCT_ID);
    when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(Optional.of(product));

    String taskId = asyncTaskService.startDataExport("product", TEST_PRODUCT_ID);
    assertThat(taskId).isNotNull();

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      assertThat(asyncTaskService.getTaskStatus(taskId)).isEqualTo(AsyncTaskService.STATUS_COMPLETED);
    });
  }

  @Test
  @DisplayName("startBatchProductUpdate - успешный запуск")
  void testStartBatchProductUpdateSuccess() {
    Map<Long, BigDecimal> updates = Map.of(1L, TEST_PRICE);
    Product product = new Product();
    product.setId(1L);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

    String taskId = asyncTaskService.startBatchProductUpdate(updates);
    assertThat(taskId).isNotNull();

    await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
      assertThat(asyncTaskService.getTaskStatus(taskId)).isEqualTo(AsyncTaskService.STATUS_COMPLETED);
    });
  }

  private List<Product> createProductList(int count) {
    List<Product> products = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
      Product product = new Product();
      product.setId((long) i);
      product.setName("Product " + i);
      product.setPrice(new BigDecimal("100.00"));
      products.add(product);
    }
    return products;
  }
}