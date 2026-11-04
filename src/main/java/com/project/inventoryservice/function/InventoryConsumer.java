package com.project.inventoryservice.function;

import com.project.inventoryservice.dto.*;
import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class InventoryConsumer {

    private final ProductRepository productRepository;
    private final StreamBridge streamBridge;

    public InventoryConsumer(ProductRepository productRepository, StreamBridge streamBridge) {
        this.productRepository = productRepository;
        this.streamBridge = streamBridge;
    }

    @Bean
    public Consumer<OrderCreatedEvent> processOrder() {
        return event -> {
            log.info("Processing order id: {}", event.orderId());

            Optional<Product> productOpt = productRepository.findBySku(event.sku());

            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                if (product.getStock() >= event.quantity()) {
                    product.setStock(product.getStock() - event.quantity());

                    productRepository.save(product);

                    log.info("Stock reserved for: {}", event.sku());

                    streamBridge.send("stockReserved-out-0",
                            new StockReservedEvent(event.orderId(), event.sku(), event.quantity()));
                } else {
                    log.warn("Insufficient stock for: {}", event.sku());

                    streamBridge.send("stockRejected-out-0",
                            new StockRejectedEvent(event.orderId(), event.sku(), "Insufficient stock"));
                }
            } else {
                log.warn("Product not found for: {}", event.sku());

                streamBridge.send("stockRejected-out-0",
                        new StockRejectedEvent(event.orderId(), event.sku(), "Product does not exist"));
            }
        };
    }

    @Bean
    public Consumer<OrderCancelledEvent> restoreStock() {
        return event -> {
            log.info("Restoring stock for cancelled order: {}", event.orderId());

            productRepository.findBySku(event.sku()).ifPresent(product -> {
               product.setStock(product.getStock() + event.quantity());

               productRepository.save(product);
               log.info("Stock restored for product: {}", event.sku());

               streamBridge.send("stockRestored-out-0",
                       new StockRestoredEvent(event.orderId(), event.sku(), event.quantity()));
            });
        };
    }
}
