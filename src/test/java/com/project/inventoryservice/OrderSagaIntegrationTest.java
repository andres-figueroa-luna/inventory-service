package com.project.inventoryservice;

import com.project.inventoryservice.dto.OrderCreatedEvent;
import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Testcontainers
class OrderSagaIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"));

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("apache/kafka:latest"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.bindings.processOrder-in-0.consumer.startOffset",
                () -> "earliest");
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void crateTopic() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());

        try (AdminClient adminClient = AdminClient.create(props)) {
            NewTopic newTopic = new NewTopic("orders-topic", 1, (short) 1);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();

            System.out.println("✅ Topic 'orders-topic' created successfully");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void whenCreatedEventIsSent_ThenStockIsDecremented() throws Exception {
        Product product = new Product();
        product.setSku("SAGA-TEST-001");
        product.setStock(10);

        productRepository.save(product);

        OrderCreatedEvent event = new OrderCreatedEvent(
                100L, "SAGA-TEST-001", 2);
        String jsonPayload = objectMapper.writeValueAsString(event);

        System.out.println("⏳ Waiting for consumer to subscribe...");
        Thread.sleep(5000);

        System.out.println("🚀 Sending event to Kafka...");
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(props);
        KafkaTemplate<String, String> localKafkaTemplate = new KafkaTemplate<>(producerFactory);

        localKafkaTemplate.send("orders-topic", jsonPayload);

        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                   Product updatedProduct = productRepository.findBySku("SAGA-TEST-001").orElseThrow();

                   Assertions.assertEquals(
                           8,
                           updatedProduct.getStock(),
                           "Stock should be updated");
                });
    }
}
