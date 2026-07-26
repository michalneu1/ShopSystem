package org.example.order;

import org.example.cart.CartItem;
import org.example.exception.InsufficientStockException;
import org.example.model.Config;
import org.example.model.Product;
import org.example.model.StatusType;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Fulfills orders: checks stock levels, removes goods from the warehouse,
 * sets the status and saves the invoice.
 */
public class OrderProcessor {
    private static final DateTimeFormatter INVOICE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProductManager manager;
    private final OrderRepository repository;
    private final Lock lock = new ReentrantLock();


    public ProductManager getManager() {
        return manager;
    }

    public OrderProcessor(ProductManager manager, OrderRepository repository) {
        this.manager = manager;
        this.repository = repository;
    }

    public void printProcessResult(Order order){
        if(order.getStatus().getType()==StatusType.ACCEPTED){
            System.out.println("Zamowienie zlozone. Do zaplaty: " + discountedValue(order));
        }else {
            System.out.println(order.getStatus());
        }
    }

    public void saveInvoice(Order order){
        String invoice = generateInvoice(order);
        repository.save(order, invoice);
    }

    public Order processOrder(Order order) {
        lock.lock();
        try {
            order.setStatus(StatusType.PENDING);
            for (CartItem item : order.getItems()) {
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
            for (CartItem item : order.getItems()) {
                manager.findByID(item.getProduct().getId()).ifPresent(itemInWarehouse -> {
                    manager.removeProductFromWareHouse(item.getProduct().getId(), item.getQuantity());
                    for (Config config : itemInWarehouse.getConfigs()) {
                        for (Config chosenConfig : item.getChosenConfigs()) {
                            if (chosenConfig.equals(config)) {
                                config.setQuantity(config.getQuantity() - chosenConfig.getQuantity());
                            }
                        }
                    }
                });
            }
            order.setStatus(StatusType.ACCEPTED);
        } catch (InsufficientStockException e) {
            order.setStatus(StatusType.REJECTED, e.getMessage());
        } catch (Exception e) {
            order.setStatus(StatusType.ERROR, e.getMessage());
        } finally {
            lock.unlock();
        }
        return order;
    }

    /**
     * Builds the invoice text.
     */
    public String generateInvoice(Order order) {
        StringBuilder sb = new StringBuilder();
        String title =
                switch (order.getStatus().getType()) {
                    case ACCEPTED -> "========== FAKTURA ==========\n";
                    case ERROR -> "======= FAKTURA BŁĘDNA =======\n";
                    case REJECTED -> "====== FAKTURA ODRZUCONA========\n";
                    default -> "========== FAKTURA ==========\n";
                };

        sb.append(title);
        sb.append("Nr zamowienia: ").append(order.getId()).append("\n");
        sb.append("Data zlozenia: ").append(LocalDateTime.ofInstant(order.getCreatedAt(),
                ZoneId.systemDefault()).format(INVOICE_DATE_FORMAT)).append("\n");
        sb.append("Klient: ").append(order.getClient()).append("\n");
        sb.append("-----------------------------\n");
        for (CartItem item : order.getItems()) {
            BigDecimal linePrice = item.getProduct().getValue()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            sb.append(item.getProduct().getName()).append(" x").append(item.getQuantity())
                    .append("   cena jedn.: ").append(item.getProduct().getValue()).append("\n");
            for (Config chosenConfig : item.getChosenConfigs()) {
                linePrice = linePrice.add(chosenConfig.getAddValue()
                        .multiply(BigDecimal.valueOf(chosenConfig.getQuantity())));
                sb.append("    + ").append(chosenConfig).append("\n");
            }
            sb.append("    razem: ").append(linePrice).append("\n");
        }
        sb.append("-----------------------------\n");
        sb.append("Suma: ").append(order.getValue()).append("\n");
        if (order.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Rabat: -").append(order.getValue().subtract(discountedValue(order))).append("\n");
        }
        sb.append("DO ZAPLATY: ").append(discountedValue(order)).append("\n");
        sb.append("=============================");
        return sb.toString();
    }

    private BigDecimal discountedValue(Order order) {
        return order.getValue()
                .multiply(order.getDiscount())
                .setScale(2, RoundingMode.HALF_UP);
    }
}
