package org.example.cart;

import org.example.model.Config;
import org.example.model.Product;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private final List<CartItem> items = new ArrayList<>();

    public void addToCart(CartItem item) {
        // jesli taka sama pozycja (produkt + konfiguracja) juz jest w koszyku, laczymy ilosci
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
            // cena pozycji * ilosc
            amount = amount.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return amount;
    }

    public void makeOrder(ProductManager manager) {
        if (items.isEmpty()) {
            System.out.println("Nie mozna zlozyc zamowienia - koszyk jest pusty.");
            return;
        }

        // sprawdzamy dostepnosc kazdej pozycji w magazynie
        for (CartItem item : items) {
            Optional<Product> inStock = manager.findByID(item.getProduct().getId());
            if (inStock.isEmpty()) {
                System.out.println("Produktu nie ma juz w magazynie: " + item.getProduct().getName());
                return;
            }
            if (inStock.get().getQuantity() < item.getQuantity()) {
                System.out.println("Za malo sztuk w magazynie dla: " + item.getProduct().getName()
                        + " (dostepne: " + inStock.get().getQuantity()
                        + ", w koszyku: " + item.getQuantity() + ")");
                return;
            }
        }

        // wszystko dostepne - zdejmujemy ze stanu magazynu
        for (CartItem item : items) {
            manager.removeFromWareHouse(item.getProduct().getId(), item.getQuantity());
        }

        System.out.println("Zamowienie zlozone. Do zaplaty: " + getTotal());
        items.clear();
    }
}