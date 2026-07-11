package org.example.warehouse;

import org.example.model.Product;

import java.util.ArrayList;
import java.util.List;

public class WareHouse {
    private final List<Product> products;

    public WareHouse() {
        this.products = new ArrayList<>();
    }

    public WareHouse(List<Product> products) {
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }
}
