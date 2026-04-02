package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByCategoryName(String categoryName);

  Optional<Product> findByName(String name);

  @Query("SELECT p FROM Product p " +
      "WHERE (:category IS NULL OR p.category.name = :category) " +
      "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
      "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
  Page<Product> searchProductsJpql(
      @Param("category") String category,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable
  );

  // Задача 2: Аналогичный запрос через native query
  @Query(value = "SELECT p.* FROM products p " +
      "LEFT JOIN categories c ON p.category_id = c.id " +
      "WHERE (:category IS NULL OR c.name = :category) " +
      "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
      "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
      nativeQuery = true)
  Page<Product> searchProductsNative(
      @Param("category") String category,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable
  );
}