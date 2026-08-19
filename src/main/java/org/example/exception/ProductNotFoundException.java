package org.example.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(int id) {
        super("Nie znaleziono produktu o id: " + id);
    }
}