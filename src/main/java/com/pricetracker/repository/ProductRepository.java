package com.pricetracker.repository;

import com.pricetracker.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с товарами.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  /**
   * Найти товары, у которых категория имеет указанное имя. Spring Data JPA: Product -> Category ->
   * name.
   *
   * @param categoryName название категории (например, "Electronics")
   * @return список найденных товаров
   */
  List<Product> findByCategoryName(String categoryName);
}