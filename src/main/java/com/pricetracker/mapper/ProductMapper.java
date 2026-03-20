package com.pricetracker.mapper;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import org.springframework.stereotype.Component;

@Component
public final class ProductMapper {


  public ProductDto toDto(final Product product) {
    if (product == null) {
      return null;
    }

    String categoryName = null;
    if (product.getCategory() != null) {
      categoryName = product.getCategory().getName();
    }

    return new ProductDto(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getCurrentPrice(),
        categoryName
    );
  }

  public Product toEntity(final ProductDto dto) {
    if (dto == null) {
      return null;
    }

    Product product = new Product();
    product.setId(dto.id());
    product.setName(dto.name());
    product.setDescription(dto.description());
    product.setCurrentPrice(dto.currentPrice());

    return product;
  }
}