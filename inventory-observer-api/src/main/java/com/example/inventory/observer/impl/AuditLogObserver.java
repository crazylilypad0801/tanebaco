package com.example.inventory.observer.impl;

import com.example.inventory.model.Product;
import com.example.inventory.observer.InventoryObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogObserver implements InventoryObserver {
    private static final Logger log = LoggerFactory.getLogger(AuditLogObserver.class);

    @Override
    public void onStockUpdated(Product product, int oldQuantity, int newQuantity) {
        log.info("AUDIT: Product '{}' (ID: {}) stock changed from {} to {}.",
                product.getName(), product.getId(), oldQuantity, newQuantity);
    }

    @Override
    public void onRfidScanned(Product product, String tagId) {
        log.info("AUDIT: RFID tag '{}' scanned for product '{}' (ID: {}).",
                tagId, product.getName(), product.getId());
    }
}
