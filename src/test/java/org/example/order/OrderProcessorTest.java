package org.example.order;

import org.example.cart.CartItem;
import org.example.model.Client;
import org.example.model.Product;
import org.example.model.ProductType;
import org.example.model.StatusType;
import org.example.persistence.OrderRepository;
import org.example.warehouse.ProductManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private OrderProcessor processor;

    private final Client client = new Client("Jan", "Kowalski", (short) 30, "jan@example.com");

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

        assertThat(result.getStatus().getType()).isEqualTo(StatusType.ACCEPTED);
        verify(manager).removeProductFromWareHouse(laptop.getId(), 3);
    }

    @Test
    void processOrderRejectsOrderWhenStockIsTooLow() {
        Product laptop = getProduct("Laptop", 2);
        when(manager.findByID(laptop.getId())).thenReturn(Optional.of(laptop));

        Order result = processor.processOrder(getOrder(laptop, 5));

        assertThat(result.getStatus().getType()).isEqualTo(StatusType.REJECTED);
        assertThat(result.getStatus().getDescription())
                .as("Rejection reason should name the product")
                .contains("Laptop");
        verify(manager, never()).removeProductFromWareHouse(anyInt(), anyInt());
    }

    @Test
    void generateInvoiceContainsOrderDetails() {
        Product laptop = getProduct("Laptop", 10);
        Order order = getOrder(laptop, 3);

        String invoice = processor.generateInvoice(order);

        assertThat(invoice)
                .contains("Nr zamowienia: " + order.getId())
                .contains("Laptop x3")
                .contains("DO ZAPLATY: 300.00");
    }
}
