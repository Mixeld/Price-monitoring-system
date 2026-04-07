package com.pricetracker.controller;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
    return ResponseEntity.ok(productService.getProductById(id));
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> getProducts(
      @RequestParam(required = false) String category) {
    return ResponseEntity.ok(productService.getProducts(category));
  }

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) {
    ProductDto created = productService.saveProduct(productDto);
    return ResponseEntity
        .created(URI.create("/api/products/" + created.id()))
        .body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody ProductDto productDto) {
    return ResponseEntity.ok(productService.updateProduct(id, productDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  public Page<ProductDto> searchProducts(
      @RequestParam(required = false) final String category,
      @RequestParam(required = false) final BigDecimal minPrice,
      @RequestParam(required = false) final BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") final int page,
      @RequestParam(defaultValue = "10") final int size,
      @RequestParam(defaultValue = "id,asc") final String sort,
      @RequestParam(defaultValue = "false") final boolean useNative
  ) {
    Sort sortOrder = parseSortParameter(sort);
    Pageable pageable = PageRequest.of(page, size, sortOrder);

    return productService.searchProducts(category, minPrice, maxPrice, pageable, useNative);
  }

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