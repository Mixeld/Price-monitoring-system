package com.pricetracker.controller;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAllCategories() {
    return ResponseEntity.ok(categoryService.getAllCategories());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
    // Этот метод нужно добавить в CategoryService
    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<CategoryDto> getCategoryByName(@PathVariable String name) {
    // Этот метод нужно добавить в CategoryService
    return ResponseEntity.ok(categoryService.getCategoryByName(name));
  }

  @PostMapping
  public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
    CategoryDto created = categoryService.createCategory(categoryDto);
    return ResponseEntity
        .created(URI.create("/api/categories/" + created.id()))
        .body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> updateCategory(
      @PathVariable Long id,
      @RequestBody CategoryDto categoryDto) {
    // Этот метод нужно добавить в CategoryService
    return ResponseEntity.ok(categoryService.updateCategory(id, categoryDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}