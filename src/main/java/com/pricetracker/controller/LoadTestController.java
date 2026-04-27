package com.pricetracker.controller;

import com.pricetracker.service.LoadTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "Load Testing", description = "Нагрузочное тестирование")
@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
public class LoadTestController {

  private final LoadTestService loadTestService;

  @Operation(summary = "Запустить нагрузочный тест")
  @PostMapping("/run")
  public ResponseEntity<Map<String, Object>> runLoadTest(
      @RequestParam(defaultValue = "50") int threads,
      @RequestParam(defaultValue = "100") int requestsPerThread) throws InterruptedException {

    return ResponseEntity.ok(loadTestService.runLoadTest(threads, requestsPerThread));
  }

  @Operation(summary = "Получить статистику нагрузочного тестирования")
  @GetMapping("/stats")
  public ResponseEntity<Map<String, Object>> getStats() {
    return ResponseEntity.ok(loadTestService.getLoadTestStats());
  }

  @Operation(summary = "Сбросить статистику")
  @DeleteMapping("/stats")
  public ResponseEntity<String> resetStats() {
    loadTestService.resetStats();
    return ResponseEntity.ok("Статистика сброшена");
  }
}