package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query("SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.name = :categoryName")
  List<Product> findByCategoryNameWithFetch(String categoryName);

  @EntityGraph(attributePaths = {"category"})
  List<Product> findByCategoryName(String categoryName);

  @Override
  @EntityGraph(attributePaths = {"category"})
  List<Product> findAll();
}