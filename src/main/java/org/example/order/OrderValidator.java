package org.example.order;

import org.example.cart.CartItem;
import org.example.exception.InsufficientStockException;
import org.example.model.Product;
import org.example.warehouse.ProductManager;

/** Checks that the warehouse can fulfil every item of an order before it is processed. */
public class OrderValidator {

    private final ProductManager manager;

    public OrderValidator(ProductManager manager) {
        this.manager = manager;
    }

    public void validate(Order order) {
        order.getItems().forEach(this::validateItem);
    }

    private void validateItem(CartItem item) {
        Product inStock = manager.findByID(item.getProduct().getId())
                .orElseThrow(() -> new InsufficientStockException(
                        item.getProduct().getName(), item.getQuantity()));
        if (inStock.getQuantity() < item.getQuantity()) {
            throw new InsufficientStockException(
                    item.getProduct().getName(), item.getQuantity(), inStock.getQuantity());
        }
        validateConfigStock(inStock, item);
    }

    private void validateConfigStock(Product inStock, CartItem item) {
        item.getChosenConfigs().forEach((config, perUnit) -> {
            int needed = perUnit * item.getQuantity();
            int available = inStock.getConfigStock(config);
            if (available < needed) {
                throw new InsufficientStockException(config.getName(), needed, available);
            }
        });
    }
}
