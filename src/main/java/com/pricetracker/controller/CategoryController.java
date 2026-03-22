package com.pricetracker.controller;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.service.CategoryService;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<CategoryDto> getCategoryByName(@PathVariable String name) {
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

    return ResponseEntity.ok(categoryService.updateCategory(id, categoryDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}