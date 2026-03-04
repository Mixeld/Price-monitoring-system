package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository; // <--- ДОБАВИЛИ
  private final ProductMapper productMapper;

  public ProductDto getProductById(Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new RuntimeException("Product not found"));
  }

  public List<ProductDto> getProducts(String categoryName) {
    // Если категория передана, ищем товары по ней
    if (categoryName != null && !categoryName.isBlank()) {
      return productRepository.findByCategoryName(categoryName) // <--- НУЖНО ДОБАВИТЬ В РЕПОЗИТОРИЙ
          .stream()
          .map(productMapper::toDto)
          .toList();
    }
    // Иначе возвращаем всё
    return productRepository.findAll().stream()
        .map(productMapper::toDto)
        .toList();
  }

  @Transactional // Чтобы создание категории и товара было одной операцией
  public ProductDto saveProduct(ProductDto dto) {
    Product product = productMapper.toEntity(dto);

    // --- ЛОГИКА С КАТЕГОРИЯМИ ---
    if (dto.category() != null) {
      // Ищем категорию в базе по имени
      Category category = categoryRepository.findByName(dto.category())
          .orElseGet(() -> {
            // Если не нашли - создаем новую
            Category newCat = new Category();
            newCat.setName(dto.category());
            return categoryRepository.save(newCat);
          });

      // Привязываем найденную/созданную категорию к товару
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    return productMapper.toDto(savedProduct);
  }
}