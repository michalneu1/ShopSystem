package org.example.order;

import org.example.cart.CartItem;
import org.example.exception.InsufficientStockException;
import org.example.model.Config;
import org.example.model.StatusType;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
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
    private final OrderValidator validator;
    private final Lock lock = new ReentrantLock();


    public ProductManager getManager() {
        return manager;
    }

    public OrderProcessor(ProductManager manager, OrderRepository repository) {
        this.manager = manager;
        this.repository = repository;
        this.validator = new OrderValidator(manager);
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
            validator.validate(order);
            order.getItems().forEach(this::removeFromWarehouse);
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

    private void removeFromWarehouse(CartItem item) {
        manager.removeProductFromWareHouse(item.getProduct().getId(), item.getQuantity());
        item.getChosenConfigs().forEach((config, perUnit) ->
                config.removeStock(perUnit * item.getQuantity()));
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
            appendInvoiceItem(sb, item);
        }
        sb.append("-----------------------------\n");
        sb.append("Suma: ").append(order.getValue()).append("\n");
        if (order.getDiscount().compareTo(BigDecimal.ONE) == 1) {
            sb.append("Rabat: -").append(order.getValue().subtract(discountedValue(order))).append("\n");
        }
        sb.append("DO ZAPLATY: ").append(discountedValue(order)).append("\n");
        sb.append("=============================");
        return sb.toString();
    }

    private void appendInvoiceItem(StringBuilder sb, CartItem item) {
        BigDecimal unitPrice = item.getProduct().getValue();
        sb.append(item.getProduct().getName()).append(" x").append(item.getQuantity())
                .append("   cena jedn.: ").append(item.getProduct().getValue()).append("\n");
        for (Map.Entry<Config, Integer> chosen : item.getChosenConfigs().entrySet()) {
            unitPrice = unitPrice.add(chosen.getKey().getAddValue()
                    .multiply(BigDecimal.valueOf(chosen.getValue())));
            sb.append("    + ").append(chosen.getKey())
                    .append(" x").append(chosen.getValue()).append("/szt\n");
        }
        BigDecimal linePrice = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        sb.append("    razem: ").append(linePrice).append("\n");
    }

    private BigDecimal discountedValue(Order order) {
        return order.getValue()
                .multiply(order.getDiscount())
                .setScale(2, RoundingMode.HALF_UP);
    }
}
