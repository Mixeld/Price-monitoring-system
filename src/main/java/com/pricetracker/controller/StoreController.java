package com.pricetracker.controller;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;

  @GetMapping
  public ResponseEntity<List<StoreDto>> getAllStores() {
    return ResponseEntity.ok(storeService.getAllStores());
  }

  @GetMapping("/{id}")
  public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id) {
    return ResponseEntity.ok(storeService.getStoreById(id));
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<StoreDto> getStoreByName(@PathVariable String name) {
    return ResponseEntity.ok(storeService.getStoreByName(name));
  }

  @GetMapping("/search")
  public ResponseEntity<List<StoreDto>> searchStores(@RequestParam String query) {
    return ResponseEntity.ok(storeService.searchStoresByName(query));
  }

  @PostMapping
  public ResponseEntity<StoreDto> createStore(@RequestBody StoreDto storeDto) {
    StoreDto created = storeService.createStore(storeDto);
    return ResponseEntity
        .created(URI.create("/api/stores/" + created.id()))
        .body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<StoreDto> updateStore(
      @PathVariable Long id,
      @RequestBody StoreDto storeDto) {
    return ResponseEntity.ok(storeService.updateStore(id, storeDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
    storeService.deleteStore(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/history/count")
  public ResponseEntity<Map<String, Object>> getPriceHistoryCount(@PathVariable Long id) {
    long count = storeService.getPriceHistoryCount(id);
    StoreDto store = storeService.getStoreById(id);

    Map<String, Object> response = new HashMap<>();
    response.put("storeId", id);
    response.put("storeName", store.name());
    response.put("priceHistoryCount", count);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/exists")
  public ResponseEntity<Map<String, Boolean>> checkStoreExists(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String websiteUrl) {

    Map<String, Boolean> response = new HashMap<>();

    if (name != null && !name.isBlank()) {
      response.put("nameExists", storeService.existsByName(name));
    }

    if (websiteUrl != null && !websiteUrl.isBlank()) {
      response.put("websiteUrlExists", storeService.existsByWebsiteUrl(websiteUrl));
    }

    return ResponseEntity.ok(response);
  }
}