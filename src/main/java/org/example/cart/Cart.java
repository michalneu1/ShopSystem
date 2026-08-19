package org.example.cart;

import org.example.exception.CartEmptyException;
import org.example.model.Client;
import org.example.order.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Customer's cart: holds items before an order is placed, calculates the total.
 */
public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addToCart(CartItem newItem) {
        items.stream()
                .filter(newItem::equals)
                .findFirst()
                .ifPresentOrElse(cartItem ->
                                cartItem.setQuantity(cartItem.getQuantity() + newItem.getQuantity()),
                                () -> items.add(newItem));
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
        return items.stream()
                .map(this::itemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal itemPrice(CartItem item) {
        BigDecimal unitPrice = item.getProduct().getValue().add(configsUnitPrice(item));
        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal configsUnitPrice(CartItem item) {
        return item.getChosenConfigs().entrySet().stream()
                .map(chosen -> chosen.getKey().getAddValue()
                        .multiply(BigDecimal.valueOf(chosen.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Creates an order from the cart and clears the cart
     */
    public Optional<Order> makeOrder(Client client, BigDecimal discount) {
        if (items.isEmpty()) {
            throw new CartEmptyException();
        }
        Optional<Order> order = Optional.of(new Order(new ArrayList<>(items), getTotal(), client, discount));
        items.clear();
        return order;
    }
}
