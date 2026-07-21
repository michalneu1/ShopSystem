package org.example.cart;

import org.example.model.Config;
import org.example.model.Product;

import java.util.List;
import java.util.Objects;

public class CartItem {
    private final Product product;
    private final List<Config> chosenConfigs;
    private int quantity;

    public Product getProduct() {
        return product;
    }

    public List<Config> getChosenConfigs() {
        return chosenConfigs;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CartItem(Product product, List<Config> chosenConfigs, int quantity) {
        this.product = product;
        this.chosenConfigs = chosenConfigs;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "product=" + product +
                ", chosenConfigs=" + chosenConfigs +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(product, cartItem.product) && Objects.equals(chosenConfigs, cartItem.chosenConfigs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, chosenConfigs);
    }
}
