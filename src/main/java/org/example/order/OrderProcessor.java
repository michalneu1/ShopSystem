package org.example.order;

import org.example.cart.CartItem;
import org.example.exception.InsufficientStockException;
import org.example.model.Config;
import org.example.model.Product;
import org.example.model.StatusType;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    public void processOrder(Order order) {
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
            System.out.println("Zamowienie zlozone. Do zaplaty: " + order.getValue());
        } catch (InsufficientStockException e) {
            order.setStatus(StatusType.REJECTED, e.getMessage());
            throw e;
        } catch (Exception e) {
            order.setStatus(StatusType.ERROR, e.getMessage());
            throw e;
        } finally {
            lock.unlock();
            String invoice = generateInvoice(order);
            repository.save(order, invoice);
        }
    }

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
        sb.append("Data zlozenia: ").append(order.getCreatedAt().format(INVOICE_DATE_FORMAT)).append("\n");
        sb.append("Klient: ").append(order.getClient()).append("\n");
        sb.append("-----------------------------\n");
        for (CartItem item : order.getItems()) {
            BigDecimal unitPrice = item.getProduct().getValue();
            for (Config chosenConfig : item.getChosenConfigs()) {
                unitPrice = unitPrice.add(chosenConfig.getAddValue());
            }
            BigDecimal linePrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            sb.append(item.getProduct().getName()).append(" x").append(item.getQuantity()).append("\n");
            for (Config chosenConfig : item.getChosenConfigs()) {
                sb.append("    + ").append(chosenConfig).append("\n");
            }
            sb.append("    cena jedn.: ").append(unitPrice)
                    .append("   razem: ").append(linePrice).append("\n");
        }
        sb.append("-----------------------------\n");
        sb.append("DO ZAPLATY: ").append(order.getValue()).append("\n");
        sb.append("=============================");
        return sb.toString();
    }
}
