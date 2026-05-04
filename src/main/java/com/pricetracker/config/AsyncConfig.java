package com.pricetracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "taskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);       // Сколько потоков работают всегда
    executor.setMaxPoolSize(20);      // Максимум потоков при нагрузке
    executor.setQueueCapacity(100);   // Очередь для задач
    executor.setThreadNamePrefix("AsyncTask-");
    executor.initialize();
    return executor;
  }
}