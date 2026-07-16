package org.example.persistence;

import org.example.exception.PersistenceException;
import org.example.order.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final List<Order> orders = new ArrayList<>();
    private final Path file = Path.of("orders.txt");

    public void save(Order order, String invoice) {
        orders.add(order);
        try {
            Files.writeString(file, invoice + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new PersistenceException("Nie udalo sie zapisac zamowienia nr " + order.getId(), e);
        }
    }
}
