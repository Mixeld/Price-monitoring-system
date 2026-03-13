package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Category;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.CategoryRepository;
import com.pricetracker.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  @Transactional(readOnly = true)
  public ProductDto getProductById(Long id) {
    log.debug("Fetching product by id: {}", id);
    return productRepository.findById(id)
        .map(productMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<ProductDto> getProducts(String categoryName) {
    log.debug("Fetching products with category: {}", categoryName);

    if (categoryName != null && !categoryName.isBlank()) {
      return productRepository.findByCategoryName(categoryName)
          .stream()
          .map(productMapper::toDto)
          .collect(Collectors.toList());
    }

    return productRepository.findAll().stream()
        .map(productMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public ProductDto saveProduct(ProductDto dto) {
    log.debug("Saving new product: {}", dto.name());

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
    log.info("Product saved successfully with id: {}", savedProduct.getId());

    return productMapper.toDto(savedProduct);
  }

  @Transactional
  public ProductDto updateProduct(Long id, ProductDto dto) {
    log.debug("Updating product with id: {}", id);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

    product.setName(dto.name());
    product.setCurrentPrice(dto.price());

    if (dto.category() != null) {
      Category category = categoryRepository.findByName(dto.category())
          .orElseGet(() -> {
            Category newCat = new Category();
            newCat.setName(dto.category());
            return categoryRepository.save(newCat);
          });
      product.setCategory(category);
    } else {
      product.setCategory(null);
    }

    log.info("Product updated successfully with id: {}", id);
    return productMapper.toDto(product);
  }

  @Transactional
  public void deleteProduct(Long id) {
    log.debug("Deleting product with id: {}", id);

    if (!productRepository.existsById(id)) {
      throw new EntityNotFoundException("Product not found with id: " + id);
    }

    productRepository.deleteById(id);
    log.info("Product deleted successfully with id: {}", id);
  }
}