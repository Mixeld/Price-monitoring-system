package com.pricetracker.exception;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String entityName, String field, Object value) {
    super(String.format("%s not found with %s: '%s'", entityName, field, value));
  }

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}