package org.example.cart;

import org.example.model.Config;
import org.example.model.Product;

import java.util.List;

public class CartItem {
    private final Product product;
    private final List<Config> chosenConfigs;
    private int quantity;

    public CartItem(Product product, List<Config> chosenConfigs, int quantity) {
        this.product = product;
        this.chosenConfigs = chosenConfigs;
        this.quantity = quantity;
    }

}
