package org.example.order;

import org.example.cart.CartItem;
import org.example.model.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Order {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final int id;
    private final List<CartItem> items;
    private final BigDecimal value;
    private final Client client;
    private final LocalDateTime createdAt;

    public Order(List<CartItem> items, BigDecimal value, Client client) {
        this.id = COUNTER.getAndIncrement();
        this.items = items;
        this.value = value;
        this.client = client;
        this.createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Client getClient() {
        return client;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", products=" + items +
                ", value=" + value +
                ", client=" + client +
                ", createdAt=" + createdAt +
                '}';
    }
}
