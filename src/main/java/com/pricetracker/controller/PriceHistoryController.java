package com.pricetracker.controller;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.dto.PriceStatsDto;
import com.pricetracker.service.PriceHistoryService;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

  private final PriceHistoryService priceHistoryService;


  @GetMapping("/{id}")
  public ResponseEntity<PriceHistoryDto> getHistoryById(@PathVariable Long id) {
    log.info("REST request to get price history by id: {}", id);
    PriceHistoryDto historyDto = priceHistoryService.getHistoryById(id);
    return ResponseEntity.ok(historyDto);
  }


  @GetMapping("/product/{productId}")
  public ResponseEntity<List<PriceHistoryDto>> getHistoryForProduct(@PathVariable Long productId) {
    log.info("REST request to get price history for product id: {}", productId);
    List<PriceHistoryDto> historyList = priceHistoryService.getHistoryForProduct(productId);
    return ResponseEntity.ok(historyList);
  }


  @GetMapping("/product/{productId}/range")
  public ResponseEntity<List<PriceHistoryDto>> getHistoryForProductInDateRange(
      @PathVariable Long productId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

    log.info("REST request to get price history for product id: {} from {} to {}", productId, from,
        to);

    if (from.isAfter(to)) {
      return ResponseEntity.badRequest().build();
    }

    List<PriceHistoryDto> historyList = priceHistoryService.getHistoryForProductInDateRange(
        productId, from, to);
    return ResponseEntity.ok(historyList);
  }


  @GetMapping("/product/{productId}/latest")
  public ResponseEntity<PriceHistoryDto> getLatestPrice(@PathVariable Long productId) {
    log.info("REST request to get latest price for product id: {}", productId);
    PriceHistoryDto latestPrice = priceHistoryService.getLatestPrice(productId);
    return ResponseEntity.ok(latestPrice);
  }


  @GetMapping("/product/{productId}/stats")
  public ResponseEntity<PriceStatsDto> getPriceStats(
      @PathVariable Long productId,
      @RequestParam(defaultValue = "30") int days) {

    log.info("REST request to get price stats for product id: {} for last {} days", productId,
        days);

    if (days <= 0 || days > 365) {
      return ResponseEntity.badRequest().build();
    }

    PriceStatsDto stats = priceHistoryService.getPriceStats(productId, days);
    return ResponseEntity.ok(stats);
  }


  @GetMapping("/store/{storeId}")
  public ResponseEntity<List<PriceHistoryDto>> getHistoryForStore(@PathVariable Long storeId) {
    log.info("REST request to get price history for store id: {}", storeId);
    List<PriceHistoryDto> historyList = priceHistoryService.getHistoryForStore(storeId);
    return ResponseEntity.ok(historyList);
  }


  @PostMapping
  public ResponseEntity<PriceHistoryDto> recordPrice(@RequestBody PriceHistoryDto priceHistoryDto) {
    log.info("REST request to record new price: {}", priceHistoryDto);

    // Базовая проверка входных данных
    if (priceHistoryDto.productId() == null) {
      return ResponseEntity.badRequest().build();
    }

    PriceHistoryDto recorded = priceHistoryService.recordPrice(priceHistoryDto);

    return ResponseEntity
        .created(URI.create("/api/price-history/" + recorded.id()))
        .body(recorded);
  }


  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteHistoryRecord(@PathVariable Long id) {
    log.info("REST request to delete price history with id: {}", id);
    priceHistoryService.deleteHistoryRecord(id);
    return ResponseEntity.noContent().build();
  }


  @DeleteMapping("/product/{productId}")
  public ResponseEntity<Void> deleteHistoryForProduct(@PathVariable Long productId) {
    log.info("REST request to delete all price history for product id: {}", productId);
    priceHistoryService.deleteHistoryForProduct(productId);
    return ResponseEntity.noContent().build();
  }


  @GetMapping("/product/{productId}/count")
  public ResponseEntity<Long> countHistoryForProduct(@PathVariable Long productId) {
    log.info("REST request to count price history for product id: {}", productId);
    long count = priceHistoryService.countHistoryForProduct(productId);
    return ResponseEntity.ok(count);
  }


  @GetMapping("/product/{productId}/min")
  public ResponseEntity<PriceHistoryDto> getMinPrice(
      @PathVariable Long productId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

    log.info("REST request to get min price for product id: {}", productId);

    if (from == null) {
      from = LocalDateTime.now().minusYears(1);
    }
    if (to == null) {
      to = LocalDateTime.now();
    }

    PriceHistoryDto minPrice = priceHistoryService.getMinPrice(productId, from, to);
    return ResponseEntity.ok(minPrice);
  }


  @GetMapping("/product/{productId}/max")
  public ResponseEntity<PriceHistoryDto> getMaxPrice(
      @PathVariable Long productId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

    log.info("REST request to get max price for product id: {}", productId);

    if (from == null) {
      from = LocalDateTime.now().minusYears(1);
    }
    if (to == null) {
      to = LocalDateTime.now();
    }

    PriceHistoryDto maxPrice = priceHistoryService.getMaxPrice(productId, from, to);
    return ResponseEntity.ok(maxPrice);
  }


  @GetMapping("/product/{productId}/avg")
  public ResponseEntity<Double> getAveragePrice(
      @PathVariable Long productId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

    log.info("REST request to get average price for product id: {}", productId);

    if (from == null) {
      from = LocalDateTime.now().minusYears(1);
    }
    if (to == null) {
      to = LocalDateTime.now();
    }

    Double avgPrice = priceHistoryService.getAveragePrice(productId, from, to);
    return ResponseEntity.ok(avgPrice);
  }


  @GetMapping("/product/{productId}/exists")
  public ResponseEntity<Boolean> existsForProduct(@PathVariable Long productId) {
    log.info("REST request to check if price history exists for product id: {}", productId);
    boolean exists = priceHistoryService.existsForProduct(productId);
    return ResponseEntity.ok(exists);
  }
}