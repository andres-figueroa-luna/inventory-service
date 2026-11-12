package com.project.inventoryservice.function;

import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class InventoryConsumerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private ProductRepository productRepository;

    @Test
    void whenOrderCreated_ThenStockIsDecremented() {
        Product product = new Product();
        product.setSku("TEST-INTEGRATION");
        product.setStock(10);
        productRepository.save(product);

        Product productToUpdate = productRepository.findBySku("TEST-INTEGRATION").orElseThrow();
        productToUpdate.setStock(productToUpdate.getStock() - 2);
        productRepository.save(productToUpdate);

        Product updatedProduct = productRepository.findBySku("TEST-INTEGRATION").orElseThrow();
        Assertions.assertEquals(8, updatedProduct.getStock());
    }
}
