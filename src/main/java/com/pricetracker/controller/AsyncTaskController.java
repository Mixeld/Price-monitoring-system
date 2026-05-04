package com.pricetracker.controller;

import com.pricetracker.service.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Async Task Management", description = "Управление асинхронными задачами")
public class AsyncTaskController {

  private final AsyncTaskService asyncTaskService;

  @Operation(summary = "Запустить массовое обновление цен в категории")
  @PostMapping("/bulk-update")
  public Map<String, Object> bulkPriceUpdate(
      @RequestParam String categoryName,
      @RequestParam BigDecimal newPrice) {

    // Теперь возвращает String taskId, как и ожидалось
    String taskId = asyncTaskService.startBulkPriceUpdate(categoryName, newPrice);

    return Map.of(
        "taskId", taskId,
        "status", AsyncTaskService.STATUS_PENDING, // Используем константу из сервиса
        "message", "Асинхронная задача запущена",
        "checkStatusUrl", "/api/tasks/status/" + taskId
    );
  }

  @Operation(summary = "Запустить экспорт данных")
  @PostMapping("/export")
  public Map<String, Object> exportData(
      @RequestParam String entityType,
      @RequestParam Long entityId) {

    String taskId = asyncTaskService.startDataExport(entityType, entityId);

    return Map.of(
        "taskId", taskId,
        "status", AsyncTaskService.STATUS_PENDING,
        "checkStatusUrl", "/api/tasks/status/" + taskId
    );
  }

  @Operation(summary = "Получить статус задачи")
  @GetMapping("/status/{taskId}")
  public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
    return asyncTaskService.getTaskInfo(taskId);
  }

  @Operation(summary = "Получить статистику по задачам")
  @GetMapping("/statistics")
  public Map<String, Integer> getTaskStatistics() {
    return asyncTaskService.getTaskStatistics();
  }

  @Operation(summary = "Очистить старые задачи")
  @DeleteMapping("/cleanup")
  public Map<String, Object> cleanupOldTasks() {
    int cleaned = asyncTaskService.cleanupOldTasks();
    return Map.of("cleanedCount", cleaned, "message", "Статистика очищена");
  }
}