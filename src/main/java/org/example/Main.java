package org.example;
import org.example.cli.ShopCLI;
import org.example.model.*;

import org.example.order.OrderProcessor;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;
import org.example.warehouse.WareHouse;

import java.math.BigDecimal;
import java.util.ArrayList;


public class Main {
    public static void main(String[] args) {
        WareHouse wareHouse = new WareHouse();
        ProductManager manager = new ProductManager(wareHouse);

        Product laptop = new Product(new BigDecimal("4000"), ProductType.Computer, "Laptop Pro", 5,
                new ArrayList<>());
        laptop.addConfig(new Config("Intel i7", new BigDecimal(800), ConfigType.CPU), 30);
        laptop.addConfig(new Config("Intel i5", new BigDecimal(500), ConfigType.CPU), 2);
        laptop.addConfig(new Config("32GB RAM", new BigDecimal("600"), ConfigType.RAM), 5);

        Product phone = new Product(new BigDecimal("2500"), ProductType.Smartphone, "Smartfon X", 10,
                new ArrayList<>());
        phone.addConfig(new Config("Czarny", BigDecimal.ZERO, ConfigType.COLOR), 5);
        phone.addConfig(new Config("Etui", new BigDecimal("50"), ConfigType.ACCESSORY), 5);

        Product cable = new Product(new BigDecimal("30"), ProductType.Electronics, "Kabel USB-C", 100,
                new ArrayList<>());

        manager.addToWareHouse(laptop);
        manager.addToWareHouse(phone);
        manager.addToWareHouse(cable);

        System.out.println("=== Produkty w magazynie ===");
        manager.showProducts();


        ShopCLI cli = new ShopCLI(new OrderProcessor(manager, new OrderRepository()));
        cli.run();

    }
}