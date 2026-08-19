package org.example.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String name, int requested, int available) {
        super("Brak wystarczającej ilości '" + name + "': żądano " + requested
                + ", dostępne " + available);
    }

    public InsufficientStockException(String name, int requested) {
        this(name, requested, 0);
    }
}
