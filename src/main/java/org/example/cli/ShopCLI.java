package org.example.cli;

import org.example.cart.Cart;
import org.example.cart.CartItem;
import org.example.exception.ProductNotFoundException;
import org.example.model.Client;
import org.example.model.Config;
import org.example.model.Product;
import org.example.model.ProductType;
import org.example.order.Order;
import org.example.order.OrderProcessor;
import org.example.persistence.DiscountRepository;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Shop interface. Reads user input only on the main thread.
 */
public class ShopCLI {
    private ProductManager manger;
    private OrderProcessor orderProcessor;
    private Scanner scanner;
    private Cart cart;
    private ExecutorService pool = Executors.newFixedThreadPool(4);
    private final DiscountRepository discountRepository = new DiscountRepository(Path.of("discounts.txt"));

    public ShopCLI(OrderProcessor orderProcessor) {
        this.manger = orderProcessor.getManager();
        this.orderProcessor = orderProcessor;
        this.scanner = new Scanner(System.in);
        cart = new Cart();
    }

    public void run() {
        boolean running = true;
        while (running) {
            showMenu();
            System.out.println("Podaj odpowiedź");
            try {
                switch (getChoice()) {
                    case 1 -> manger.showProducts();
                    case 2 -> addProductToCart();
                    case 3 -> cart.showCart();
                    case 4 -> placeOrder();
                    case 5 -> cart = new Cart();
                    case 6 -> {
                        running = false;
                        pool.shutdown();
                        pool.awaitTermination(10, TimeUnit.SECONDS);
                    }
                    default -> System.out.println("Nieznana opcja");
                }
            } catch (Exception e) {
                System.out.println("Błąd: " + e.getMessage());
            }
        }
        System.out.println("Do widzenia!");
    }

    private int getChoice() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Podana wartość nie jest poprawna");
            }
        }
    }

    private void showMenu() {
        System.out.println("1. Przeglądaj produkty");
        System.out.println("2. Dodaj produkt do koszyka");
        System.out.println("3. Sprawdż koszyk");
        System.out.println("4. Złóż zamówienie");
        System.out.println("5. Wyczyść koszyk");
        System.out.println("6. wyjdź");
    }

    private void addProductToCart() {
        manger.showProducts();
        System.out.println("Podaj id produktu");
        int choice = getChoice();
        System.out.println("Podaj ilość");
        int choosedQuantity = getChoice();
        Product choosedProduct = manger.findByID(choice)
                .orElseThrow(() -> new ProductNotFoundException(choice));
        int counter = 0;
        for (Config config : choosedProduct.getConfigs()) {
            System.out.println(counter + " " + config);
            counter++;
        }
        if (choosedProduct.getType() == ProductType.Electronics) {
            cart.addToCart(new CartItem(choosedProduct, choosedQuantity));
            System.out.println("Dodano do koszyka.");
            return;
        }
        System.out.println("Podaj wybrane konfiguracje po przecinku np: (0,1,2) lub Enter aby pominąć");
        ArrayList<Config> choosedConfigs = new ArrayList<>();
        String line = scanner.nextLine().trim();
        if (!line.isEmpty()) {
            for (String s : line.split(",")) {
                try {
                    Config original = choosedProduct.getConfigs().get(Integer.parseInt(s.trim()));
                    System.out.println(original);
                    System.out.println("Podaj ilość");
                    choosedConfigs.add(new Config(original, getChoice()));
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    System.out.println("Pomijam niepoprawną konfigurację: " + s);
                }
            }
        }
        cart.addToCart(new CartItem(choosedProduct, choosedConfigs, choosedQuantity));
        System.out.println("Dodano do koszyka.");
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            System.out.println("Koszyk jest pusty.");
            return;
        }
        Optional<Order> order = cart.makeOrder(readClient());
        BigDecimal discount = getDiscount();
        order.ifPresent(o -> pool.submit(() -> {
            try {
                orderProcessor.processOrder(o,discount);
            } catch (Exception e) {
                System.out.println("Błąd: " + e.getMessage());
            }
        }));
    }

    private BigDecimal getDiscount() {
        System.out.println("Wpisz kod rabatowy (Enter = brak)");
        String code = scanner.nextLine().trim();
        if (code.isEmpty()) {
            return BigDecimal.ONE;
        }
        BigDecimal discount = discountRepository.getDiscount(code);
        if (discount.compareTo(BigDecimal.ONE) == 1) {
            System.out.println("Nieznany kod rabatowy");
        }
        return discount;
    }

    private Client readClient() {
        System.out.println("Podaj imię");
        String name = scanner.nextLine().trim();
        System.out.println("Podaj nazwisko");
        String lastName = scanner.nextLine().trim();
        System.out.println("Podaj wiek");
        short age = (short) getChoice();
        System.out.println("Podaj email");
        String mail = scanner.nextLine().trim();
        return new Client(name, lastName, age, mail);
    }

}
