package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByCategoryName(String categoryName);

  @Override
  @EntityGraph(attributePaths = {"category"})
  List<Product> findAll();
}