package com.pricetracker.controller;

import com.pricetracker.entity.PriceHistory;
import com.pricetracker.entity.Store;
import com.pricetracker.repository.PriceHistoryRepository;
import com.pricetracker.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
      } else {
        log.info("Запрос #{}: магазин не указан для записи истории #{}",
            i + 2, history.getId());
      }
    }

    log.info("=".repeat(70));
    log.info("ИТОГО: 1 (история) + {} (магазины) = {} запросов к БД",
        histories.size(), histories.size() + 1);
    log.info("=".repeat(70));

    return histories;
  }

  @GetMapping("/nplus1/stores/{storeId}/histories")
  public Store getStoreWithHistoriesNPlus1(@PathVariable Long storeId) {
    log.info("\n" + "=".repeat(70));
    log.info("ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1: Store -> PriceHistory (через репозиторий)");
    log.info("=".repeat(70));

    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new RuntimeException("Store not found"));
    log.info("1-й запрос: получили магазин '{}'", store.getName());

    List<PriceHistory> histories = priceHistoryRepository.findByStoreIdOrderByDateRecordedDesc(storeId);
    log.info("2-й запрос: получили {} записей истории для магазина", histories.size());

    log.info("=".repeat(70));
    log.info("ИТОГО: минимум 2 запроса (магазин + история)");
    log.info("=".repeat(70));

    return store;
  }

  @GetMapping("/nplus1/all-stores-with-histories")
  public List<Store> getAllStoresWithHistoriesNPlus1() {
    log.info("\n" + "=".repeat(70));
    log.info("ДЕМОНСТРАЦИЯ ПРОБЛЕМЫ N+1: Все Store -> PriceHistory");
    log.info("=".repeat(70));

    List<Store> stores = storeRepository.findAll();
    log.info("1-й запрос: получили {} магазинов", stores.size());

    int totalQueries = 1;
    for (int i = 0; i < stores.size(); i++) {
      Store store = stores.get(i);

      List<PriceHistory> histories = priceHistoryRepository
          .findByStoreIdOrderByDateRecordedDesc(store.getId());

      log.info("Запрос #{}: магазин '{}' имеет {} записей истории",
          i + 2, store.getName(), histories.size());
      totalQueries++;
    }

    log.info("=".repeat(70));
    log.info("ИТОГО: 1 (все магазины) + {} (история каждого) = {} запросов к БД",
        stores.size(), totalQueries);
    log.info("=".repeat(70));

    return stores;
  }

  @GetMapping("/fixed/all-stores-with-histories")
  public List<Store> getAllStoresWithHistoriesFixed() {
    log.info("\n" + "=".repeat(70));
    log.info("РЕШЕНИЕ: EntityGraph для Store -> PriceHistory");
    log.info("=".repeat(70));

    List<Store> stores = storeRepository.findAll();

    log.info("Единственный запрос с JOIN: получили {} магазинов с историей цен", stores.size());

    for (Store store : stores) {
      int historyCount = store.getPriceHistories() != null
          ? store.getPriceHistories().size()
          : 0;
      log.info("Магазин '{}' (история уже загружена: {} записей)",
          store.getName(), historyCount);
    }

    log.info("=".repeat(70));
    log.info("ИТОГО: всего 1 запрос к БД!");
    log.info("=".repeat(70));

    return stores;
  }

  @GetMapping("/fixed/stores")
  public List<PriceHistory> getPriceHistoryFixed() {
    log.info("\n" + "=".repeat(70));
    log.info("РЕШЕНИЕ ПРОБЛЕМЫ N+1: PriceHistory -> Store с EntityGraph");
    log.info("=".repeat(70));

    // Теперь этот метод будет использовать EntityGraph
    List<PriceHistory> histories = priceHistoryRepository.findAll();
    log.info("1 запрос с JOIN: получили {} записей истории цен с магазинами", histories.size());

    for (int i = 0; i < histories.size(); i++) {
      PriceHistory history = histories.get(i);
      Store store = history.getStore();

      if (store != null) {
        log.info("Магазин '{}' для записи истории #{} (уже загружен)",
            store.getName(), history.getId());
      }
    }

    log.info("=".repeat(70));
    log.info("ИТОГО: всего 1 запрос к БД!");
    log.info("=".repeat(70));

    return histories;
  }
}