package org.example.warehouse;

import org.example.model.Config;
import org.example.model.Product;

import java.util.List;

public class WareHouse {
    private final List<Product> products;
    private final List<Config> configs;

    public WareHouse(List<Product> products, List<Config> configs) {
        this.products = products;
        this.configs = configs;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Config> getConfigs() {
        return configs;
    }
}
