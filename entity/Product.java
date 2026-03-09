package com.pricetracker.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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


@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;


  private String name;

  private BigDecimal currentPrice;


  private String description;


  @ManyToOne
  @JoinColumn(name = "category_id")
  private Category category;

  @OneToMany(
      mappedBy = "product",
      cascade = CascadeType.ALL,
      fetch = FetchType.LAZY
  )
  private List<PriceHistory> priceHistoryList = new ArrayList<>();


  @ManyToMany(mappedBy = "trackedProducts", fetch = FetchType.LAZY)
  private List<User> subscribedUsers = new ArrayList<>();
}