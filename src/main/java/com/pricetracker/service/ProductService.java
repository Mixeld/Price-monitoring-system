package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public final class ProductService {


  private final ProductRepository productRepository;

  private final CategoryRepository categoryRepository;

  private final ProductMapper productMapper;


  public ProductDto getProductById(final Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "Product not found with id: " + id));
  }

  public List<ProductDto> getProducts(final String categoryName) {
    if (categoryName != null && !categoryName.isBlank()) {
      return productRepository.findByCategoryName(categoryName)
          .stream()
          .map(productMapper::toDto)
          .toList();
    }
    return productRepository.findAll().stream()
        .map(productMapper::toDto)
        .toList();
  }

  @Transactional
  public ProductDto saveProduct(final ProductDto dto) {
    Product product = productMapper.toEntity(dto);

    if (dto.category() != null) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.category());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    }

    Product savedProduct = productRepository.save(product);
    return productMapper.toDto(savedProduct);
  }

  @Transactional
  public ProductDto updateProduct(final Long id, final ProductDto dto) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Product not found with id: " + id));

    // Обновляем простые поля
    product.setName(dto.name());
    product.setCurrentPrice(dto.price());

    // Обновляем категорию (ищем или создаем)
    if (dto.category() != null) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.category());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    }

    Product updatedProduct = productRepository.save(product);
    return productMapper.toDto(updatedProduct);
  }

  @Transactional
  public void deleteProduct(final Long id) {
    if (!productRepository.existsById(id)) {
      throw new EntityNotFoundException(
          "Cannot delete. Product not found with id: " + id);
    }
    productRepository.deleteById(id);
  }
}