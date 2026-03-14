package com.pricetracker.service;

import com.pricetracker.entity.Category;
import com.pricetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@RequiredArgsConstructor
public class DemoService {

  private final CategoryRepository categoryRepository;

  public void saveWithoutTransaction() {
    Category cat = new Category();
    cat.setName("Trash Category (No Transaction)");
    try {
      categoryRepository.save(cat);
    } catch (DataAccessException e) {
      throw new DataIntegrityViolationException("Не удалось сохранить категорию без транзакции", e);
    }
    throw new DataIntegrityViolationException("Ошибка! Но транзакции нет, поэтому данные не откатятся.");
  }

  @Transactional
  public void saveWithTransaction() {
    Category cat = new Category();
    cat.setName("Clean Category (With Transaction)");
    try {
      categoryRepository.save(cat);
    } catch (DataAccessException e) {
      throw new DataIntegrityViolationException("Не удалось сохранить категорию с транзакцией", e);
    }
    throw new DataIntegrityViolationException("Ошибка! Но транзакция есть, поэтому данные откатятся.");
  }
}
