package com.pricetracker.mapper;
import com.pricetracker.dto.ProductDto;
import com.pricetracker.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public ProductDto toDto(Product product) {
        if (product == null) return null;
        return new ProductDto(product.getId(), product.getName(), product.getCurrentPrice(), product.getCategory());
    }
    public Product toEntity(ProductDto dto) {
        if (dto == null) return null;
        Product product = new Product();
        product.setId(dto.id());
        product.setName(dto.name());
        product.setCurrentPrice(dto.price());
        product.setCategory(dto.category());
        return product;
    }
}
