package org.example.exception;

public class IllegalConfigurationException extends RuntimeException {
    public IllegalConfigurationException(String message) {
        super(message);
    }
}
