// exception/DuplicateResourceExceptionTest.java
package com.pricetracker.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateResourceExceptionTest {

  @Test
  void shouldCreateExceptionWithEntityFieldAndValue() {
    // given
    String entityName = "User";
    String field = "email";
    Object value = "john@example.com";

    // when
    DuplicateResourceException exception =
        new DuplicateResourceException(entityName, field, value);

    // then
    String expectedMessage = "User with email 'john@example.com' already exists";
    assertEquals(expectedMessage, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithCustomMessage() {
    // given
    String customMessage = "Custom duplicate error message";

    // when
    DuplicateResourceException exception = new DuplicateResourceException(customMessage);

    // then
    assertEquals(customMessage, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithMessageAndCause() {
    // given
    String message = "Duplicate resource";
    Throwable cause = new IllegalStateException("Database constraint violation");

    // when
    DuplicateResourceException exception = new DuplicateResourceException(message, cause);

    // then
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void shouldHandleDifferentValueTypes() {
    // when
    DuplicateResourceException exception1 =
        new DuplicateResourceException("Product", "id", 123L);
    DuplicateResourceException exception2 =
        new DuplicateResourceException("Order", "status", "PENDING");

    // then
    assertEquals("Product with id '123' already exists", exception1.getMessage());
    assertEquals("Order with status 'PENDING' already exists", exception2.getMessage());
  }
}