package com.reliaquest.api.exception;

public class EmployeeCreationException extends RuntimeException {
    public EmployeeCreationException(String message) {
        super(message);
    }

    public EmployeeCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
