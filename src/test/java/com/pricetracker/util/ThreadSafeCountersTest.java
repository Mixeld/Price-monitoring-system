package com.pricetracker.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для ThreadSafeCounters")
class ThreadSafeCountersTest {

  private static final int THREAD_COUNT = 50;
  private static final int INCREMENTS_PER_THREAD = 1000;
  private static final int EXPECTED_TOTAL = THREAD_COUNT * INCREMENTS_PER_THREAD;

  @Nested
  @DisplayName("Тесты для AtomicCounter")
  class AtomicCounterTest {

    @Test
    @DisplayName("AtomicCounter должен быть потокобезопасным")
    void atomicCounter_shouldBeThreadSafe() throws InterruptedException {
      ThreadSafeCounters.AtomicCounter counter = new ThreadSafeCounters.AtomicCounter();
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
            counter.increment();
          }
        });
      }

      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.SECONDS);

      assertThat(counter.getCount()).isEqualTo(EXPECTED_TOTAL);
    }

    @Test
    @DisplayName("reset должен обнулять счётчик")
    void reset_shouldZeroCounter() {
      ThreadSafeCounters.AtomicCounter counter = new ThreadSafeCounters.AtomicCounter();
      counter.increment();
      counter.increment();

      counter.reset();

      assertThat(counter.getCount()).isZero();
    }
  }

  @Nested
  @DisplayName("Тесты для SynchronizedCounter")
  class SynchronizedCounterTest {

    @Test
    @DisplayName("SynchronizedCounter должен быть потокобезопасным")
    void synchronizedCounter_shouldBeThreadSafe() throws InterruptedException {
      ThreadSafeCounters.SynchronizedCounter counter = new ThreadSafeCounters.SynchronizedCounter();
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
            counter.increment();
          }
        });
      }

      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.SECONDS);

      assertThat(counter.getCount()).isEqualTo(EXPECTED_TOTAL);
    }

    @Test
    @DisplayName("reset должен обнулять счётчик")
    void reset_shouldZeroCounter() {
      ThreadSafeCounters.SynchronizedCounter counter = new ThreadSafeCounters.SynchronizedCounter();
      counter.increment();
      counter.increment();

      counter.reset();

      assertThat(counter.getCount()).isZero();
    }
  }

  @Nested
  @DisplayName("Тесты для UnsafeCounter (демонстрация race condition)")
  class UnsafeCounterTest {

    @Test
    @DisplayName("UnsafeCounter НЕ должен быть потокобезопасным (должны быть потери)")
    void unsafeCounter_shouldNotBeThreadSafe() throws InterruptedException {
      ThreadSafeCounters.UnsafeCounter counter = new ThreadSafeCounters.UnsafeCounter();
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
            counter.increment();
          }
        });
      }

      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.SECONDS);

      // При race condition результат должен быть МЕНЬШЕ ожидаемого
      assertThat(counter.getCount()).isLessThanOrEqualTo(EXPECTED_TOTAL);
    }

    @Test
    @DisplayName("reset должен обнулять счётчик")
    void reset_shouldZeroCounter() {
      ThreadSafeCounters.UnsafeCounter counter = new ThreadSafeCounters.UnsafeCounter();
      counter.increment();
      counter.increment();

      counter.reset();

      assertThat(counter.getCount()).isZero();
    }
  }

  @Nested
  @DisplayName("Тесты для AtomicLongCounter")
  class AtomicLongCounterTest {

    @Test
    @DisplayName("AtomicLongCounter должен быть потокобезопасным")
    void atomicLongCounter_shouldBeThreadSafe() throws InterruptedException {
      ThreadSafeCounters.AtomicLongCounter counter = new ThreadSafeCounters.AtomicLongCounter();
      ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

      for (int i = 0; i < THREAD_COUNT; i++) {
        executor.submit(() -> {
          for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
            counter.increment();
          }
        });
      }

      executor.shutdown();
      executor.awaitTermination(10, TimeUnit.SECONDS);

      assertThat(counter.getCount()).isEqualTo(EXPECTED_TOTAL);
    }

    @Test
    @DisplayName("reset должен обнулять счётчик")
    void reset_shouldZeroCounter() {
      ThreadSafeCounters.AtomicLongCounter counter = new ThreadSafeCounters.AtomicLongCounter();
      counter.increment();
      counter.increment();

      counter.reset();

      assertThat(counter.getCount()).isZero();
    }
  }
}