package com.pricetracker.controller;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.service.PriceHistoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

  private final PriceHistoryService service;

  @PostMapping
  public PriceHistoryDto recordPrice(@RequestBody final PriceHistoryDto dto) {
    return service.recordPrice(dto);
  }


  @GetMapping("/product/{productId}")
  public List<PriceHistoryDto> getHistory(
      @PathVariable final Long productId,
      @RequestParam(defaultValue = "true") boolean optimized) {

    if (optimized) {
      return service.getHistoryOptimized(productId);
    } else {
      return service.getHistoryWithNPlusOne(productId);
    }
  }
}