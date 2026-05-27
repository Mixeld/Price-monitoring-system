package com.pricetracker.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test-log")
public class TestLogController {

  @GetMapping("/400")
  public String test400() {
    log.error("ТЕСТ: Сейчас будет выброшено IllegalArgumentException (400)");
    throw new IllegalArgumentException("Тестовая 400 ошибка - проверка логирования");
  }

  @GetMapping("/500")
  public String test500() {
    log.error("ТЕСТ: Сейчас будет выброшено RuntimeException (500)");
    throw new RuntimeException("Тестовая 500 ошибка - проверка логирования");
  }
}