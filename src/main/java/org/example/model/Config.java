package org.example.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
public class Config {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private String name;
    private BigDecimal addValue;
    private ConfigType type;
    private final int id;
    private int quantity;

    public Config(String name, BigDecimal addValue, ConfigType type, int quantity) {
        this.name = name;
        this.addValue = addValue;
        this.type = type;
        this.id = COUNTER.getAndIncrement();
        this.quantity=quantity;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAddValue() {
        return addValue;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void removeStock(int quantity) {
        int left = this.quantity - quantity;
        if (left < 0) {
            throw new IllegalArgumentException(
                    "Za mało na stanie: " + name + " (potrzeba " + quantity + ", jest " + this.quantity + ")");
        }
        this.quantity = left;
    }

    @Override
    public String toString() {
        return " Config{" +
                "name='" + name + '\'' +
                ", addValue=" + addValue +
                ", type=" + type +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return id == config.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
