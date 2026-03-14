package com.pricetracker.exception;

public class ResourceNotFoundException extends RuntimeException {

  // Конструктор для случая "Сущность не найдена по полю"
  public ResourceNotFoundException(String entityName, String field, Object value) {
    super(String.format("%s not found with %s: '%s'", entityName, field, value));
  }

  // Конструктор для случая "Сущность не найдена" (общий случай)
  public ResourceNotFoundException(String message) {
    super(message);
  }

  // Конструктор с причиной
  public ResourceNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}