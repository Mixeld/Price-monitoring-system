package com.pricetracker.controller;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
public final class ProductController {

  private final ProductService productService;

  @GetMapping("/{id}")
  public ProductDto getProductById(@PathVariable final Long id) {
    return productService.getProductById(id);
  }


  @GetMapping
  public List<ProductDto> getProducts(
      @RequestParam(required = false) final String category) {
    return productService.getProducts(category);
  }


  @PostMapping
  public ProductDto createProduct(
      @RequestBody final ProductDto productDto) {
    return productService.saveProduct(productDto);
  }


  @PutMapping("/{id}")
  public ProductDto updateProduct(
      @PathVariable final Long id,
      @RequestBody final ProductDto productDto) {
    return productService.updateProduct(id, productDto);
  }


  @DeleteMapping("/{id}")
  public void deleteProduct(@PathVariable final Long id) {
    productService.deleteProduct(id);
  }
}