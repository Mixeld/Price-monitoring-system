package com.pricetracker.controller;

import com.pricetracker.service.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "Race Condition Demo", description = "Демонстрация race condition и его решений")
@RestController
@RequestMapping("/api/demo/race-condition")
@RequiredArgsConstructor
public class RaceConditionController {

  private final RaceConditionDemoService demoService;

  @Operation(summary = "Демонстрация race condition с 50+ потоками")
  @PostMapping("/demonstrate")
  public ResponseEntity<Map<String, Object>> demonstrate(
      @RequestParam(defaultValue = "50") int threadsCount,
      @RequestParam(defaultValue = "100") int salesPerThread) throws InterruptedException {

    return ResponseEntity.ok(demoService.demonstrateRaceCondition(threadsCount, salesPerThread));
  }

  @Operation(summary = "Быстрая демонстрация (50 потоков, 20 продаж на поток)")
  @GetMapping("/quick-demo")
  public ResponseEntity<Map<String, Object>> quickDemo() throws InterruptedException {
    return ResponseEntity.ok(demoService.demonstrateRaceCondition(50, 20));
  }
}