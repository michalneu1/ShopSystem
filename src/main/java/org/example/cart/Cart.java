package org.example.cart;

import org.example.exception.CartEmptyException;
import org.example.exception.IllegalConfigurationException;
import org.example.exception.InsufficientStockException;
import org.example.model.Client;
import org.example.model.Config;
import org.example.model.Product;
import org.example.order.Order;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Customer's cart: holds items before an order is placed, calculates the total. */
public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addToCart(CartItem item) {
        for (Config chosen : item.getChosenConfigs()) {
            if (!item.getProduct().getConfigs().contains(chosen)) {
                throw new IllegalConfigurationException("Nie dodano do koszyka - konfiguracja \"" + chosen
                        + "\" nie jest dostepna dla produktu: " + item.getProduct().getName());
            }
        }

        for (CartItem existing : items) {
            if (existing.equals(item)) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    public void removeFromCart(CartItem item) {
        items.remove(item);
    }

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void showCart() {
        if (items.isEmpty()) {
            System.out.println("Koszyk jest pusty");
            return;
        }
        for (CartItem item : items) {
            System.out.println(item);
        }
        System.out.println("Razem: " + getTotal());
    }

    public BigDecimal getTotal() {
        BigDecimal amount = BigDecimal.ZERO;
        for (CartItem item : items) {
            BigDecimal itemPrice = item.getProduct().getValue();
            for (Config chosenConfig : item.getChosenConfigs()) {
                itemPrice = itemPrice.add(chosenConfig.getAddValue());
            }
            amount = amount.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return amount;
    }

    /** Creates an order from the cart contents and clears the cart */
    public Optional<Order> makeOrder(Client client) {
        if (items.isEmpty()) {
            throw new CartEmptyException();
        }
        Optional<Order> order = Optional.of(new Order(new ArrayList<>(items), getTotal(), client));
        items.clear();
        return order;
    }
}
