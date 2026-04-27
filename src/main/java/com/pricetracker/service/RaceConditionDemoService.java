package com.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class RaceConditionDemoService {

  public static class InventoryUnsafe {
    private int stock = 1000;

    public void sell(int quantity) {
      if (stock >= quantity) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        stock -= quantity;
      }
    }

    public int getStock() {
      return stock;
    }
  }

  public static class InventorySynchronized {
    private int stock = 1000;

    public synchronized void sell(int quantity) {
      if (stock >= quantity) {
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        stock -= quantity;
      }
    }

    public synchronized int getStock() {
      return stock;
    }
  }

  public static class InventoryAtomic {
    private final AtomicInteger stock = new AtomicInteger(1000);

    public void sell(int quantity) {
      int currentStock;
      do {
        currentStock = stock.get();
        if (currentStock < quantity) {
          break;
        }
      } while (!stock.compareAndSet(currentStock, currentStock - quantity));
    }

    public int getStock() {
      return stock.get();
    }
  }

  public static class InventoryLock {
    private int stock = 1000;
    private final ReentrantLock lock = new ReentrantLock();

    public void sell(int quantity) {
      lock.lock();
      try {
        if (stock >= quantity) {
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          stock -= quantity;
        }
      } finally {
        lock.unlock();
      }
    }

    public int getStock() {
      lock.lock();
      try {
        return stock;
      } finally {
        lock.unlock();
      }
    }
  }

  public Map<String, Object> demonstrateRaceCondition(int threadsCount, int salesPerThread) throws InterruptedException {
    log.info("Демонстрация Race Condition с {} потоками", threadsCount);

    InventoryUnsafe unsafeInventory = new InventoryUnsafe();
    long startTime = System.currentTimeMillis();

    Thread[] unsafeThreads = new Thread[threadsCount];
    for (int i = 0; i < threadsCount; i++) {
      unsafeThreads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          for (int j = 0; j < salesPerThread; j++) {
            unsafeInventory.sell(1);
          }
        }
      });
      unsafeThreads[i].start();
    }

    for (int i = 0; i < threadsCount; i++) {
      unsafeThreads[i].join();
    }
    long unsafeTime = System.currentTimeMillis() - startTime;

    InventorySynchronized syncInventory = new InventorySynchronized();
    startTime = System.currentTimeMillis();

    Thread[] syncThreads = new Thread[threadsCount];
    for (int i = 0; i < threadsCount; i++) {
      syncThreads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          for (int j = 0; j < salesPerThread; j++) {
            syncInventory.sell(1);
          }
        }
      });
      syncThreads[i].start();
    }

    for (int i = 0; i < threadsCount; i++) {
      syncThreads[i].join();
    }
    long syncTime = System.currentTimeMillis() - startTime;

    InventoryAtomic atomicInventory = new InventoryAtomic();
    startTime = System.currentTimeMillis();

    Thread[] atomicThreads = new Thread[threadsCount];
    for (int i = 0; i < threadsCount; i++) {
      atomicThreads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          for (int j = 0; j < salesPerThread; j++) {
            atomicInventory.sell(1);
          }
        }
      });
      atomicThreads[i].start();
    }

    for (int i = 0; i < threadsCount; i++) {
      atomicThreads[i].join();
    }
    long atomicTime = System.currentTimeMillis() - startTime;

    int expectedStock = 1000 - (threadsCount * salesPerThread);
    int finalExpectedStock = Math.max(0, expectedStock);

    Map<String, Object> results = Map.of(
        "threadsCount", threadsCount,
        "salesPerThread", salesPerThread,
        "totalSales", threadsCount * salesPerThread,
        "expectedStock", finalExpectedStock,
        "unsafe", Map.of(
            "stock", unsafeInventory.getStock(),
            "timeMs", unsafeTime,
            "lostItems", finalExpectedStock - unsafeInventory.getStock()
        ),
        "synchronized", Map.of(
            "stock", syncInventory.getStock(),
            "timeMs", syncTime
        ),
        "atomic", Map.of(
            "stock", atomicInventory.getStock(),
            "timeMs", atomicTime
        )
    );

    log.error("UNSAFE - Остаток: {} (потеряно {} товаров)",
        unsafeInventory.getStock(),
        finalExpectedStock - unsafeInventory.getStock());
    log.info("SYNCHRONIZED - Остаток: {}", syncInventory.getStock());
    log.info("ATOMIC - Остаток: {}", atomicInventory.getStock());

    return results;
  }
}