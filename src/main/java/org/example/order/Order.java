package org.example.order;

import org.example.cart.CartItem;
import org.example.model.Client;
import org.example.model.Status;
import org.example.model.StatusType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A placed order: cart items, price, client, status. ID is assigned automatically.
 */
public class Order {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final int id;
    private final List<CartItem> items;
    private final BigDecimal value;
    private final Client client;
    private final Instant createdAt;
    private Status status;
    private final BigDecimal discount;

    public Order(List<CartItem> items, BigDecimal value, Client client, BigDecimal discount) {
        this.id = COUNTER.getAndIncrement();
        this.items = items;
        this.value = value;
        this.client = client;
        this.createdAt = Instant.now();
        this.status = new Status(StatusType.NEW, "Nowe zamówienie");
        this.discount = discount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(StatusType type) {
        this.status = new Status(type, null);
    }

    public void setStatus(StatusType type, String description) {
        this.status = new Status(type, description);
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

    public Instant getCreatedAt() {
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
