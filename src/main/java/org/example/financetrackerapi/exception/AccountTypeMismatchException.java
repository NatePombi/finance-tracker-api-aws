package org.example.financetrackerapi.exception;

public class AccountTypeMismatchException extends RuntimeException {
  public AccountTypeMismatchException(String message) {
    super(message);
  }
}
