package org.example.financetrackerapi.exception;

public class AccountsNotFoundException extends RuntimeException {
    public AccountsNotFoundException(String message) {
        super(message);
    }
}
