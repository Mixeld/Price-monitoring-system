package com.pricetracker.controller;

import com.pricetracker.dto.PriceHistoryDto;
import com.pricetracker.service.PriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Price History", description = "Price history management endpoints")
@RestController
@RequestMapping("/api/price-history")
@RequiredArgsConstructor
public class PriceHistoryController {

  private final PriceHistoryService service;

  @Operation(summary = "Record a price", description = "Records a new price for a product")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Price recorded successfully",
          content = @Content(schema = @Schema(implementation = PriceHistoryDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "404", description = "Product or store not found")
  })
  @PostMapping
  public PriceHistoryDto recordPrice(
      @Parameter(description = "Price history data", required = true)
      @Valid @RequestBody PriceHistoryDto dto) {
    return service.recordPrice(dto);
  }

  @Operation(summary = "Get price history for a product", description = "Returns price history for a product with optional optimization")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Price history retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found")
  })
  @GetMapping("/product/{productId}")
  public List<PriceHistoryDto> getHistory(
      @Parameter(description = "Product ID", example = "1", required = true)
      @PathVariable final Long productId,
      @Parameter(description = "Use optimized query with EntityGraph", example = "true")
      @RequestParam(defaultValue = "true") boolean optimized) {

    if (optimized) {
      return service.getHistoryOptimized(productId);
    } else {
      return service.getHistoryWithNPlusOne(productId);
    }
  }
}