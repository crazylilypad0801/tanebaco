package com.example.inventory.observer.impl;

import com.example.inventory.model.Product;
import com.example.inventory.observer.InventoryObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
