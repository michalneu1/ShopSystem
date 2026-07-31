package org.example;
import org.example.cli.ShopCLI;
import org.example.model.*;

import org.example.order.OrderProcessor;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;
import org.example.warehouse.WareHouse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        WareHouse wareHouse = new WareHouse();
        ProductManager manager = new ProductManager(wareHouse);

        Config i7 = new Config("Intel i7", new BigDecimal(800), ConfigType.CPU,30);
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


        ShopCLI cli = new ShopCLI(new OrderProcessor(manager, new OrderRepository()));
        cli.run();

    }
}