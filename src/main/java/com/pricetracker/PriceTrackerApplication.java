package com.pricetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PriceTrackerApplication {

  public PriceTrackerApplication() {
  //КОММЕНТАРИЙ ДЛЯ СОНАРА
  }

  public static void main(final String[] args) {
    SpringApplication.run(PriceTrackerApplication.class, args);

  }
}
