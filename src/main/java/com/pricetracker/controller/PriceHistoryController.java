package com.pricetracker.controller;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.service.PriceHistoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

  private final PriceHistoryService service;

  @PostMapping
  public PriceHistoryDto recordPrice(@Valid @RequestBody PriceHistoryDto dto) {
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