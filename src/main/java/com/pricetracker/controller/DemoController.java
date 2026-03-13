package com.pricetracker.controller;

import com.pricetracker.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

  private final DemoService demoService;

  @PostMapping("/transaction")
  public String testTransaction(@RequestParam boolean useTransaction) {
    try {
      if (useTransaction) {
        demoService.saveWithTransaction();
      } else {
        demoService.saveWithoutTransaction();
      }
    } catch (RuntimeException e) {
      return "ПОЙМАНА ОШИБКА: " + e.getMessage();
    }
    return "Успех (не должно случиться)";
  }
}