// exception/CannotDeleteExceptionTest.java
package com.pricetracker.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CannotDeleteExceptionTest {

  @Test
  void shouldCreateExceptionWithMessage() {
    // given
    String message = "Cannot delete user with active orders";

    // when
    CannotDeleteException exception = new CannotDeleteException(message);

    // then
    assertEquals(message, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithMessageAndCause() {
    // given
    String message = "Cannot delete resource";
    Throwable cause = new RuntimeException("Foreign key constraint violation");

    // when
    CannotDeleteException exception = new CannotDeleteException(message, cause);

    // then
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void shouldInheritFromRuntimeException() {
    // given
    CannotDeleteException exception = new CannotDeleteException("test");

    // then
    assertTrue(exception instanceof RuntimeException);
  }
}