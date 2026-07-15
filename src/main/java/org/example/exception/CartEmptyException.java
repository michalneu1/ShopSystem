package org.example.exception;

public class CartEmptyException extends RuntimeException {
    public CartEmptyException() {
        super("Koszyk jest pusty");
    }
}
