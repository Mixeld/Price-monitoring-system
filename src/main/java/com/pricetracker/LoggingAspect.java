package com.pricetracker;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Around("execution(* com.pricetracker.service.*.*(..))")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();

    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();

    log.debug("Starting {}.{}", className, methodName);

    try {
      Object result = joinPoint.proceed();
      long duration = System.currentTimeMillis() - startTime;

      if (duration > 1000) {
        log.warn("{}.{} executed in {} ms (SLOW!)", className, methodName, duration);
      } else {
        log.debug("{}.{} executed in {} ms", className, methodName, duration);
      }

      return result;
    } catch (Exception e) {
      long duration = System.currentTimeMillis() - startTime;
      log.error("{}.{} failed after {} ms: {}", className, methodName, duration, e.getMessage());
      throw e;
    }
  }
}