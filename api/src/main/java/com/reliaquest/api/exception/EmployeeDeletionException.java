package com.reliaquest.api.exception;

public class EmployeeDeletionException extends RuntimeException {
    public EmployeeDeletionException(String message) {
        super(message);
    }

    public EmployeeDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
