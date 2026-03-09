package com.pricetracker.mapper;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import org.springframework.stereotype.Component;

@Component
public final class ProductMapper {

  public ProductDto toDto(final Product product) {
    if (product == null) {
      return null;
    }

    // --- ИСПРАВЛЕНИЕ: Получаем имя категории из объекта Category ---
    String categoryName = null;
    if (product.getCategory() != null) {
      categoryName = product.getCategory().getName();
    }

    return new ProductDto(
        product.getId(),
        product.getName(),
        product.getCurrentPrice(), // Если поле currentPrice есть в Product
        categoryName               // Передаем строку
    );
  }


  public Product toEntity(final ProductDto dto) {
    if (dto == null) {
      return null;
    }
    Product product = new Product();
    product.setId(dto.id());
    product.setName(dto.name());
    product.setCurrentPrice(dto.price());

    // (Это временное решение, лучше искать категорию в БД по ID или имени)
    if (dto.category() != null) {
      Category category = new Category();
      category.setName(dto.category());
      product.setCategory(category);
    }

    return product;
  }
}