package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с таблицей товаров.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Найти товары по категории.
   *
   * @param category название категории
   * @return список найденных товаров
   */
  List<Product> findByCategory(String category);
}


