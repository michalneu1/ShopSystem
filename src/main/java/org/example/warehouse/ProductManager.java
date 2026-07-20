package org.example.warehouse;

import org.example.model.Product;

import java.math.BigDecimal;
import java.util.Optional;


/** Warehouse operations: adding products, finding them by ID and removing them from stock. */
public class ProductManager {

    private final WareHouse wareHouse;


    public ProductManager(WareHouse wareHouse) {
        this.wareHouse = wareHouse;
    }

    public void addToWareHouse(Product product) {
        wareHouse.getProducts().add(product);
    }

    public void removeProductFromWareHouse(int id, int quantity) {
        findByID(id).ifPresent(p -> p.setQuantity(p.getQuantity()-quantity));
    }

    public Optional<Product> findByID(int id) {
        return wareHouse.getProducts().stream().filter(x -> x.getId() == id).findFirst();
    }

    public void showProducts() {
        for (Product product : wareHouse.getProducts()) {
            System.out.println(product);
        }
    }

    public void updateProduct(int id, String name, BigDecimal value, int quantity) {
        Optional<Product> optional = findByID(id);
        if (optional.isPresent()) {
            Product product = optional.get();
            product.setName(name);
            product.setValue(value);
            product.setQuantity(quantity);
        }
    }

}
