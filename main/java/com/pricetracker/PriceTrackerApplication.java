package com.pricetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс запуска Spring Boot приложения.
 */
@SpringBootApplication
public class PriceTrackerApplication {

  /**
   * Конструктор по умолчанию. (Checkstyle требует Javadoc даже для публичных конструкторов).
   */
  public PriceTrackerApplication() {
    // Конструктор для Spring Context
  }

  /**
   * Точка входа в программу.
   *
   * @param args аргументы командной строки
   */
  public static void main(final String[] args) {
    SpringApplication.run(PriceTrackerApplication.class, args);
  }
}
