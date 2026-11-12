package com.project.inventoryservice.function;

import com.project.inventoryservice.dto.OrderCancelledEvent;
import com.project.inventoryservice.dto.OrderCreatedEvent;
import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryConsumerTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StreamBridge streamBridge;

    @InjectMocks
    private InventoryConsumer inventoryConsumer;

    @Test
    void whenProcessOrder_WithSufficientStock_ThenReserveAndPublishEvent() {
        OrderCreatedEvent event = new OrderCreatedEvent(1L, "LAPTOP", 2);
        Product product = new Product(1L, "LAPTOP", 10); // Hay stock

        when(productRepository.findBySku("LAPTOP")).thenReturn(Optional.of(product));

        inventoryConsumer.processOrder().accept(event);

        assertEquals(8, product.getStock());
        verify(productRepository, times(1)).save(product);
        verify(streamBridge, times(1)).send(eq("stockReserved-out-0"), any());
    }

    @Test
    void whenProcessOrder_WithInsufficientStock_ThenPublishRejection() {
        OrderCreatedEvent event = new OrderCreatedEvent(1L, "LAPTOP", 20);
        Product product = new Product(1L, "LAPTOP", 10); // No hay suficiente stock

        when(productRepository.findBySku("LAPTOP")).thenReturn(Optional.of(product));

        inventoryConsumer.processOrder().accept(event);

        verify(productRepository, never()).save(any(Product.class));
        verify(streamBridge, times(1)).send(eq("stockRejected-out-0"), any());
    }

    @Test
    void whenRestoreStock_ThenIncreaseStockAndPublishEvent() {
        OrderCancelledEvent event = new OrderCancelledEvent(1L, "LAPTOP", 2);
        Product product = new Product(1L, "LAPTOP", 10);

        when(productRepository.findBySku("LAPTOP")).thenReturn(Optional.of(product));

        inventoryConsumer.restoreStock().accept(event);

        assertEquals(12, product.getStock()); // Se devolvieron las 2 unidades
        verify(productRepository, times(1)).save(product);
        verify(streamBridge, times(1)).send(eq("stockRestored-out-0"), any());
    }
}
