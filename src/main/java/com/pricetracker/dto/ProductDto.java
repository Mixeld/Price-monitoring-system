package com.pricetracker.dto;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String name,
    BigDecimal price,
    String description,
    String category
) {
  public static class Builder {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String category;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder price(BigDecimal price) {
      this.price = price;
      return this;
    }

    public Builder price(String price) {  // Перегрузка для String
      if (price != null) {
        this.price = new BigDecimal(price);
      }
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder category(String category) {
      this.category = category;
      return this;
    }

    public ProductDto build() {
      return new ProductDto(id, name, price, description, category);
    }
  }

  public static Builder builder() {
    return new Builder();
  }
}