package com.pricetracker.controller;

import com.pricetracker.service.AsyncTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Tag(name = "Async Tasks", description = "Асинхронные операции")
@RestController
@RequestMapping("/api/async-tasks")
@RequiredArgsConstructor
public class AsyncTaskController {

  private final AsyncTaskService asyncTaskService;

  @Operation(summary = "Запустить массовое обновление цен в категории")
  @PostMapping("/bulk-price-update")
  public ResponseEntity<Map<String, Object>> startBulkPriceUpdate(
      @RequestParam String categoryName,
      @RequestParam BigDecimal newPrice) {

    String taskId = asyncTaskService.startBulkPriceUpdate(categoryName, newPrice);

    return ResponseEntity.ok(Map.of(
        "taskId", taskId,
        "status", "started",
        "message", "Асинхронная задача запущена",
        "checkStatusUrl", "/api/async-tasks/" + taskId + "/status"
    ));
  }

  @Operation(summary = "Запустить экспорт данных")
  @PostMapping("/export")
  public ResponseEntity<Map<String, Object>> startExport(
      @RequestParam String entityType,
      @RequestParam Long entityId) {

    String taskId = asyncTaskService.startDataExport(entityType, entityId);

    return ResponseEntity.ok(Map.of(
        "taskId", taskId,
        "status", "started",
        "entityType", entityType,
        "entityId", entityId,
        "checkStatusUrl", "/api/async-tasks/" + taskId + "/status"
    ));
  }

  @Operation(summary = "Запустить пакетное обновление цен")
  @PostMapping("/batch-update")
  public ResponseEntity<Map<String, Object>> startBatchUpdate(
      @RequestBody Map<Long, BigDecimal> productPriceUpdates) {

    String taskId = asyncTaskService.startBatchProductUpdate(productPriceUpdates);

    return ResponseEntity.ok(Map.of(
        "taskId", taskId,
        "status", "started",
        "productsCount", productPriceUpdates.size(),
        "checkStatusUrl", "/api/async-tasks/" + taskId + "/status"
    ));
  }

  @Operation(summary = "Получить статус задачи")
  @GetMapping("/{taskId}/status")
  public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
    Map<String, Object> taskInfo = asyncTaskService.getTaskInfo(taskId);

    if (taskInfo.containsKey("error")) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(taskInfo);
  }

  @Operation(summary = "Получить все активные задачи")
  @GetMapping("/active")
  public ResponseEntity<Map<String, Map<String, Object>>> getActiveTasks() {
    return ResponseEntity.ok(asyncTaskService.getAllActiveTasks());
  }

  @Operation(summary = "Получить статистику по задачам")
  @GetMapping("/statistics")
  public ResponseEntity<Map<String, Integer>> getTaskStatistics() {
    return ResponseEntity.ok(asyncTaskService.getTaskStatistics());
  }

  @Operation(summary = "Очистить старые задачи")
  @DeleteMapping("/cleanup")
  public ResponseEntity<Map<String, Object>> cleanupOldTasks() {
    int cleanedCount = asyncTaskService.cleanupOldTasks();
    return ResponseEntity.ok(Map.of(
        "message", "Очистка завершена",
        "cleanedTasks", cleanedCount,
        "statistics", asyncTaskService.getTaskStatistics()
    ));
  }
}