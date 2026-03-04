package com.pricetracker.mapper;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import org.springframework.stereotype.Component;

/**
 * Компонент для конвертации между Entity и DTO. Класс является final, так как не предполагает
 * наследования.
 */
@Component
public final class ProductMapper {

  /**
   * Преобразует сущность в DTO.
   *
   * @param product сущность товара
   * @return DTO товара или null
   */
  public ProductDto toDto(final Product product) {
    if (product == null) {
      return null;
    }
    return new ProductDto(
        product.getId(),
        product.getName(),
        product.getCurrentPrice(),
        product.getCategory()
    );
  }

  /**
   * Преобразует DTO в сущность.
   *
   * @param dto DTO товара
   * @return сущность товара или null
   */
  public Product toEntity(final ProductDto dto) {
    if (dto == null) {
      return null;
    }
    Product product = new Product();
    product.setId(dto.id());
    product.setName(dto.name());
    product.setCurrentPrice(dto.price());
    product.setCategory(dto.category());
    return product;
  }
}


