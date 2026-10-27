package com.example.inventory.observer;

import com.example.inventory.model.Product;

public interface InventoryObserver {
    void onStockUpdated(Product product, int oldQuantity, int newQuantity);

    default void onRfidScanned(Product product, String tagId) {
    }
}
