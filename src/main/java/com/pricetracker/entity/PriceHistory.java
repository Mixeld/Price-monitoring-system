package com.pricetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import lombok.Data;

@Entity
@Data
public class PriceHistory {
  @Id
  @GeneratedValue (strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToMany
  @JoinColumn (name = "product_id")
  private Product product;

  @ManyToMany
  @JoinColumn (name = "store_id")
  private Store strore;
}
