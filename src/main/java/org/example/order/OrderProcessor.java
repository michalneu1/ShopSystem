package org.example.order;

import org.example.cart.CartItem;
import org.example.model.Config;
import org.example.warehouse.ProductManager;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class OrderProcessor {
    private static final DateTimeFormatter INVOICE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    ProductManager manager;

    public ProductManager getManager() {
        return manager;
    }

    public OrderProcessor(ProductManager manager) {
        this.manager = manager;
    }

    public void processOrder(Order order) {
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
        System.out.println(generateInvoice(order));
    }

    public String generateInvoice(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== FAKTURA ==========\n");
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
