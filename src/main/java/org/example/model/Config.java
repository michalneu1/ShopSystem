package org.example.model;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
//todo: dodac quantity i usunac quantitystock
public class Config {
    private static final AtomicInteger COUNTER = new AtomicInteger(1);
    private String name;
    private BigDecimal addValue;
    private ConfigType type;
    private final int id;

    public Config(String name, BigDecimal addValue, ConfigType type) {
        this.name = name;
        this.addValue = addValue;
        this.type = type;
        this.id = COUNTER.getAndIncrement();
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

    @Override
    public String toString() {
        return " Config{" +
                "name='" + name + '\'' +
                ", addValue=" + addValue +
                ", type=" + type +
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
