// exception/BusinessExceptionTest.java
package com.pricetracker.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

  @Test
  void shouldCreateBusinessExceptionWithMessageAndErrorCode() {
    // given
    String message = "Business rule violation";
    String errorCode = "BIZ_001";

    // when
    BusinessException exception = new BusinessException(message, errorCode);

    // then
    assertEquals(message, exception.getMessage());
    assertEquals(errorCode, exception.getErrorCode());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateBusinessExceptionWithMessageErrorCodeAndCause() {
    // given
    String message = "Business rule violation";
    String errorCode = "BIZ_002";
    Throwable cause = new RuntimeException("Original error");

    // when
    BusinessException exception = new BusinessException(message, errorCode, cause);

    // then
    assertEquals(message, exception.getMessage());
    assertEquals(errorCode, exception.getErrorCode());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void shouldInheritFromRuntimeException() {
    // given
    BusinessException exception = new BusinessException("test", "ERR");

    // then
    assertTrue(exception instanceof RuntimeException);
  }
}