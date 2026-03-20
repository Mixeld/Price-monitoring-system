package com.pricetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PriceTrackerApplication {

  public PriceTrackerApplication() {

  }

  public static void main(final String[] args) {
    var context = SpringApplication.run(PriceTrackerApplication.class, args);

    System.out.println("====== СПИСОК КОНТРОЛЛЕРОВ ======");
    String[] beanNames = context.getBeanNamesForAnnotation(org.springframework.web.bind.annotation.RestController.class);
    for (String beanName : beanNames) {
      System.out.println(beanName);
    }
    System.out.println("=================================");
  }
}
