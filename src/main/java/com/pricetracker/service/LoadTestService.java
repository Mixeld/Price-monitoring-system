package com.pricetracker.service;

import com.pricetracker.dto.ProductDto;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.util.ThreadSafeCounters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadTestService {

  private final ProductService productService;
  private final ProductRepository productRepository;
  private final AsyncTaskService asyncTaskService;
  private final ThreadSafeCounters.AtomicCounter requestCounter = new ThreadSafeCounters.AtomicCounter();
  private final ThreadSafeCounters.AtomicCounter errorCounter = new ThreadSafeCounters.AtomicCounter();

  public Map<String, Object> runLoadTest(int threads, int requestsPerThread) throws InterruptedException {
    log.info("Запуск нагрузочного тестирования");
    log.info("Потоков: {}, Запросов на поток: {}", threads, requestsPerThread);

    ExecutorService executor = Executors.newFixedThreadPool(threads);
    CountDownLatch latch = new CountDownLatch(threads);
    AtomicLong totalTime = new AtomicLong(0);
    AtomicInteger successfulRequests = new AtomicInteger(0);
    AtomicInteger failedRequests = new AtomicInteger(0);

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < threads; i++) {
      final int threadId = i;
      executor.submit(() -> {
        long threadStartTime = System.nanoTime();

        for (int j = 0; j < requestsPerThread; j++) {
          try {
            requestCounter.increment();

            if (j % 3 == 0) {
              var products = productService.getProducts(null);
              successfulRequests.incrementAndGet();
            } else if (j % 3 == 1) {
              ProductDto dto = new ProductDto(
                  null,
                  "LoadTest_" + threadId + "_" + j,
                  new BigDecimal("100." + (j % 99)),
                  "Load test product",
                  "Electronics"
              );
              productService.saveProduct(dto);
              successfulRequests.incrementAndGet();
            } else {
              String taskId = asyncTaskService.startDataExport("product", 1L);
              int retries = 0;
              while (retries < 30) {
                String status = asyncTaskService.getTaskStatus(taskId);
                if (AsyncTaskService.STATUS_COMPLETED.equals(status)) {
                  successfulRequests.incrementAndGet();
                  break;
                } else if (AsyncTaskService.STATUS_FAILED.equals(status)) {
                  failedRequests.incrementAndGet();
                  errorCounter.increment();
                  break;
                }
                Thread.sleep(100);
                retries++;
              }
            }
          } catch (Exception e) {
            failedRequests.incrementAndGet();
            errorCounter.increment();
            log.error("Ошибка в нагрузочном тесте", e);
          }
        }

        totalTime.addAndGet(System.nanoTime() - threadStartTime);
        latch.countDown();
      });
    }

    latch.await();
    long endTime = System.currentTimeMillis();
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);

    long duration = endTime - startTime;
    int totalRequests = successfulRequests.get() + failedRequests.get();

    Map<String, Object> results = Map.of(
        "testConfig", Map.of(
            "threads", threads,
            "requestsPerThread", requestsPerThread,
            "totalRequests", totalRequests
        ),
        "results", Map.of(
            "successful", successfulRequests.get(),
            "failed", failedRequests.get(),
            "successRate", String.format("%.2f%%", (successfulRequests.get() * 100.0 / totalRequests)),
            "totalTimeMs", duration,
            "throughput", String.format("%.2f req/sec", (totalRequests * 1000.0 / duration)),
            "avgResponseTimeMs", duration * 1000.0 / totalRequests
        ),
        "timestamp", LocalDateTime.now().toString()
    );

    log.info("Результаты нагрузочного тестирования");
    log.info("Всего запросов: {}", totalRequests);
    log.info("Успешных: {}", successfulRequests.get());
    log.info("Проваленных: {}", failedRequests.get());
    log.info("Throughput: {} req/sec", (totalRequests * 1000.0 / duration));

    return results;
  }

  public Map<String, Object> getLoadTestStats() {
    return Map.of(
        "totalRequests", requestCounter.getCount(),
        "errors", errorCounter.getCount(),
        "errorRate", String.format("%.2f%%", requestCounter.getCount() > 0 ?
            (errorCounter.getCount() * 100.0 / requestCounter.getCount()) : 0)
    );
  }

  public void resetStats() {
    requestCounter.reset();
    errorCounter.reset();
  }
}