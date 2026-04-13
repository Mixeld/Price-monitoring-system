package com.pricetracker.controller;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Categories", description = "Category management endpoints")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @Operation(summary = "Get all categories", description = "Returns a list of all categories")
  @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
  @GetMapping
  public ResponseEntity<List<CategoryDto>> getAllCategories() {
    return ResponseEntity.ok(categoryService.getAllCategories());
  }

  @Operation(summary = "Get category by ID", description = "Returns a single category by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category found",
          content = @Content(schema = @Schema(implementation = CategoryDto.class))),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CategoryDto> getCategoryById(
      @Parameter(description = "Category ID", example = "1", required = true)
      @PathVariable Long id) {
    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }

  @Operation(summary = "Get category by name", description = "Returns a single category by its name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category found",
          content = @Content(schema = @Schema(implementation = CategoryDto.class))),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @GetMapping("/name/{name}")
  public ResponseEntity<CategoryDto> getCategoryByName(
      @Parameter(description = "Category name", example = "Electronics", required = true)
      @PathVariable String name) {
    return ResponseEntity.ok(categoryService.getCategoryByName(name));
  }

  @Operation(summary = "Create a new category", description = "Creates a new category with the provided name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Category created successfully",
          content = @Content(schema = @Schema(implementation = CategoryDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Category with same name already exists")
  })
  @PostMapping
  public ResponseEntity<CategoryDto> createCategory(
      @Parameter(description = "Category data", required = true)
      @Valid @RequestBody CategoryDto categoryDto) {
    CategoryDto created = categoryService.createCategory(categoryDto);
    return ResponseEntity
        .created(URI.create("/api/categories/" + created.id()))
        .body(created);
  }

  @Operation(summary = "Update a category", description = "Updates an existing category")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category updated successfully",
          content = @Content(schema = @Schema(implementation = CategoryDto.class))),
      @ApiResponse(responseCode = "404", description = "Category not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Category with new name already exists")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CategoryDto> updateCategory(
      @Parameter(description = "Category ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Updated category data", required = true)
      @Valid @RequestBody CategoryDto categoryDto) {
    return ResponseEntity.ok(categoryService.updateCategory(id, categoryDto));
  }

  @Operation(summary = "Delete a category", description = "Deletes a category by its ID (only if no products are associated)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Category not found"),
      @ApiResponse(responseCode = "409", description = "Cannot delete category with associated products")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(
      @Parameter(description = "Category ID", example = "1", required = true)
      @PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}