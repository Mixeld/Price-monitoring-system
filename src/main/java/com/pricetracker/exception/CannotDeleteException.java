package com.pricetracker.exception;

public class CannotDeleteException extends RuntimeException {

  public CannotDeleteException(String message) {
    super(message);
  }

  public CannotDeleteException(String message, Throwable cause) {
    super(message, cause);
  }
}