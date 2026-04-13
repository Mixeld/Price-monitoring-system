package com.pricetracker.controller;

import com.pricetracker.dto.StoreDto;
import com.pricetracker.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Stores", description = "Store management endpoints")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

  private final StoreService storeService;

  @Operation(summary = "Get all stores", description = "Returns a list of all stores")
  @ApiResponse(responseCode = "200", description = "Stores retrieved successfully")
  @GetMapping
  public ResponseEntity<List<StoreDto>> getAllStores() {
    return ResponseEntity.ok(storeService.getAllStores());
  }

  @Operation(summary = "Get store by ID", description = "Returns a single store by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Store found",
          content = @Content(schema = @Schema(implementation = StoreDto.class))),
      @ApiResponse(responseCode = "404", description = "Store not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<StoreDto> getStoreById(
      @Parameter(description = "Store ID", example = "1", required = true)
      @PathVariable Long id) {
    return ResponseEntity.ok(storeService.getStoreById(id));
  }

  @Operation(summary = "Get store by name", description = "Returns a single store by its name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Store found",
          content = @Content(schema = @Schema(implementation = StoreDto.class))),
      @ApiResponse(responseCode = "404", description = "Store not found")
  })
  @GetMapping("/name/{name}")
  public ResponseEntity<StoreDto> getStoreByName(
      @Parameter(description = "Store name", example = "Amazon", required = true)
      @PathVariable String name) {
    return ResponseEntity.ok(storeService.getStoreByName(name));
  }

  @Operation(summary = "Search stores by name", description = "Search stores by name pattern (case-insensitive)")
  @ApiResponse(responseCode = "200", description = "Search completed successfully")
  @GetMapping("/search")
  public ResponseEntity<List<StoreDto>> searchStores(
      @Parameter(description = "Search query (name pattern)", example = "Amazon", required = true)
      @RequestParam String query) {
    return ResponseEntity.ok(storeService.searchStoresByName(query));
  }

  @Operation(summary = "Create a new store", description = "Creates a new store with the provided data")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Store created successfully",
          content = @Content(schema = @Schema(implementation = StoreDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Store with same name or URL already exists")
  })
  @PostMapping
  public ResponseEntity<StoreDto> createStore(
      @Parameter(description = "Store data", required = true)
      @Valid @RequestBody StoreDto storeDto) {
    StoreDto created = storeService.createStore(storeDto);
    return ResponseEntity
        .created(URI.create("/api/stores/" + created.id()))
        .body(created);
  }

  @Operation(summary = "Update a store", description = "Updates an existing store")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Store updated successfully",
          content = @Content(schema = @Schema(implementation = StoreDto.class))),
      @ApiResponse(responseCode = "404", description = "Store not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Store with new name or URL already exists")
  })
  @PutMapping("/{id}")
  public ResponseEntity<StoreDto> updateStore(
      @Parameter(description = "Store ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Updated store data", required = true)
      @Valid @RequestBody StoreDto storeDto) {
    return ResponseEntity.ok(storeService.updateStore(id, storeDto));
  }

  @Operation(summary = "Delete a store", description = "Deletes a store by its ID (only if no price history records exist)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Store deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Store not found"),
      @ApiResponse(responseCode = "409", description = "Cannot delete store with associated price history")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStore(
      @Parameter(description = "Store ID", example = "1", required = true)
      @PathVariable Long id) {
    storeService.deleteStore(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get price history count", description = "Returns the number of price history records for a store")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Store not found")
  })
  @GetMapping("/{id}/history/count")
  public ResponseEntity<Map<String, Object>> getPriceHistoryCount(
      @Parameter(description = "Store ID", example = "1", required = true)
      @PathVariable Long id) {
    long count = storeService.getPriceHistoryCount(id);
    StoreDto store = storeService.getStoreById(id);

    Map<String, Object> response = new HashMap<>();
    response.put("storeId", id);
    response.put("storeName", store.name());
    response.put("priceHistoryCount", count);

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Check store existence", description = "Checks if a store exists by name or website URL")
  @ApiResponse(responseCode = "200", description = "Check completed successfully")
  @GetMapping("/exists")
  public ResponseEntity<Map<String, Boolean>> checkStoreExists(
      @Parameter(description = "Store name to check", example = "Amazon")
      @RequestParam(required = false) String name,
      @Parameter(description = "Website URL to check", example = "https://www.amazon.com")
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