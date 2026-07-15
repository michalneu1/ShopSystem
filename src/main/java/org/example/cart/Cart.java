package org.example.cart;

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
                System.out.println("Nie dodano do koszyka - konfiguracja \"" + chosen
                        + "\" nie jest dostepna dla produktu: " + item.getProduct().getName());
                return;
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
            System.out.println("Koszyk jest pusty.");
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
            System.out.println("Nie mozna zlozyc zamowienia - koszyk jest pusty.");
            return Optional.empty();
        }

        for (CartItem item : items) {
            Optional<Product> inStock = manager.findByID(item.getProduct().getId());
            if (inStock.isEmpty()) {
                System.out.println("Produktu nie ma juz w magazynie: " + item.getProduct().getName());
                return Optional.empty();
            }
            if (inStock.get().getQuantity() < item.getQuantity()) {
                System.out.println("Za malo sztuk w magazynie dla: " + item.getProduct().getName()
                        + " (dostepne: " + inStock.get().getQuantity()
                        + ", w koszyku: " + item.getQuantity() + ")");
                return Optional.empty();
            }
            for (Config chosen : item.getChosenConfigs()) {
                Optional<Config> stockConfig = inStock.get().getConfigs().stream()
                        .filter(c -> c.equals(chosen)).findFirst();
                if (stockConfig.isEmpty()) {
                    System.out.println("Konfiguracji nie ma juz w magazynie: " + chosen.getName()
                            + " dla produktu: " + item.getProduct().getName());
                    return Optional.empty();
                }
                if (stockConfig.get().getQuantity() < chosen.getQuantity()) {
                    System.out.println("Za malo konfiguracji \"" + chosen.getName() + "\" w magazynie dla: "
                            + item.getProduct().getName() + " (dostepne: " + stockConfig.get().getQuantity()
                            + ", potrzeba: " + chosen.getQuantity() + ")");
                    return Optional.empty();
                }
            }
        }
        Optional<Order> order = Optional.of(new Order(new ArrayList<>(items), getTotal(),client));
        System.out.println("Zamowienie zlozone. Do zaplaty: " + getTotal());
        items.clear();
        return order;
    }
}
