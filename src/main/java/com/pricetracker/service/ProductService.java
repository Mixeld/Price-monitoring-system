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
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  @Transactional(readOnly = true)
  public ProductDto getProductById(final Long id) {
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(
            "Product not found with id: " + id));
  }

  @Transactional(readOnly = true)
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

    // Используем dto.categoryName()
    if (dto.categoryName() != null) {
      Category category = categoryRepository.findByName(dto.categoryName())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.categoryName());
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

    product.setName(dto.name());
    product.setCurrentPrice(dto.currentPrice());
    product.setDescription(dto.description());

    if (dto.categoryName() != null) {
      Category category = categoryRepository.findByName(dto.categoryName())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.categoryName());
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

  @Transactional
  public void demoTransaction(final boolean triggerError) {
    Category cat = new Category();
    cat.setName("ROLLBACK_TEST_CATEGORY");
    categoryRepository.save(cat);

    if (triggerError) {
      throw new RuntimeException("ИСКУССТВЕННАЯ ОШИБКА ДЛЯ ТЕСТА ТРАНЗАКЦИИ");
    }
  }
}