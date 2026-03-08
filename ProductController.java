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

/**
 * REST контроллер для управления товарами. Предоставляет API endpoints для CRUD операций.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public final class ProductController {

  /**
   * Сервис для работы с бизнес-логикой товаров.
   */
  private final ProductService productService;

  /**
   * GET запрос для получения товара по ID.
   *
   * @param id идентификатор товара из пути
   * @return найденный товар
   */
  @GetMapping("/{id}")
  public ProductDto getProductById(@PathVariable final Long id) {
    return productService.getProductById(id);
  }

  /**
   * GET запрос для получения списка товаров. Поддерживает фильтрацию по категории.
   *
   * @param category категория (опционально)
   * @return список товаров
   */
  @GetMapping
  public List<ProductDto> getProducts(
      @RequestParam(required = false) final String category) {
    return productService.getProducts(category);
  }

  /**
   * POST запрос для создания нового товара.
   *
   * @param productDto тело запроса с данными
   * @return созданный товар
   */
  @PostMapping
  public ProductDto createProduct(
      @RequestBody final ProductDto productDto) {
    return productService.saveProduct(productDto);
  }

  /**
   * PUT запрос для обновления существующего товара.
   *
   * @param id         идентификатор обновляемого товара
   * @param productDto новые данные товара
   * @return обновленный товар
   */
  @PutMapping("/{id}")
  public ProductDto updateProduct(
      @PathVariable final Long id,
      @RequestBody final ProductDto productDto) {
    return productService.updateProduct(id, productDto);
  }

  /**
   * DELETE запрос для удаления товара.
   *
   * @param id идентификатор удаляемого товара
   */
  @DeleteMapping("/{id}")
  public void deleteProduct(@PathVariable final Long id) {
    productService.deleteProduct(id);
  }
}