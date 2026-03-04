package com.pricetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность пользователя приложения.
 */
@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

  /**
   * Уникальный идентификатор пользователя.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * Имя пользователя (логин).
   */
  private String username;

  /**
   * Электронная почта.
   */
  private String email;

  /**
   * Список отслеживаемых товаров. Связь ManyToMany: один юзер может следить за многими товарами.
   */
  @ManyToMany
  @JoinTable(
      name = "user_subscriptions",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "product_id")
  )
  private List<Product> trackedProducts = new ArrayList<>();
}