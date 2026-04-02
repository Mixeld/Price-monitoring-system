package com.pricetracker.mapper;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.Category;
import com.pricetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

  private final CategoryRepository categoryRepository;

  public ProductDto toDto(final Product product) {
    if (product == null) {
      return null;
    }

    String categoryName = null;
    if (product.getCategory() != null) {
      categoryName = product.getCategory().getName();
    }

    // ВНИМАНИЕ: порядок параметров должен соответствовать record ProductDto
    // ProductDto(Long id, String name, BigDecimal price, String description, String category)
    return new ProductDto(
        product.getId(),           // id
        product.getName(),         // name
        product.getPrice(),        // price (BigDecimal)
        product.getDescription(),  // description
        categoryName               // category
    );
  }

  public Product toEntity(final ProductDto dto) {
    if (dto == null) {
      return null;
    }

    Product product = new Product();
    product.setId(dto.id());              // <-- dto.id(), не dto.getId()
    product.setName(dto.name());          // <-- dto.name(), не dto.getName()
    product.setDescription(dto.description());  // <-- dto.description()
    product.setPrice(dto.price());        // <-- dto.price() с маленькой буквы, НЕ dto.Price()

    // Устанавливаем категорию, если она указана
    if (dto.category() != null && !dto.category().isBlank()) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseThrow(() -> new RuntimeException("Category not found: " + dto.category()));
      product.setCategory(category);
    }

    return product;
  }

  public void updateEntity(final Product existing, final ProductDto dto) {
    if (dto == null || existing == null) {
      return;
    }

    if (dto.name() != null && !dto.name().isBlank()) {
      existing.setName(dto.name());
    }

    if (dto.price() != null) {
      existing.setPrice(dto.price());
    }

    if (dto.description() != null) {
      existing.setDescription(dto.description());
    }

    if (dto.category() != null && !dto.category().isBlank()) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseThrow(() -> new RuntimeException("Category not found: " + dto.category()));
      existing.setCategory(category);
    }
  }
}