package com.pricetracker.exception;

public class DuplicateResourceException extends RuntimeException {

  public DuplicateResourceException(String entityName, String field, Object value) {
    super(String.format("%s with %s '%s' already exists", entityName, field, value));
  }

  public DuplicateResourceException(String message) {
    super(message);
  }

  public DuplicateResourceException(String message, Throwable cause) {
    super(message, cause);
  }
}