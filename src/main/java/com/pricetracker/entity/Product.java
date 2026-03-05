package com.pricetracker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность товара для сохранения в БД.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

  /**
   * Уникальный идентификатор товара.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Название товара.
   */
  private String name;

  /**
   * Описание товара.
   */
  private String description;

  private BigDecimal currentPrice;

  /**
   * Связь ManyToOne: Много товаров -> Одна категория. В базе данных будет колонка category_id.
   */
  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  /**
   * Связь OneToMany: Один товар -> Много записей истории цен. mappedBy указывает на поле 'product'
   * в классе PriceHistory.
   */
  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
  private List<PriceHistory> priceHistoryList = new ArrayList<>();

  /**
   * Связь ManyToMany: Товар отслеживают много пользователей. mappedBy указывает на поле
   * 'trackedProducts' в классе User.
   */
  @ManyToMany(mappedBy = "trackedProducts")
  private List<User> subscribedUsers = new ArrayList<>();
}