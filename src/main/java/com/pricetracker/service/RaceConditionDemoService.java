package com.pricetracker.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RaceConditionDemoService {

  private int unsafeCounter = 0;
  private int safeCounter = 0;

  public synchronized void incrementSafe() {
    safeCounter++;
  }

  public Map<String, Integer> runDemo(int threads) throws InterruptedException {
    unsafeCounter = 0;
    safeCounter = 0;

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch latch = new CountDownLatch(threads);

    for (int i = 0; i < threads; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < 1000; j++) {

            unsafeCounter++;

            incrementSafe();
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    return Map.of(
        "expected", threads * 1000,
        "actual_unsafe", unsafeCounter,
        "actual_safe", safeCounter
    );
  }
}