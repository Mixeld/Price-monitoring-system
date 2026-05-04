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
    log.info("Запуск нагрузочного тестирования: {} потоков, {} запросов на поток", threads, requestsPerThread);

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
              productService.getProducts(null);
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

              boolean taskFinished = false;
              int retries = 0;

              while (retries < 30 && !taskFinished) {
                Map<String, Object> taskInfo = asyncTaskService.getTaskInfo(taskId);
                String status = (String) taskInfo.get("status");

                if (AsyncTaskService.STATUS_COMPLETED.equals(status)) {
                  successfulRequests.incrementAndGet();
                  taskFinished = true;
                } else if (AsyncTaskService.STATUS_FAILED.equals(status)) {
                  failedRequests.incrementAndGet();
                  errorCounter.increment();
                  taskFinished = true;
                } else {
                  Thread.sleep(100); // Опрос статуса
                  retries++;
                }
              }

              if (!taskFinished) {
                log.warn("Задача {} не завершилась по таймауту", taskId);
                failedRequests.incrementAndGet();
              }
            }
          } catch (Exception e) {
            failedRequests.incrementAndGet();
            errorCounter.increment();
            log.error("Ошибка в потоке {}: {}", threadId, e.getMessage());
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

    // Формируем отчет
    return Map.of(
        "testConfig", Map.of(
            "threads", threads,
            "requestsPerThread", requestsPerThread,
            "totalRequests", totalRequests
        ),
        "results", Map.of(
            "successful", successfulRequests.get(),
            "failed", failedRequests.get(),
            "successRate", String.format("%.2f%%", (totalRequests > 0 ? successfulRequests.get() * 100.0 / totalRequests : 0)),
            "totalTimeMs", duration,
            "throughput", String.format("%.2f req/sec", (duration > 0 ? totalRequests * 1000.0 / duration : 0)),
            "avgResponseTimeMs", (totalRequests > 0 ? (double) duration / totalRequests : 0)
        ),
        "timestamp", LocalDateTime.now().toString()
    );
  }

  public Map<String, Object> getLoadTestStats() {
    long total = requestCounter.getCount();
    long errors = errorCounter.getCount();
    return Map.of(
        "totalRequests", total,
        "errors", errors,
        "errorRate", String.format("%.2f%%", total > 0 ? (errors * 100.0 / total) : 0)
    );
  }

  public void resetStats() {
    requestCounter.reset();
    errorCounter.reset();
  }
}