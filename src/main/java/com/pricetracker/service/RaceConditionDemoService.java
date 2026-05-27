package com.pricetracker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class RaceConditionDemoService {

  // ВАЖНО: Используем один экзекутор, а не создаем новый на каждый чих
  private final ExecutorService demoExecutor = Executors.newFixedThreadPool(100);

  public Map<String, Object> demonstrateRaceCondition(int threadsCount, int salesPerThread) {
    // Ограничим количество потоков для защиты от падения сервера
    int safeThreads = Math.min(threadsCount, 50);

    AtomicInteger safeInventory = new AtomicInteger(safeThreads * salesPerThread);
    // Обычная переменная для демонстрации Race Condition
    final int[] unsafeInventory = {safeThreads * salesPerThread};

    CountDownLatch latch = new CountDownLatch(safeThreads);

    for (int i = 0; i < safeThreads; i++) {
      demoExecutor.submit(() -> {
        try {
          for (int j = 0; j < salesPerThread; j++) {
            // Race condition происходит здесь
            unsafeInventory[0]--;
            // Безопасная операция
            safeInventory.decrementAndGet();
          }
        } finally {
          latch.countDown();
        }
      });
    }

    try {
      // Ждем завершения не более 5 секунд
      boolean completed = latch.await(5, TimeUnit.SECONDS);

      Map<String, Object> result = new HashMap<>();
      result.put("status", completed ? "Success" : "Timeout");
      result.put("expected", 0);
      result.put("unsafeResult", unsafeInventory[0]);
      result.put("safeResult", safeInventory.get());
      result.put("isRaceConditionDetected", unsafeInventory[0] != 0);

      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Тест был прерван");
    }
  }
}