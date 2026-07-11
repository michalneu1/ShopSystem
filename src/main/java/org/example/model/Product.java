package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Product {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final int id;
    private BigDecimal value;
    private String name;
    private ProductType type;
    private List<Config> configs = new ArrayList<>();
    private int quantity;

    public Product(int id, BigDecimal value, ProductType type, String name, int quantity, List<Config> configs) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.name = name;
        this.quantity = quantity;
        this.configs = configs;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public List<Config> getConfigs() {
        return configs;
    }

    public void setConfigs(List<Config> configs) {
        this.configs = configs;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(value, product.value) && Objects.equals(name, product.name) && type == product.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, name, type);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", value=" + value +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", configs=" + configs +
                ", quantity=" + quantity +
                '}';
    }
}
