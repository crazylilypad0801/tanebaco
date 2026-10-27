package com.example.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// --- ENTITY ---
package com.example.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int quantity;
    private int lowStockThreshold;

    public Product() {}

    public Product(String name, int quantity, int lowStockThreshold) {
        this.name = name;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
}

// --- REPOSITORY ---
package com.example.inventory.repository;

import com.example.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {}

// --- OBSERVER PATTERN INTERFACES ---
package com.example.inventory.observer;

import com.example.inventory.model.Product;

public interface InventoryObserver {
    void onStockUpdated(Product product, int oldQuantity, int newQuantity);
}

public interface InventorySubject {
    void registerObserver(InventoryObserver observer);
    void removeObserver(InventoryObserver observer);
    void notifyObservers(Product product, int oldQuantity, int newQuantity);
}

// --- CONCRETE OBSERVERS ---
package com.example.inventory.observer.impl;

import com.example.inventory.model.Product;
import com.example.inventory.observer.InventoryObserver;
import org.slf.Logger;
import org.slf.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LowStockNotificationObserver implements InventoryObserver {
    private static final Logger log = LoggerFactory.getLogger(LowStockNotificationObserver.class);

    @Override
    public void onStockUpdated(Product product, int oldQuantity, int newQuantity) {
        if (newQuantity <= product.getLowStockThreshold()) {
            log.warn("ALERT: Product '{}' (ID: {}) stock is LOW! Current: {}, Threshold: {}",
                    product.getName(), product.getId(), newQuantity, product.getLowStockThreshold());
        }
    }
}

@Component
public class AuditLogObserver implements InventoryObserver {
    private static final Logger log = LoggerFactory.getLogger(AuditLogObserver.class);

    @Override
    public void onStockUpdated(Product product, int oldQuantity, int newQuantity) {
        log.info("AUDIT LOG: Product '{}' (ID: {}) stock changed from {} to {}.",
                product.getName(), product.getId(), oldQuantity, newQuantity);
    }
}

// --- SERVICE (SUBJECT IMPLEMENTATION) ---
package com.example.inventory.service;

import com.example.inventory.model.Product;
import com.example.inventory.observer.InventoryObserver;
import com.example.inventory.observer.InventorySubject;
import com.example.inventory.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService implements InventorySubject {

    private final ProductRepository productRepository;
    private final List<InventoryObserver> observers = new ArrayList<>();

    @Autowired
    public InventoryService(ProductRepository productRepository, List<InventoryObserver> registeredObservers) {
        this.productRepository = productRepository;
        // Automatically register all Spring beans that implement InventoryObserver
        registeredObservers.forEach(this::registerObserver);
    }

    @Override
    public void registerObserver(InventoryObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(InventoryObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Product product, int oldQuantity, int newQuantity) {
        for (InventoryObserver observer : observers) {
            observer.onStockUpdated(product, oldQuantity, newQuantity);
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateStock(Long id, int newQuantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        int oldQuantity = product.getQuantity();
        product.setQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);

        // Notify registered observers of the state change
        notifyObservers(savedProduct, oldQuantity, newQuantity);

        return savedProduct;
    }
}

// --- CONTROLLER ---
package com.example.inventory.controller;

import com.example.inventory.model.Product;
import com.example.inventory.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return inventoryService.getAllProducts();
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return inventoryService.saveProduct(product);
    }

    @PutMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id, @RequestParam int quantity) {
        return inventoryService.updateStock(id, quantity);
    }
}