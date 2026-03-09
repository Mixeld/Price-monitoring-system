package com.pricetracker.repository;

import com.pricetracker.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для доступа к таблице категорий.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  /**
   * Найти категорию по названию. Используется для проверки, существует ли такая категория перед
   * созданием товара.
   *
   * @param name Название категории
   * @return Optional (может быть найдено, а может нет)
   */
  Optional<Category> findByName(String name);
}