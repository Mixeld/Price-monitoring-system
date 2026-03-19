package com.pricetracker.controller;

import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Store;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/debug/price-history")
@RequiredArgsConstructor
public class DebugPriceHistoryController {

  private final PriceHistoryRepository priceHistoryRepository;
  private final StoreRepository storeRepository;

  @GetMapping("/nplus1/stores")
  public List<PriceHistory> getPriceHistoryWithNPlus1() {
    log.info("\n" + "=".repeat(70));
    log.info("ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1: PriceHistory -> Store");
    log.info("=".repeat(70));

    List<PriceHistory> histories = priceHistoryRepository.findAll();
    log.info("1-й запрос: получили {} записей истории цен", histories.size());

    for (int i = 0; i < histories.size(); i++) {
      PriceHistory history = histories.get(i);
      Store store = history.getStore();

      if (store != null) {
        log.info("Запрос #{}: загружаем магазин '{}' для записи истории #{}",
            i + 2, store.getName(), history.getId());
      }
    }

    log.info("=".repeat(70));
    log.info("ИТОГО: 1 (история) + {} (магазины) = {} запросов к БД",
        histories.size(), histories.size() + 1);
    log.info("=".repeat(70));

    return histories;
  }

  @GetMapping("/nplus1/all-stores")
  public List<Store> getAllStoresNPlus1() {
    log.info("\n" + "=".repeat(70));
    log.info("ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1: Все Store");
    log.info("=".repeat(70));

    List<Store> stores = storeRepository.findAll();
    log.info("1-й запрос: получили {} магазинов", stores.size());

    log.info("=".repeat(70));
    log.info("ИТОГО: всего 1 запрос");
    log.info("=".repeat(70));

    return stores;
  }
}