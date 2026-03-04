package com.pricetracker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность категории товаров.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

  /**
   * Уникальный идентификатор категории.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Название категории (например, "Электроника").
   */
  private String name;

  /**
   * Список товаров в этой категории. mappedBy = "category" означает, что связь управляется полем
   * 'category' в классе Product.
   */
  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
  private List<Product> products = new ArrayList<>();
}