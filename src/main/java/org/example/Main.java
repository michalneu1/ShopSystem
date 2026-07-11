package org.example;

import org.example.cart.Cart;
import org.example.cart.CartItem;
import org.example.model.Config;
import org.example.model.ConfigType;
import org.example.model.Product;
import org.example.model.ProductType;
import org.example.warehouse.ProductManager;
import org.example.warehouse.WareHouse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // --- przygotowanie magazynu (Task 2) ---
        WareHouse wareHouse = new WareHouse();
        ProductManager manager = new ProductManager(wareHouse);

        Product laptop = new Product(new BigDecimal("4000"), ProductType.Computer, "Laptop Pro", 5, new ArrayList<>());
        Product phone = new Product(new BigDecimal("2500"), ProductType.Smartphone, "Smartfon X", 10, new ArrayList<>());
        Product cable = new Product(new BigDecimal("30"), ProductType.Electronics, "Kabel USB-C", 100, new ArrayList<>());

        manager.addToWareHouse(laptop);
        manager.addToWareHouse(phone);
        manager.addToWareHouse(cable);

        System.out.println("=== Produkty w magazynie ===");
        manager.showProducts();

        // --- Task 4: dodawanie produktow do koszyka ---
        Cart cart = new Cart();

        // laptop z konfiguracja (Task 1 - konfiguracja komputera)
        List<Config> laptopConfig = List.of(
                new Config("Intel i7", new BigDecimal("800"), ConfigType.CPU),
                new Config("32GB RAM", new BigDecimal("600"), ConfigType.RAM)
        );
        cart.addToCart(new CartItem(laptop, laptopConfig, 1));

        // smartfon z konfiguracja (Task 1 - kolor, akcesoria)
        List<Config> phoneConfig = List.of(
                new Config("Czarny", BigDecimal.ZERO, ConfigType.COLOR),
                new Config("Etui", new BigDecimal("50"), ConfigType.ACCESSORY)
        );
        cart.addToCart(new CartItem(phone, phoneConfig, 2));

        // zwykla elektronika - bez konfiguracji
        cart.addToCart(new CartItem(cable, new ArrayList<>(), 3));

        // --- Task 4: przegladanie koszyka ---
        System.out.println("\n=== Koszyk ===");
        cart.showCart();

        // --- Task 4: skladanie zamowienia ---
        System.out.println("\n=== Skladanie zamowienia ===");
        cart.makeOrder(manager);

        System.out.println("\n=== Stan magazynu po zamowieniu ===");
        manager.showProducts();
    }
}