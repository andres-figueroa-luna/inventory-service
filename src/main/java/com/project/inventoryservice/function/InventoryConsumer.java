package com.project.inventoryservice.function;

import com.project.inventoryservice.dto.OrderCreatedEvent;
import com.project.inventoryservice.dto.StockRejectedEvent;
import com.project.inventoryservice.dto.StockReservedEvent;
import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
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

    public Consumer<OrderCreatedEvent> processOrder() {
        return event -> {
            log.info("Processing order id: {}", event.orderId());

            Optional<Product> productOpt = productRepository.findById(event.productId());

            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                if (product.getStock() >= event.quantity()) {
                    product.setStock(product.getStock() - event.quantity());

                    productRepository.save(product);

                    log.info("Stock reserved for: {}", event.productId());

                    streamBridge.send("stockReserved-out-0",
                            new StockReservedEvent(event.orderId(), event.productId(), event.quantity()));
                } else {
                    log.warn("Insufficient stock for: {}", event.productId());

                    streamBridge.send("stockRejected-out-0",
                            new StockRejectedEvent(event.orderId(), event.productId(), "Insufficient stock"));
                }
            } else {
                log.warn("Product not found for: {}", event.productId());

                streamBridge.send("stockRejected-out-0",
                        new StockRejectedEvent(event.orderId(), event.productId(), "Product does not exist"));
            }
        };
    }
}
