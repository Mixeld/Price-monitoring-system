package com.pricetracker.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final String TIMESTAMP = "timestamp ";
  private static final String STATUS = "status";
  private static final String ERROR = "error";
  private static final String MESSAGE = "message";

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.NOT_FOUND.value());
    response.put(ERROR, "Not Found");
    response.put(MESSAGE, e.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.CONFLICT.value());
    response.put(ERROR, "Conflict");
    response.put(MESSAGE, e.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(CannotDeleteException.class)
  public ResponseEntity<Map<String, Object>> handleCannotDelete(CannotDeleteException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.BAD_REQUEST.value());
    response.put(ERROR, "Bad Request");
    response.put(MESSAGE, e.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.UNPROCESSABLE_ENTITY.value());
    response.put(ERROR, "Business Error");
    response.put(MESSAGE, e.getMessage());
    response.put("errorCode", e.getErrorCode());

    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String,Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      fieldErrors.put(fieldName, errorMessage);
    });

    Map<String,Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.BAD_REQUEST.value());
    response.put(ERROR,"Validation Failed");
    response.put(MESSAGE, "Invalid input parameters");
    response.put(ERROR, fieldErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex) {

    Map<String, String> violations = ex.getConstraintViolations().stream()
        .collect(Collectors.toMap(
            violation -> violation.getPropertyPath().toString(),
            ConstraintViolation::getMessage,
            (v1, v2) -> v1 + ", " + v2
        ));

    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.BAD_REQUEST.value());
    response.put(ERROR, "Validation Failed");
    response.put(MESSAGE, "Constraint violation");
    response.put("violations", violations);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.NOT_FOUND.value());
    response.put(ERROR, "Not Found");
    response.put(MESSAGE, e.getMessage());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.BAD_REQUEST.value());
    response.put(ERROR, "Bad Request");
    response.put(MESSAGE, e.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.CONFLICT.value());
    response.put(ERROR, "Conflict");
    response.put(MESSAGE, e.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
      DataIntegrityViolationException e) {

    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.CONFLICT.value());
    response.put(ERROR, "Data Integrity Violation");

    String message = e.getMostSpecificCause().getMessage();
    if (message.contains("Duplicate entry") || message.contains("UNIQUE constraint")) {
      response.put(MESSAGE, "Duplicate record violates unique constraint");
    } else if (message.contains("foreign key constraint")) {
      response.put(MESSAGE, "Operation violates foreign key constraint");
    } else {
      response.put(MESSAGE, "Database integrity constraint violated: " + message);
    }

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<Map<String, Object>> handleDataAccess(DataAccessException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put(ERROR, "Database Error");
    response.put(MESSAGE, "An error occurred while accessing the database");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
      HttpMessageNotReadableException e) {

    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.BAD_REQUEST.value());
    response.put(ERROR, "Malformed JSON Request");
    response.put(MESSAGE, "The request body is malformed or contains invalid data");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException e) {

    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.BAD_REQUEST.value());
    response.put(ERROR, "Type Mismatch");

    String paramName = e.getName();
    String requiredType = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown";
    response.put(MESSAGE, String.format("Parameter '%s' must be of type '%s'", paramName, requiredType));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.NOT_FOUND.value());
    response.put(ERROR, "Not Found");
    response.put(MESSAGE, "The requested resource was not found");

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put(ERROR, "Internal Server Error");
    response.put(MESSAGE, "An unexpected error occurred. Please try again later.");

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}