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

    public Optional<Order> makeOrder(ProductManager manager, Client client) {
        if (items.isEmpty()) {
            throw new CartEmptyException();
        }

        for (CartItem item : items) {
            Optional<Product> inStock = manager.findByID(item.getProduct().getId());
            if (inStock.isEmpty()) {
                throw new InsufficientStockException(item.getProduct().getName(), item.getQuantity());
            }
            if (inStock.get().getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        item.getProduct().getName(), item.getQuantity(), inStock.get().getQuantity());
            }
            for (Config chosen : item.getChosenConfigs()) {
                Optional<Config> stockConfig = inStock.get().getConfigs().stream()
                        .filter(c -> c.equals(chosen)).findFirst();
                if (stockConfig.isEmpty()) {
                    throw new InsufficientStockException(chosen.getName(), chosen.getQuantity());
                }
                if (stockConfig.get().getQuantity() < chosen.getQuantity()) {
                    throw new InsufficientStockException(chosen.getName(), chosen.getQuantity(), stockConfig.get().getQuantity());
                }
            }
        }
        Optional<Order> order = Optional.of(new Order(new ArrayList<>(items), getTotal(), client));
        System.out.println("Zamowienie zlozone. Do zaplaty: " + getTotal());
        items.clear();
        return order;
    }
}
