// exception/ResourceNotFoundExceptionTest.java
package com.pricetracker.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

  @Test
  void shouldCreateExceptionWithEntityFieldAndValue() {
    // given
    String entityName = "Product";
    String field = "id";
    Object value = 999L;

    // when
    ResourceNotFoundException exception =
        new ResourceNotFoundException(entityName, field, value);

    // then
    String expectedMessage = "Product not found with id: '999'";
    assertEquals(expectedMessage, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithCustomMessage() {
    // given
    String customMessage = "Resource with custom criteria not found";

    // when
    ResourceNotFoundException exception = new ResourceNotFoundException(customMessage);

    // then
    assertEquals(customMessage, exception.getMessage());
    assertNull(exception.getCause());
  }

  @Test
  void shouldCreateExceptionWithMessageAndCause() {
    // given
    String message = "User not found";
    Throwable cause = new IllegalArgumentException("Invalid user ID format");

    // when
    ResourceNotFoundException exception = new ResourceNotFoundException(message, cause);

    // then
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void shouldHandleDifferentFieldTypes() {
    // when
    ResourceNotFoundException exception1 =
        new ResourceNotFoundException("Order", "orderNumber", "ORD-123");
    ResourceNotFoundException exception2 =
        new ResourceNotFoundException("Category", "active", true);

    // then
    assertEquals("Order not found with orderNumber: 'ORD-123'", exception1.getMessage());
    assertEquals("Category not found with active: 'true'", exception2.getMessage());
  }
}