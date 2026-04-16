package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByCategoryName(String categoryName);
  
  boolean existsByName(String name);

  @Query(value = "SELECT * FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
      "WHERE (:category IS NULL OR c.name = :category) " +
      "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
      "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
      countQuery = "SELECT count(*) FROM products p LEFT JOIN categories c ON p.category_id = c.id " +
          "WHERE (:category IS NULL OR c.name = :category) " +
          "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
          "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
      nativeQuery = true)
  Page<Product> searchProductsNative(@Param("category") String category,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable);


  @Query("SELECT p FROM Product p LEFT JOIN p.category c " +
      "WHERE (:category IS NULL OR c.name = :category) " +
      "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
      "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
  Page<Product> searchProductsJpql(@Param("category") String category,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable);
}