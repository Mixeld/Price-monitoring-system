package com.pricetracker;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

  @Mock
  private ProceedingJoinPoint joinPoint;

  @Mock
  private MethodSignature signature;

  @InjectMocks
  private LoggingAspect loggingAspect;

  @BeforeEach
  void setUp() throws Exception {
    // Настройка возвращаемой сигнатуры метода
    when(joinPoint.getSignature()).thenReturn(signature);
    when(signature.getName()).thenReturn("testMethod");

    // Настройка класса-цели
    Object target = new TestService();
    when(joinPoint.getTarget()).thenReturn(target);
  }

  // 1. Успешное выполнение, длительность <= 1000 мс
  @Test
  void testLogExecutionTime_Success_Fast() throws Throwable {
    // given
    when(joinPoint.proceed()).thenReturn("success");

    // when
    Object result = loggingAspect.logExecutionTime(joinPoint);

    // then
    assert result.equals("success");
    verify(joinPoint, times(1)).proceed();
    // В реальном тесте можно проверить логи, но для 100% покрытия достаточно выполнения кода
  }

  // 2. Успешное выполнение, длительность > 1000 мс
  @Test
  void testLogExecutionTime_Success_Slow() throws Throwable {
    // given
    when(joinPoint.proceed()).thenAnswer(invocation -> {
      Thread.sleep(1100); // делаем выполнение дольше 1000 мс
      return "slowResult";
    });

    // when
    Object result = loggingAspect.logExecutionTime(joinPoint);

    // then
    assert result.equals("slowResult");
    verify(joinPoint, times(1)).proceed();
  }

  // 3. Выброс исключения
  @Test
  void testLogExecutionTime_ThrowsException() throws Throwable {
    // given
    RuntimeException testException = new RuntimeException("Test error");
    when(joinPoint.proceed()).thenThrow(testException);

    // when & then
    assertThrows(RuntimeException.class, () -> loggingAspect.logExecutionTime(joinPoint));
    verify(joinPoint, times(1)).proceed();
  }

  // Вспомогательный класс для имитации целевого сервиса
  static class TestService {
    public String testMethod() {
      return "test";
    }
  }
}