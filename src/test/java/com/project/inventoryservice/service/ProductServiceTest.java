package com.project.inventoryservice.service;

import com.project.inventoryservice.dto.ProductCreatedResponse;
import com.project.inventoryservice.dto.ProductRequest;
import com.project.inventoryservice.exception.DuplicateResourceException;
import com.project.inventoryservice.exception.ResourceNotFoundException;
import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void whenCreateProduct_WithNewSku_ThenReturnCreatedResponse() {
        ProductRequest request = new ProductRequest("MONITOR-LG", 10);
        Product savedProduct = new Product(1L, "MONITOR-LG", 10);

        when(productRepository.existsBySku("MONITOR-LG")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductCreatedResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("MONITOR-LG", response.sku());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void whenCreateProduct_WithExistingSku_ThenThrowException() {
        ProductRequest request = new ProductRequest("MONITOR-LG", 10);
        when(productRepository.existsBySku("MONITOR-LG")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> productService.createProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void whenUpdateStock_WithNonExistingProduct_ThenThrowException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateStock(99L, 50));
        verify(productRepository, never()).save(any(Product.class));
    }
}
