package com.pricetracker.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerCoverageTest {

  private GlobalExceptionHandler exceptionHandler;

  @Mock
  private BindingResult bindingResult;

  // Dummy-метод для создания валидного MethodParameter в тесте
  public void dummyMethodForParam(String param) {}

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
  }

  // === Тесты для ConstraintViolationException ===

  @Test
  void shouldHandleConstraintViolationWithMultipleMessagesForSameField() {
    ConstraintViolationException exception = mock(ConstraintViolationException.class);
    ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
    ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("email");
    when(violation1.getPropertyPath()).thenReturn(path);
    when(violation1.getMessage()).thenReturn("must not be empty");
    when(violation2.getPropertyPath()).thenReturn(path);
    when(violation2.getMessage()).thenReturn("must be a valid email");
    when(exception.getConstraintViolations()).thenReturn(Set.of(violation1, violation2));

    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleConstraintViolation(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Map<String, String> violations = getViolations(response);
    assertTrue(violations.get("email").contains("must not be empty"));
    assertTrue(violations.get("email").contains("must be a valid email"));
  }

  // === Тесты для MethodArgumentNotValidException ===

  @Test
  void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
    Method method = GlobalExceptionHandlerCoverageTest.class.getMethod("dummyMethodForParam", String.class);
    MethodParameter methodParameter = new MethodParameter(method, 0);
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
    FieldError fieldError = new FieldError("userDto", "email", "Email must be valid");
    when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationExceptions(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Map<String, Object> body = response.getBody();
    assertNotNull(body);
    assertEquals("Validation Failed", body.get("error"));
    Map<String, String> violations = getViolations(response);
    assertEquals("Email must be valid", violations.get("email"));
  }

  // === Тесты для DataIntegrityViolationException ===

  @Test
  void shouldHandleDataIntegrityViolationWithUniqueConstraintMessage() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Error", new Throwable("violates unique constraint"));
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("Duplicate entry violates unique constraint", response.getBody().get("message"));
  }

  @Test
  void shouldHandleDataIntegrityViolationWithUppercaseUniqueConstraintMessage() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Error", new Throwable("violates Unique constraint"));
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("Duplicate entry violates unique constraint", response.getBody().get("message"));
  }

  @Test
  void shouldHandleDataIntegrityViolationWithGenericCause() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Error", new Throwable("Some other database error"));
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("Database constraint violation", response.getBody().get("message"));
  }

  @Test
  void shouldHandleDataIntegrityViolationWithoutCause() {
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Data access error");
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("Database constraint violation", response.getBody().get("message"));
  }

  @Test
  void shouldHandleDataIntegrityViolationWithCauseAndNullMessage() {
    // ЭТОТ ТЕСТ ПОКРЫВАЕТ ОСТАВШУЮСЯ ВЕТКУ IF
    Throwable cause = mock(Throwable.class);
    when(cause.getMessage()).thenReturn(null);
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Data access error with cause", cause);

    // when
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleDataIntegrityViolation(ex);

    // then
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Database constraint violation", response.getBody().get("message"));
  }

  // === Тесты для остальных исключений ===

  @Test
  void shouldHandleIllegalStateException() {
    IllegalStateException ex = new IllegalStateException("The application is in an invalid state.");
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalState(ex);
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("The application is in an invalid state.", response.getBody().get("message"));
  }

  @Test
  void shouldHandleGenericException() {
    Exception ex = new RuntimeException("Something completely unexpected happened.");
    ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("An unexpected error occurred", response.getBody().get("message"));
  }

  // Вспомогательный метод
  @SuppressWarnings("unchecked")
  private Map<String, String> getViolations(ResponseEntity<Map<String, Object>> response) {
    Map<String, Object> body = response.getBody();
    assertNotNull(body, "Response body is null");
    return (Map<String, String>) body.get("violations");
  }
}