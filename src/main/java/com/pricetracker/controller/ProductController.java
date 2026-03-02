package com.pricetracker.controller;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public ProductDto getProductById(@PathVariable Long id) { return productService.getProductById(id); }

    @GetMapping
    public List<ProductDto> getProducts(@RequestParam(required = false) String category) { return productService.getProducts(category); }

    @PostMapping
    public ProductDto createProduct(@RequestBody ProductDto productDto) { return productService.saveProduct(productDto); }
}
