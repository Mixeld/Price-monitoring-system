package com.pricetracker.mapper;

import com.pricetracker.dto.CategoryDto;
import com.pricetracker.entity.Category;

public class CategoryMapper {
  public CategoryDto toDto(Category category) {
    if (category == null) {
      return null;
    }
    return new CategoryDto(
        category.getId(),
        category.getName()
    );
  }

  public Category toEntity(CategoryDto dto){
    Category category = new Category();
    category.setName(dto.name());
    return category;
  }
}
