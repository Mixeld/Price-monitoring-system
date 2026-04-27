package com.pricetracker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Async-Task-");
    executor.setRejectedExecutionHandler((r, executor1) ->
        log.warn("Задача отклонена: очередь заполнена"));
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(60);
    executor.initialize();

    log.info("Асинхронный Executor инициализирован: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
        executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
    return executor;
  }
}