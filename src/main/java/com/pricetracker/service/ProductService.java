package com.pricetracker.service;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import com.pricetracker.mapper.ProductMapper;
import com.pricetracker.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductDto getProductById(Long id) {
        return productRepository.findById(id).map(productMapper::toDto).orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }
    public List<ProductDto> getProducts(String category) {
        List<Product> products = (category != null && !category.isBlank()) ? productRepository.findByCategory(category) : productRepository.findAll();
        return products.stream().map(productMapper::toDto).collect(Collectors.toList());
    }
    public ProductDto saveProduct(ProductDto dto) {
        Product product = productMapper.toEntity(dto);
        return productMapper.toDto(productRepository.save(product));
    }
}
