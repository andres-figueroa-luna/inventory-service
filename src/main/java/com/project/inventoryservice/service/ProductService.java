package com.project.inventoryservice.service;

import com.project.inventoryservice.dto.*;
import com.project.inventoryservice.exception.DuplicateResourceException;
import com.project.inventoryservice.exception.ResourceNotFoundException;
import com.project.inventoryservice.model.Product;
import com.project.inventoryservice.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductCreatedResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product with SKU " + request.sku() + " already exists");
        }

        Product newProduct = new Product(null, request.sku(), request.stock());
        Product saved = productRepository.save(newProduct);

        return new ProductCreatedResponse(saved.getId(), saved.getSku(),
                "Product created successfully");
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));

        return new ProductResponse(product.getId(), product.getSku(), product.getStock());
    }

    public PageResponse<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);

        List<ProductResponse> content = page.getContent().stream()
                .map(p -> new ProductResponse(p.getId(), p.getSku(), p.getStock()))
                .toList();

        return new PageResponse<>(
                content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast()
        );
    }

    @Transactional
    public ProductUpdatedResponse updateStock(Long id, Integer stock) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        product.setStock(stock);

        Product updated = productRepository.save(product);
        return new ProductUpdatedResponse(
                updated.getId(),
                updated.getSku(),
                updated.getStock(),
                "Stock updated successfully");
    }
}
