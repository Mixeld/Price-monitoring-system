package com.pricetracker.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final String TIMESTAMP = "timestamp";
  private static final String STATUS = "status";
  private static final String ERROR = "error";
  private static final String MESSAGE = "message";

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException e) {
    log.warn("Resource not found: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException e) {
    log.warn("Duplicate resource: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", e.getMessage());
  }

  @ExceptionHandler(CannotDeleteException.class)
  public ResponseEntity<Map<String, Object>> handleCannotDelete(CannotDeleteException e) {
    log.warn("Cannot delete: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException e) {
    log.warn("Business error [{}]: {}", e.getErrorCode(), e.getMessage());
    ResponseEntity<Map<String, Object>> response =
        buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Business Error", e.getMessage());
    response.getBody().put("errorCode", e.getErrorCode());
    return response;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>>
  handleValidationExceptions(MethodArgumentNotValidException ex) {
    log.warn("Validation failed: {}", ex.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.put(error.getField(), error.getDefaultMessage())
    );

    ResponseEntity<Map<String, Object>> response =
        buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", "Invalid input parameters");
    response.getBody().put("violations", fieldErrors);
    return response;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());

    Map<String, String> violations = ex.getConstraintViolations().stream()
        .collect(Collectors.toMap(
            violation -> violation.getPropertyPath().toString(),
            ConstraintViolation::getMessage,
            (v1, v2) -> v1 + ", " + v2
        ));

    ResponseEntity<Map<String, Object>> response =
        buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", "Constraint violation");
    response.getBody().put("violations", violations);
    return response;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("Illegal argument: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
    log.warn("Illegal state: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.CONFLICT, "Conflict", e.getMessage());
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>>
  handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException e) {
    log.warn("Data integrity violation: {}", e.getMessage());

    String message = "Database constraint violation";
    if (e.getCause() != null && e.getCause().getMessage() != null) {
      String causeMsg = e.getCause().getMessage();
      if (causeMsg.contains("Unique") || causeMsg.contains("unique")) {
        message = "Duplicate entry violates unique constraint";
      }
    }
    return buildErrorResponse(HttpStatus.CONFLICT, "Data Integrity Violation", message);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error", "An unexpected error occurred");
  }

  private ResponseEntity<Map<String, Object>>
  buildErrorResponse(HttpStatus status, String error, String message) {
    Map<String, Object> response = new HashMap<>();
    response.put(TIMESTAMP, LocalDateTime.now());
    response.put(STATUS, status.value());
    response.put(ERROR, error);
    response.put(MESSAGE, message);
    return ResponseEntity.status(status).body(response);
  }
}