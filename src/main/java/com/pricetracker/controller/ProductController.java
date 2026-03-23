package com.pricetracker.controller;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
    ProductDto created = productService.saveProduct(productDto);
    return ResponseEntity
        .created(URI.create("/api/products/" + created.id()))
        .body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDto> updateProduct(
      @PathVariable Long id,
      @RequestBody ProductDto productDto) {
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
      @RequestParam(defaultValue = "false") final boolean useNative
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
    return productService.searchProducts(category, minPrice, maxPrice, pageable, useNative);
  }
}