package com.pricetracker.controller;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Tag(name = "Transaction Demo", description = "Demonstrates @Transactional behavior")
@RestController
@RequestMapping("/api/demo/transactions")
@RequiredArgsConstructor
public class TransactionDemoController {

  private final ProductService productService;

  @Operation(summary = "Test bulk insert WITH transaction",
      description = "All products are saved or none if error occurs")
  @PostMapping("/with-transaction")
  public ResponseEntity<String> testWithTransaction() {
    BulkProductCreateDto bulkDto = createTestBulkData();

    try {
      List<ProductDto> results = productService.createProductsBulk(bulkDto);
      return ResponseEntity.ok(String.format(
          "SUCCESS WITH TRANSACTION: Created %d products. All saved or none!",
          results.size()
      ));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          "TRANSACTION ROLLBACK: Error occurred, no products were saved. Error: " + e.getMessage()
      );
    }
  }

  @Operation(summary = "Test bulk insert WITHOUT transaction",
      description = "Partial saves possible - some products may be saved before error")
  @PostMapping("/without-transaction")
  public ResponseEntity<String> testWithoutTransaction() {
    BulkProductCreateDto bulkDto = createTestBulkData();

    try {
      List<ProductDto> results = productService.createProductsBulkWithoutTransaction(bulkDto);
      return ResponseEntity.ok(String.format(
          "WITHOUT TRANSACTION: Created %d products BUT some may be partially saved!",
          results.size()
      ));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(
          "PARTIAL SAVE: Some products may have been saved before the error! Error: " + e.getMessage()
      );
    }
  }

  private BulkProductCreateDto createTestBulkData() {
    return new BulkProductCreateDto(List.of(
        new ProductDto(null, "Product 1", new BigDecimal("100.00"), "First product", "Electronics"),
        new ProductDto(null, "Product 2", new BigDecimal("200.00"), "Second product", "Electronics"),
        // Этот продукт вызовет ошибку (имя слишком короткое)
        new ProductDto(null, "P3", new BigDecimal("300.00"), "Invalid name", "Electronics"),
        new ProductDto(null, "Product 4", new BigDecimal("400.00"), "Fourth product", "Electronics")
    ));
  }
}