package org.example.cart;

import org.example.exception.CartEmptyException;
import org.example.exception.IllegalConfigurationException;
import org.example.model.Client;
import org.example.model.Config;
import org.example.order.Order;

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
                throw new IllegalConfigurationException("Nie dodano do koszyka konfiguracja \"" + chosen
                        + "\" nie jest dostepna dla produktu: " + item.getProduct().getName());
            }
        }

        for (CartItem existing : items) {
            if (existing.equals(item)) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                List<Config> existingConfigs = existing.getChosenConfigs();
                List<Config> addedConfigs = item.getChosenConfigs();
                for (int i = 0; i < existingConfigs.size(); i++) {
                    Config target = existingConfigs.get(i);
                    target.setQuantity(target.getQuantity() + addedConfigs.get(i).getQuantity());
                }
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
            BigDecimal itemPrice = item.getProduct().getValue()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            for (Config chosenConfig : item.getChosenConfigs()) {
                itemPrice = itemPrice.add(chosenConfig.getAddValue()
                        .multiply(BigDecimal.valueOf(chosenConfig.getQuantity())));
            }
            amount = amount.add(itemPrice);
        }
        return amount;
    }

    /** Creates an order from the cart contents and clears the cart */
    public Optional<Order> makeOrder(Client client,BigDecimal discount) {
        if (items.isEmpty()) {
            throw new CartEmptyException();
        }
        Optional<Order> order = Optional.of(new Order(new ArrayList<>(items), getTotal(), client, discount));
        items.clear();
        return order;
    }
}
