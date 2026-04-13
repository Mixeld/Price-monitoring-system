package com.pricetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "Product Data Transfer Object")
public record ProductDto(
    @Schema(description = "Product ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    Long id,

    @Schema(description = "Product name", example = "iPhone 15 Pro", required = true)
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    String name,

    @Schema(description = "Current product price", example = "999.99", required = true)
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,

    @Schema(description = "Product description", example = "Latest smartphone with A17 Pro chip")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,

    @Schema(description = "Category name", example = "Electronics")
    String category
) {
  public static class Builder {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String category;

    public Builder id(Long id) { this.id = id; return this; }
    public Builder name(String name) { this.name = name; return this; }
    public Builder price(BigDecimal price) { this.price = price; return this; }
    public Builder price(String price) {
      if (price != null) this.price = new BigDecimal(price);
      return this;
    }
    public Builder description(String description) { this.description = description; return this; }
    public Builder category(String category) { this.category = category; return this; }
    public ProductDto build() {
      return new ProductDto(id, name, price, description, category);
    }
  }
  public static Builder builder() { return new Builder(); }
}