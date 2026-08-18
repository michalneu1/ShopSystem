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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
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
    private final int TIMEOUT = 10;

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
                Optional<MenuOption> option = MenuOption.fromId(getChoice());
                if (option.isEmpty()) {
                    System.out.println("Nieznana opcja");
                    continue;
                }
                switch (option.get()) {
                    case SHOW_PRODUCTS -> manger.showProducts();
                    case ADD_PRODUCT -> addProductToCart();
                    case SHOW_CART -> cart.showCart();
                    case PLACE_ORDER -> placeOrder();
                    case CLEAR_CART -> cart = new Cart();
                    case END -> {
                        running = false;
                        pool.shutdown();
                        pool.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
                    }
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
        for (MenuOption option : MenuOption.values()) {
            System.out.println(option.getId() + ". " + option.getLabel());
        }
    }

    private Product getProduct(int choice) {
        return manger.findByID(choice).orElseThrow(() -> new ProductNotFoundException(choice));
    }

    private void addProductToCart() {
        manger.showProducts();
        System.out.println("Podaj id produktu");
        int choice = getChoice();
        System.out.println("Podaj ilość");
        int choosedQuantity = getChoice();
        Product choosedProduct = getProduct(choice);
        showConfigs(choosedProduct);
        cart.addToCart(new CartItem(choosedProduct, readConfigs(choosedProduct), choosedQuantity));
        System.out.println("Dodano do koszyka.");
    }

    private void showConfigs(Product product) {
        int counter = 0;
        for (Config config : product.getConfigs()) {
            System.out.println(counter + " " + config);
            counter++;
        }
    }

    private Map<Config, Integer> readConfigs(Product product) {
        Map<Config, Integer> choosedConfigs = new LinkedHashMap<>();
        if (product.getType() == ProductType.Electronics) {
            return choosedConfigs;
        }
        System.out.println("Podaj wybrane konfiguracje po przecinku np: (0,1,2) lub Enter aby pominąć");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return choosedConfigs;
        }
        for (String s : line.split(",")) {
            readSingleConfig(product, s, choosedConfigs);
        }
        return choosedConfigs;
    }

    private void readSingleConfig(Product product, String token, Map<Config, Integer> choosedConfigs) {
        try {
            Config original = product.getConfigs().get(Integer.parseInt(token.trim()));
            System.out.println(original);
            System.out.println("Podaj ilość (na sztukę)");
            choosedConfigs.merge(original, getChoice(), Integer::sum);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            System.out.println("Pomijam niepoprawną konfigurację: " + token);
        }
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            System.out.println("Koszyk jest pusty.");
            return;
        }
        Optional<Order> order = cart.makeOrder(readClient(), getDiscount());
        order.ifPresent((o) -> CompletableFuture.supplyAsync
                        (() -> orderProcessor.processOrder(o), pool)
                .thenAccept(processed -> {
                    orderProcessor.printProcessResult(processed);
                    orderProcessor.saveInvoice(processed);
                }).exceptionally(ex -> {
                    System.out.println(ex.getCause().getMessage());
                    return null;
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
