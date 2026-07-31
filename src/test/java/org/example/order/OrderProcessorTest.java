package org.example.order;

import org.example.cart.CartItem;
import org.example.model.Client;
import org.example.model.Product;
import org.example.model.ProductType;
import org.example.model.StatusType;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    @Mock
    private ProductManager manager;

    @Mock
    private OrderRepository repository;

    private OrderProcessor processor;
    private Client client;

    @BeforeEach
    void setUp() {
        processor = new OrderProcessor(manager, repository);
        client = new Client("Jan", "Kowalski", (short) 30, "jan@example.com");
    }

    private Product getProduct(String name, int quantityInStock) {
        return new Product(new BigDecimal("100.00"), ProductType.Electronics,
                name, quantityInStock, new ArrayList<>());
    }

    private Order getOrder(Product product, int quantity) {
        BigDecimal value = product.getValue().multiply(BigDecimal.valueOf(quantity));
        return new Order(List.of(new CartItem(product, quantity)), value, client, BigDecimal.ONE);
    }

    @Test
    void processOrderAcceptsOrderWhenStockIsSufficient() {
        Product laptop = getProduct("Laptop", 10);
        when(manager.findByID(laptop.getId())).thenReturn(Optional.of(laptop));

        Order result = processor.processOrder(getOrder(laptop, 3));

        assertEquals(StatusType.ACCEPTED, result.getStatus().getType());
        verify(manager).removeProductFromWareHouse(laptop.getId(), 3);
    }

    @Test
    void processOrderRejectsOrderWhenStockIsTooLow() {
        Product laptop = getProduct("Laptop", 2);
        when(manager.findByID(laptop.getId())).thenReturn(Optional.of(laptop));

        Order result = processor.processOrder(getOrder(laptop, 5));

        assertEquals(StatusType.REJECTED, result.getStatus().getType());
        assertTrue(result.getStatus().getDescription().contains("Laptop"),
                "Rejection reason should name the product, was: "
                        + result.getStatus().getDescription());
        verify(manager, never()).removeProductFromWareHouse(anyInt(), anyInt());
    }

    @Test
    void generateInvoiceContainsOrderDetails() {
        Product laptop = getProduct("Laptop", 10);
        Order order = getOrder(laptop, 3);

        String invoice = processor.generateInvoice(order);

        assertTrue(invoice.contains("Nr zamowienia: " + order.getId()), invoice);
        assertTrue(invoice.contains("Laptop x3"), invoice);
        assertTrue(invoice.contains("DO ZAPLATY: 300.00"), invoice);
    }
}