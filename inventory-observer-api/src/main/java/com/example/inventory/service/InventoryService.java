package com.example.inventory.service;

import com.example.inventory.model.Product;
import com.example.inventory.observer.InventoryObserver;
import com.example.inventory.observer.InventorySubject;
import com.example.inventory.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InventoryService implements InventorySubject {
    private final ProductRepository productRepository;
    private final List<InventoryObserver> observers = new CopyOnWriteArrayList<>();

    public InventoryService(ProductRepository productRepository, List<InventoryObserver> registeredObservers) {
        this.productRepository = productRepository;
        registeredObservers.forEach(this::registerObserver);
    }

    @Override
    public void registerObserver(InventoryObserver observer) {
        observers.addIfAbsent(observer);
    }

    @Override
    public void removeObserver(InventoryObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Product product, int oldQuantity, int newQuantity) {
        observers.forEach(observer -> observer.onStockUpdated(product, oldQuantity, newQuantity));
    }

    @Override
    public void notifyRfidScanned(Product product, String tagId) {
        observers.forEach(observer -> observer.onRfidScanned(product, tagId));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product saveProduct(Product product) {
        if (product.getQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateStock(Long id, int newQuantity) {
        if (newQuantity < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be negative");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product not found with id: " + id));

        int oldQuantity = product.getQuantity();
        product.setQuantity(newQuantity);
        Product savedProduct = productRepository.save(product);
        notifyObservers(savedProduct, oldQuantity, newQuantity);
        return savedProduct;
    }

    @Transactional
    public Product processRfidScan(String tagId) {
        if (tagId == null || tagId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RFID tagId must not be blank");
        }

        String normalizedTagId = tagId.strip().toUpperCase(Locale.ROOT);
        Product product = productRepository.findByRfidTagIdIgnoreCase(normalizedTagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No product is registered for RFID tag: " + normalizedTagId));

        notifyRfidScanned(product, normalizedTagId);
        return product;
    }
}
