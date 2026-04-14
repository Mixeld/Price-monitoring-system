package com.pricetracker.controller;

import com.pricetracker.dto.BulkProductCreateDto;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Products", description = "Product management endpoints")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @Operation(summary = "Get product by ID", description = "Returns a single product by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product found",
          content = @Content(schema = @Schema(implementation = ProductDto.class))),
      @ApiResponse(responseCode = "404", description = "Product not found",
          content = @Content)
  })
  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(
      @Parameter(description = "Product ID", example = "1", required = true)
      @PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @Operation(summary = "Get all products", description = "Returns a list of all products, optionally filtered by category")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid category parameter")
  })
  @GetMapping
  public ResponseEntity<List<ProductDto>> getProducts(
      @Parameter(description = "Category name filter", example = "Electronics")
      @RequestParam(required = false) String category) {
    return ResponseEntity.ok(productService.getProducts(category));
  }

  @Operation(summary = "Create a new product", description = "Creates a new product with the provided data")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Product created successfully",
          content = @Content(schema = @Schema(implementation = ProductDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Product with same name already exists")
  })
  @PostMapping
  public ResponseEntity<ProductDto> createProduct(
      @Parameter(description = "Product data", required = true)
      @Valid @RequestBody ProductDto productDto) {
    ProductDto created = productService.saveProduct(productDto);
    return ResponseEntity
        .created(URI.create("/api/products/" + created.id()))
        .body(created);
  }

  @Operation(summary = "Bulk create products", description = "Creates multiple products in one request with transaction support")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Products created successfully",
          content = @Content(schema = @Schema(implementation = ProductDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "500", description = "Transaction failed - all products rolled back")
  })
  @PostMapping("/bulk")
  public ResponseEntity<List<ProductDto>> createProductsBulk(
      @Parameter(description = "List of products to create", required = true)
      @Valid @RequestBody BulkProductCreateDto bulkDto,
      @Parameter(description = "Use transaction (default: true). Set to false to see partial commits", example = "true")
      @RequestParam(defaultValue = "true") boolean useTransaction
  ) {
    List<ProductDto> created;

    if (useTransaction) {
      log.info("Creating {} products WITH transaction", bulkDto.products().size());
      created = productService.createProductsBulk(bulkDto);
    } else {
      log.warn("Creating {} products WITHOUT transaction - this may cause partial commits!",
          bulkDto.products().size());
      created = productService.createProductsBulkWithoutTransaction(bulkDto);
    }

    return ResponseEntity.ok(created);
  }

  @Operation(summary = "Update an existing product", description = "Updates a product with the provided data")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product updated successfully",
          content = @Content(schema = @Schema(implementation = ProductDto.class))),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PutMapping("/{id}")
  public ResponseEntity<ProductDto> updateProduct(
      @Parameter(description = "Product ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Updated product data", required = true)
      @Valid @RequestBody ProductDto productDto) {
    return ResponseEntity.ok(productService.updateProduct(id, productDto));
  }

  @Operation(summary = "Delete a product", description = "Deletes a product by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Product not found"),
      @ApiResponse(responseCode = "409", description = "Cannot delete product with existing references")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(
      @Parameter(description = "Product ID", example = "1", required = true)
      @PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Search products", description = "Search products with filters, pagination and sorting. " +
      "Demonstrates caching and N+1 problem solutions.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Search completed successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid search parameters")
  })
  @GetMapping("/search")
  public Page<ProductDto> searchProducts(
      @Parameter(description = "Category name filter", example = "Electronics")
      @RequestParam(required = false) final String category,

      @Parameter(description = "Minimum price filter", example = "100.00")
      @RequestParam(required = false) final BigDecimal minPrice,

      @Parameter(description = "Maximum price filter", example = "1000.00")
      @RequestParam(required = false) final BigDecimal maxPrice,

      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") final int page,

      @Parameter(description = "Page size", example = "10")
      @RequestParam(defaultValue = "10") final int size,

      @Parameter(description = "Sort parameter format: field,asc|desc (can be multiple: field1,asc,field2,desc)",
          example = "price,desc")
      @RequestParam(defaultValue = "id,asc") final String sort,

      @Parameter(description = "Use native SQL query instead of JPQL (for performance comparison)",
          example = "false")
      @RequestParam(defaultValue = "false") final boolean useNative
  ) {
    Sort sortOrder = parseSortParameter(sort);
    Pageable pageable = PageRequest.of(page, size, sortOrder);

    return productService.searchProducts(category, minPrice, maxPrice, pageable, useNative);
  }

  @Operation(summary = "Get cache statistics", description = "Returns current search cache size and keys")
  @ApiResponse(responseCode = "200", description = "Cache stats retrieved successfully")
  @GetMapping("/cache/stats")
  public ResponseEntity<java.util.Map<String, Object>> getCacheStats() {
    return ResponseEntity.ok(productService.getCacheStats());
  }

  @Operation(summary = "Clear search cache", description = "Invalidates all cached search results")
  @ApiResponse(responseCode = "200", description = "Cache cleared successfully")
  @DeleteMapping("/cache")
  public ResponseEntity<String> clearCache() {
    productService.clearCache();
    return ResponseEntity.ok("Cache cleared successfully");
  }

  /**
   * Parses sort parameter from string format: "field,asc" or "field1,asc,field2,desc"
   *
   * @param sort sort string parameter
   * @return Sort object for pagination
   */
  private Sort parseSortParameter(String sort) {
    if (sort == null || sort.isBlank()) {
      return Sort.by("id").ascending();
    }

    String[] parts = sort.split(",");
    if (parts.length == 1) {
      return Sort.by(parts[0]).ascending();
    }

    List<Sort.Order> orders = new ArrayList<>();
    for (int i = 0; i < parts.length; i += 2) {
      String field = parts[i];
      String direction = (i + 1 < parts.length) ? parts[i + 1] : "asc";

      Sort.Order order = direction.equalsIgnoreCase("desc")
          ? Sort.Order.desc(field)
          : Sort.Order.asc(field);
      orders.add(order);
    }

    return Sort.by(orders);
  }
}