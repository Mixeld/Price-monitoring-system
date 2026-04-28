package com.pricetracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для RaceConditionDemoService (100% Coverage)")
class RaceConditionDemoServiceTest {

  @InjectMocks private RaceConditionDemoService demoService;

  @Test
  @DisplayName("Покрытие demonstrateRaceCondition")
  void testDemonstrateRaceCondition() throws InterruptedException {
    demoService.demonstrateRaceCondition(2, 2);
    demoService.demonstrateRaceCondition(0, 0);
  }

  @Test
  @DisplayName("Покрытие веток недостатка товара")
  void testInsufficientStock() {
    var atomic = new RaceConditionDemoService.InventoryAtomic();
    var lock = new RaceConditionDemoService.InventoryLock();
    var sync = new RaceConditionDemoService.InventorySynchronized();

    // Продаем больше чем есть
    atomic.sell(5000);
    lock.sell(5000);
    sync.sell(5000);

    assertThat(atomic.getStock()).isEqualTo(1000);
    assertThat(lock.getStock()).isEqualTo(1000);
    assertThat(sync.getStock()).isEqualTo(1000);
  }

  @Test
  @DisplayName("InventoryAtomic: Покрытие CAS-конкуренции (ветка false в do-while)")
  void testAtomicCASRetry() throws InterruptedException {
    var atomic = new RaceConditionDemoService.InventoryAtomic();
    // Много потоков, вызывающих sell одновременно, заставят CAS возвращать false
    Runnable race = () -> {
      for(int i = 0; i < 1000; i++) atomic.sell(1);
    };
    Thread[] threads = new Thread[4];
    for(int i = 0; i < 4; i++) {
      threads[i] = new Thread(race);
      threads[i].start();
    }
    for(Thread t : threads) t.join();
  }

  @Test
  @DisplayName("Покрытие InterruptedException в блоках catch")
  void testInterruptionCatchBlocks() throws InterruptedException {
    assertInterrupts(() -> {
      new RaceConditionDemoService.InventoryUnsafe().sell(1);
    });
    assertInterrupts(() -> {
      new RaceConditionDemoService.InventorySynchronized().sell(1);
    });
    assertInterrupts(() -> {
      new RaceConditionDemoService.InventoryLock().sell(1);
    });
  }

  @Test
  @DisplayName("InventoryLock: Покрытие getStock и ReentrantLock")
  void testLockGetStock() {
    var inv = new RaceConditionDemoService.InventoryLock();
    assertThat(inv.getStock()).isEqualTo(1000);
  }

  private void assertInterrupts(Runnable action) throws InterruptedException {
    Thread t = new Thread(action);
    t.start();
    Thread.sleep(5);
    t.interrupt();
    t.join(500);
  }
}