package com.pricetracker.service;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.entity.Product;
import com.pricetracker.exception.ResourceNotFoundException;
import com.pricetracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

  private final ProductRepository productRepository;
  private final PriceHistoryService priceHistoryService;

  public static final String STATUS_PENDING = "PENDING";
  public static final String STATUS_RUNNING = "RUNNING";
  public static final String STATUS_COMPLETED = "COMPLETED";
  public static final String STATUS_FAILED = "FAILED";

  private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();
  private final Map<String, String> taskResultMap = new ConcurrentHashMap<>();
  private final Map<String, LocalDateTime> taskStartTimeMap = new ConcurrentHashMap<>();
  private final Map<String, String> taskErrorMap = new ConcurrentHashMap<>();
  private final Map<String, Integer> taskProgressMap = new ConcurrentHashMap<>();

  public String startBulkPriceUpdate(String categoryName, BigDecimal newPrice) {
    String taskId = UUID.randomUUID().toString();
    taskStatusMap.put(taskId, STATUS_PENDING);
    taskStartTimeMap.put(taskId, LocalDateTime.now());
    taskProgressMap.put(taskId, 0);

    log.info("Запущена асинхронная задача {} для категории {} с новой ценой {}",
        taskId, categoryName, newPrice);

    executeBulkPriceUpdate(taskId, categoryName, newPrice);

    return taskId;
  }

  public String startDataExport(String entityType, Long entityId) {
    String taskId = UUID.randomUUID().toString();
    taskStatusMap.put(taskId, STATUS_PENDING);
    taskStartTimeMap.put(taskId, LocalDateTime.now());
    taskProgressMap.put(taskId, 0);

    log.info("Запущена задача экспорта {} для ID: {}", taskId, entityId);

    executeDataExport(taskId, entityType, entityId);

    return taskId;
  }

  public String startBatchProductUpdate(Map<Long, BigDecimal> productPriceUpdates) {
    String taskId = UUID.randomUUID().toString();
    taskStatusMap.put(taskId, STATUS_PENDING);
    taskStartTimeMap.put(taskId, LocalDateTime.now());
    taskProgressMap.put(taskId, 0);

    log.info("Запущена пакетная задача {} для {} продуктов", taskId, productPriceUpdates.size());

    executeBatchProductUpdate(taskId, productPriceUpdates);

    return taskId;
  }

  @Async("taskExecutor")
  public CompletableFuture<String> executeBulkPriceUpdate(String taskId, String categoryName, BigDecimal newPrice) {
    log.info("Задача {} начала выполнение в потоке: {}", taskId, Thread.currentThread().getName());
    taskStatusMap.put(taskId, STATUS_RUNNING);

    try {
      var products = productRepository.findByCategoryName(categoryName);

      if (products.isEmpty()) {
        throw new ResourceNotFoundException("Category", "name", categoryName);
      }

      int total = products.size();
      int updatedCount = 0;

      for (int i = 0; i < products.size(); i++) {
        Product product = products.get(i);

        PriceHistoryDto historyDto = new PriceHistoryDto(
            null,
            product.getPrice(),
            LocalDateTime.now(),
            null,
            product.getId(),
            null
        );
        priceHistoryService.recordPrice(historyDto);

        product.setPrice(newPrice);
        productRepository.save(product);
        updatedCount++;

        int progress = (i + 1) * 100 / total;
        taskProgressMap.put(taskId, progress);

        Thread.sleep(50);
      }

      String result = String.format("Обновлено %d продуктов в категории '%s' до цены %s",
          updatedCount, categoryName, newPrice);
      taskResultMap.put(taskId, result);
      taskStatusMap.put(taskId, STATUS_COMPLETED);
      taskProgressMap.put(taskId, 100);

      log.info("Задача {} успешно завершена. {}", taskId, result);
      return CompletableFuture.completedFuture(result);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      taskStatusMap.put(taskId, STATUS_FAILED);
      taskErrorMap.put(taskId, "Задача была прервана: " + e.getMessage());
      log.error("Задача {} была прервана", taskId, e);
      return CompletableFuture.failedFuture(e);

    } catch (Exception e) {
      taskStatusMap.put(taskId, STATUS_FAILED);
      taskErrorMap.put(taskId, e.getMessage());
      log.error("Задача {} провалилась", taskId, e);
      return CompletableFuture.failedFuture(e);
    }
  }

  @Async("taskExecutor")
  public CompletableFuture<String> executeDataExport(String taskId, String entityType, Long entityId) {
    log.info("Экспорт задачи {} в потоке: {}", taskId, Thread.currentThread().getName());
    taskStatusMap.put(taskId, STATUS_RUNNING);

    try {
      for (int progress = 0; progress <= 100; progress += 20) {
        taskProgressMap.put(taskId, progress);
        Thread.sleep(500);
        log.debug("Прогресс экспорта задачи {}: {}%", taskId, progress);
      }

      String result = switch (entityType) {
        case "product" -> {
          Product product = productRepository.findById(entityId)
              .orElseThrow(() -> new ResourceNotFoundException("Product", "id", entityId));
          yield String.format("Экспорт продукта: ID=%d, Name='%s', Price=%s",
              product.getId(), product.getName(), product.getPrice());
        }
        case "category" -> {
          yield String.format("Экспорт категории: ID=%d", entityId);
        }
        default -> String.format("Экспорт %s с ID=%d", entityType, entityId);
      };

      taskResultMap.put(taskId, result);
      taskStatusMap.put(taskId, STATUS_COMPLETED);
      taskProgressMap.put(taskId, 100);

      log.info("Экспорт задачи {} завершен", taskId);
      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      taskStatusMap.put(taskId, STATUS_FAILED);
      taskErrorMap.put(taskId, e.getMessage());
      log.error("Экспорт задачи {} провалился", taskId, e);
      return CompletableFuture.failedFuture(e);
    }
  }

  @Async("taskExecutor")
  public CompletableFuture<String> executeBatchProductUpdate(String taskId, Map<Long, BigDecimal> productPriceUpdates) {
    log.info("Пакетная задача {} начала выполнение", taskId);
    taskStatusMap.put(taskId, STATUS_RUNNING);

    try {
      int total = productPriceUpdates.size();
      int updatedCount = 0;
      int failedCount = 0;

      for (Map.Entry<Long, BigDecimal> entry : productPriceUpdates.entrySet()) {
        try {
          Long productId = entry.getKey();
          BigDecimal newPrice = entry.getValue();

          Product product = productRepository.findById(productId)
              .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

          PriceHistoryDto historyDto = new PriceHistoryDto(
              null,
              product.getPrice(),
              LocalDateTime.now(),
              null,
              product.getId(),
              null
          );
          priceHistoryService.recordPrice(historyDto);

          product.setPrice(newPrice);
          productRepository.save(product);
          updatedCount++;

        } catch (Exception e) {
          failedCount++;
          log.error("Ошибка при обновлении продукта {}", entry.getKey(), e);
        }

        int progress = (updatedCount + failedCount) * 100 / total;
        taskProgressMap.put(taskId, progress);
      }

      String result = String.format("Пакетное обновление завершено: успешно=%d, ошибок=%d",
          updatedCount, failedCount);
      taskResultMap.put(taskId, result);
      taskStatusMap.put(taskId, STATUS_COMPLETED);
      taskProgressMap.put(taskId, 100);

      log.info("Пакетная задача {} завершена", taskId);
      return CompletableFuture.completedFuture(result);

    } catch (Exception e) {
      taskStatusMap.put(taskId, STATUS_FAILED);
      taskErrorMap.put(taskId, e.getMessage());
      log.error("Пакетная задача {} провалилась", taskId, e);
      return CompletableFuture.failedFuture(e);
    }
  }

  public String getTaskStatus(String taskId) {
    return taskStatusMap.getOrDefault(taskId, null);
  }

  public boolean isTaskCompleted(String taskId) {
    return STATUS_COMPLETED.equals(taskStatusMap.get(taskId));
  }

  public boolean isTaskFailed(String taskId) {
    return STATUS_FAILED.equals(taskStatusMap.get(taskId));
  }

  public String getTaskResult(String taskId) {
    return taskResultMap.get(taskId);
  }

  public String getTaskError(String taskId) {
    return taskErrorMap.get(taskId);
  }

  public int getTaskProgress(String taskId) {
    return taskProgressMap.getOrDefault(taskId, 0);
  }

  public Map<String, Object> getTaskInfo(String taskId) {
    String status = getTaskStatus(taskId);

    if (status == null) {
      return Map.of("error", "Задача не найдена");
    }

    Map<String, Object> info = new ConcurrentHashMap<>();
    info.put("taskId", taskId);
    info.put("status", status);
    info.put("statusDescription", getStatusDescription(status));
    info.put("progress", getTaskProgress(taskId));
    info.put("result", getTaskResult(taskId));
    info.put("error", getTaskError(taskId));
    info.put("startTime", taskStartTimeMap.get(taskId));

    if (taskStartTimeMap.containsKey(taskId)) {
      long durationSeconds = Duration.between(taskStartTimeMap.get(taskId), LocalDateTime.now()).getSeconds();
      info.put("durationSeconds", durationSeconds);
    }

    return info;
  }

  public Map<String, Map<String, Object>> getAllActiveTasks() {
    Map<String, Map<String, Object>> activeTasks = new ConcurrentHashMap<>();

    for (String taskId : taskStatusMap.keySet()) {
      String status = taskStatusMap.get(taskId);
      if (STATUS_PENDING.equals(status) || STATUS_RUNNING.equals(status)) {
        activeTasks.put(taskId, getTaskInfo(taskId));
      }
    }

    return activeTasks;
  }

  public Map<String, Integer> getTaskStatistics() {
    int pending = 0;
    int running = 0;
    int completed = 0;
    int failed = 0;

    for (String status : taskStatusMap.values()) {
      switch (status) {
        case STATUS_PENDING -> pending++;
        case STATUS_RUNNING -> running++;
        case STATUS_COMPLETED -> completed++;
        case STATUS_FAILED -> failed++;
      }
    }

    return Map.of(
        "pending", pending,
        "running", running,
        "completed", completed,
        "failed", failed,
        "total", taskStatusMap.size()
    );
  }

  public int cleanupOldTasks() {
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
    int cleanedCount = 0;

    for (String taskId : Set.copyOf(taskStartTimeMap.keySet())) {
      LocalDateTime startTime = taskStartTimeMap.get(taskId);
      if (startTime != null && startTime.isBefore(oneHourAgo)) {
        taskStatusMap.remove(taskId);
        taskResultMap.remove(taskId);
        taskErrorMap.remove(taskId);
        taskProgressMap.remove(taskId);
        taskStartTimeMap.remove(taskId);
        cleanedCount++;
      }
    }

    log.info("Очищено {} старых задач. Текущий размер кэша: {}", cleanedCount, taskStatusMap.size());
    return cleanedCount;
  }

  private String getStatusDescription(String status) {
    switch (status) {
      case STATUS_PENDING:
        return "Ожидает выполнения";
      case STATUS_RUNNING:
        return "Выполняется";
      case STATUS_COMPLETED:
        return "Завершена успешно";
      case STATUS_FAILED:
        return "Завершена с ошибкой";
      default:
        return "Неизвестный статус";
    }
  }
}