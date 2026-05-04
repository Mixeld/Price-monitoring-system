package com.pricetracker.controller;

import com.pricetracker.service.RaceConditionDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Race Condition & Load Test", description = "Демонстрация проблем многопоточности")
public class PriceTrackerController {

  private final RaceConditionDemoService raceConditionDemoService;

  @Operation(summary = "Продемонстрировать Race Condition (50+ потоков)")
  @PostMapping("/race-condition/run")
  public Map<String, Integer> runRaceDemo(
      @Parameter(description = "Количество потоков", example = "100")
      @RequestParam(defaultValue = "100") int threads) throws InterruptedException {

    // 3. Запуск демонстрации Race Condition
    return raceConditionDemoService.runDemo(threads);
  }

  @Operation(summary = "Endpoint для нагрузочного тестирования JMeter")
  @GetMapping("/load-test/ping")
  public Map<String, String> ping() {
    // 4. Используется JMeter для замера пропускной способности этого метода
    return Map.of(
        "status", "UP",
        "timestamp", String.valueOf(System.currentTimeMillis())
    );
  }
}