package com.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class AsyncTaskService {

  public static final String STATUS_PENDING = "PENDING";
  public static final String STATUS_RUNNING = "RUNNING";
  public static final String STATUS_COMPLETED = "COMPLETED";
  public static final String STATUS_FAILED = "FAILED";

  private final AtomicInteger successCounter = new AtomicInteger(0);

  private final Map<String, Map<String, Object>> taskStorage = new ConcurrentHashMap<>();

  private final TaskExecutor taskExecutor;

  public AsyncTaskService(@Qualifier("taskExecutor") TaskExecutor taskExecutor) {
    this.taskExecutor = taskExecutor;
  }

  public String startBulkPriceUpdate(String categoryName, BigDecimal newPrice) {
    String taskId = generateTaskId();
    taskExecutor.execute(() -> runTaskLogic(taskId, "Массовое обновление категории: " + categoryName));
    return taskId;
  }

  public String startDataExport(String entityType, Long entityId) {
    String taskId = generateTaskId();
    taskExecutor.execute(() -> runTaskLogic(taskId, "Экспорт сущности [" + entityType + "] ID: " + entityId));
    return taskId;
  }

  public String startBatchProductUpdate(Map<Long, BigDecimal> updates) {
    String taskId = generateTaskId();
    taskExecutor.execute(() -> runTaskLogic(taskId, "Пакетное обновление: " + updates.size() + " товаров"));
    return taskId;
  }

  private String generateTaskId() {
    String taskId = UUID.randomUUID().toString();
    Map<String, Object> taskInfo = new ConcurrentHashMap<>();
    taskInfo.put("taskId", taskId);
    taskInfo.put("status", STATUS_PENDING);
    taskInfo.put("progress", "0%");
    taskInfo.put("startTime", LocalDateTime.now().toString());
    taskStorage.put(taskId, taskInfo);
    return taskId;
  }

  private void runTaskLogic(String taskId, String description) {
    Map<String, Object> taskInfo = taskStorage.get(taskId);
    if (taskInfo == null) return;

    try {
      log.info("--- [START] Задача {} начала работу ---", taskId);
      taskInfo.put("status", STATUS_RUNNING);
      taskInfo.put("description", description);

      for (int i = 1; i <= 3; i++) {
        Thread.sleep(5000);
        int currentProgress = i * 33;
        if (currentProgress > 100) currentProgress = 100;

        taskInfo.put("progress", currentProgress + "%");
        log.info("Задача {}: шаг {}/3 выполнен", taskId, i);
      }

      taskInfo.put("status", STATUS_COMPLETED);
      taskInfo.put("progress", "100%");
      taskInfo.put("endTime", LocalDateTime.now().toString());

      successCounter.incrementAndGet();
      log.info("--- [SUCCESS] Задача {} полностью завершена ---", taskId);

    } catch (InterruptedException e) {
      log.error("Задача {} была прервана", taskId);
      taskInfo.put("status", STATUS_FAILED);
      taskInfo.put("error", "Процесс прерван");
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      log.error("Ошибка при выполнении задачи {}: {}", taskId, e.getMessage());
      taskInfo.put("status", STATUS_FAILED);
      taskInfo.put("error", e.getMessage());
    }
  }

  // --- Методы получения данных (используются контроллером) ---

  public Map<String, Object> getTaskInfo(String taskId) {
    return taskStorage.getOrDefault(taskId, Map.of("error", "Задача " + taskId + " не найдена"));
  }

  public Map<String, Integer> getTaskStatistics() {
    int total = taskStorage.size();
    int completed = (int) taskStorage.values().stream()
        .filter(t -> STATUS_COMPLETED.equals(t.get("status")))
        .count();

    return Map.of(
        "totalTasksCreated", total,
        "tasksInStorage", taskStorage.size(),
        "atomicSuccessCounter", successCounter.get()
    );
  }

  public Map<String, Map<String, Object>> getAllActiveTasks() {
    return taskStorage;
  }

  public int cleanupOldTasks() {
    int count = taskStorage.size();
    taskStorage.clear();
    log.info("Память очищена. Удалено задач: {}", count);
    return count;
  }
}