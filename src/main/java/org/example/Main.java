package org.example;

import org.example.cart.Cart;
import org.example.cart.CartItem;
import org.example.model.*;
import org.example.order.Order;
import org.example.order.OrderProcessor;
import org.example.warehouse.ProductManager;
import org.example.warehouse.WareHouse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        WareHouse wareHouse = new WareHouse();
        ProductManager manager = new ProductManager(wareHouse);

        Config i7 = new Config("Intel i7", new BigDecimal(800), ConfigType.CPU,3);
        Config i5 = new Config("Intel i5", new BigDecimal(500), ConfigType.CPU,2);
        Config ram32 = new Config("32GB RAM", new BigDecimal("600"), ConfigType.RAM,5);
        Product laptop = new Product(new BigDecimal("4000"), ProductType.Computer, "Laptop Pro", 5,
                new ArrayList<>(List.of(i7,i5, ram32)));

        Config black = new Config("Czarny", BigDecimal.ZERO, ConfigType.COLOR,5);
        Config etui = new Config("Etui", new BigDecimal("50"), ConfigType.ACCESSORY,5);
        Product phone = new Product(new BigDecimal("2500"), ProductType.Smartphone, "Smartfon X", 10,
                new ArrayList<>(List.of(black, etui)));

        Product cable = new Product(new BigDecimal("30"), ProductType.Electronics, "Kabel USB-C", 100,
                new ArrayList<>());

        manager.addToWareHouse(laptop);
        manager.addToWareHouse(phone);
        manager.addToWareHouse(cable);

        System.out.println("=== Produkty w magazynie ===");
        manager.showProducts();
        Client client = new Client("Jan","kowalski", (short) 15,"test@interia.pl");
        Cart cart = new Cart();

        Config i7c = new Config("Intel i7", new BigDecimal(800), ConfigType.CPU,1);
        Config ram32c = new Config("32GB RAM", new BigDecimal("600"), ConfigType.RAM,1);

        cart.addToCart(new CartItem(laptop, List.of(i7c, ram32c), 1));
        cart.addToCart(new CartItem(phone, List.of(black, etui), 2));
        cart.addToCart(new CartItem(cable, new ArrayList<>(), 3));

        System.out.println("\n=== Proba dodania telefonu z procesorem ===");
        cart.addToCart(new CartItem(phone, List.of(i7), 1));

        System.out.println("\n=== Koszyk ===");
        cart.showCart();

        System.out.println("\n=== Skladanie zamowienia ===");
        Optional<Order> order = cart.makeOrder(manager, client);

        OrderProcessor processor = new OrderProcessor(manager);
        order.ifPresent(processor::processOrder);

        System.out.println("\n=== Stan magazynu po zamowieniu ===");
        manager.showProducts();
    }
}