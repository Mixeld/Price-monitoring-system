package com.pricetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id; // <--- ДОБАВЛЕНО
import lombok.Data;

@Entity
@Data
public class Store {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String websiteUrl; // Исправил опечатку (было 'wedsiteUrl')
}