package com.pricetracker.service;

import com.pricetracker.entity.Category;
import com.pricetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class DemoService {

  private final CategoryRepository categoryRepository;

  public void saveWithoutTransaction() {
    Category cat = new Category();
    cat.setName("Trash Category (No Transaction)");
    categoryRepository.save(cat);

    throw new RuntimeException("Ошибка! Но транзакции нет, поэтому данные не откатятся.");
  }


  @Transactional
  public void saveWithTransaction() {
    Category cat = new Category();
    cat.setName("Clean Category (With Transaction)");
    categoryRepository.save(cat);

    throw new RuntimeException("Ошибка! Но транзакция есть, поэтому данные откатятся.");
  }
}