package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByCategoryName(String categoryName);

  @Override
  @EntityGraph(attributePaths = {"category"})
  List<Product> findAll();

  @Query("""
        SELECT p FROM Product p
        JOIN p.category c
        WHERE (:categoryName IS NULL OR c.name = :categoryName)
        AND (:minPrice IS NULL OR p.currentPrice >= :minPrice)
        AND (:maxPrice IS NULL OR p.currentPrice <= :maxPrice)
    """)
  @EntityGraph(attributePaths = {"category"})
  Page<Product> searchProductsJpql (
      @Param("categoryName") String categoryName,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable
  );

  @Query(value = """
     SELECT p.* FROM products p
     LEFT JOIN categories c ON p.category_id = c.id
     WHERE (:categoryName IS NULL OR c.name = :categoryName)
     AND (:minPrice IS NULL OR p.current_price >= :minPrice)
     AND (:maxPrice IS NULL OR p.current_price <= :maxPrice)
     """,
    countQuery = """
        SELECT count(*) FROM products p
        LEFT JOIN categories c ON p.category_id = c.id
        WHERE (:categoryName IS NULL OR c.name = :categoryName)
        AND (:minPrice IS NULL OR p.current_price >= :minPrice)
        AND (:maxPrice IS NULL OR p.current_price <= :maxPrice)
    """,
    nativeQuery = true)
  Page<Product> searchProductsNative(
      @Param("categoryName") String categoryName,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable
  );
}