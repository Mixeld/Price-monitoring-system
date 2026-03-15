package com.pricetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
}