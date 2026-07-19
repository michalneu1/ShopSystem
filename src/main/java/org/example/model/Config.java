package org.example.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Config {
    private String name;
    private BigDecimal addValue;
    private int quantity;
    private ConfigType type;

    public Config(String name, BigDecimal addValue, ConfigType type, int quantity) {
        this.name = name;
        this.addValue = addValue;
        this.type = type;
        this.quantity = quantity;
    }

    public Config(Config other) {
        this.name = other.name;
        this.addValue = other.addValue;
        this.type = other.type;
        this.quantity = other.quantity;
    }
    public Config(Config other, int quantity) {
        this.name = other.name;
        this.addValue = other.addValue;
        this.type = other.type;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAddValue() {
        return addValue;
    }

    public void setAddValue(BigDecimal addValue) {
        this.addValue = addValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if(quantity<0){
            throw new IllegalArgumentException("Wartość ilości nie może być mniejsza od 0");
        }
        this.quantity = quantity;
    }

    public ConfigType getType() {
        return type;
    }

    public void setType(ConfigType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return " Config{" +
                "name='" + name + '\'' +
                ", addValue=" + addValue +
                ", quantity=" + quantity +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(name, config.name) && Objects.equals(addValue, config.addValue) && type == config.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, addValue, type);
    }
}
