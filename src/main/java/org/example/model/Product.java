package org.example.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Product {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private final int id;
    private BigDecimal value;
    private String name;
    private ProductType type;
    private List<Config> configs = new ArrayList<>();
    private final Map<Integer, Integer> configStock = new HashMap<>();
    private int quantity;

    public Product(BigDecimal value, ProductType type, String name, int quantity, List<Config> configs) {
        this.id = COUNTER.getAndIncrement();
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

    public List<Config> getConfigs() {
        return configs;
    }

    public void addConfig(Config config, int stock) {
        configs.add(config);
        configStock.put(config.getId(), stock);
    }

    public int getConfigStock(Config config) {
        return configStock.getOrDefault(config.getId(), 0);
    }

    public void removeConfigStock(Config config, int quantity) {
        int left = getConfigStock(config) - quantity;
        if (left < 0) {
            throw new IllegalArgumentException("Ilość nie może być mniejsza od 0");
        }
        configStock.put(config.getId(), left);
    }

    public int getQuantity() {
        return quantity;
    }

    public void removeStock(int quantity) {
        setQuantity(this.quantity - quantity);
    }

    public void setQuantity(int quantity) {
        if(quantity<0){
            throw new IllegalArgumentException("Ilość nie może być mniejsza od 0");
        }
        this.quantity = quantity;
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
