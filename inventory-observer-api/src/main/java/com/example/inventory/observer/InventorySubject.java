package com.example.inventory.observer;

import com.example.inventory.model.Product;

public interface InventorySubject {
    void registerObserver(InventoryObserver observer);

    void removeObserver(InventoryObserver observer);

    void notifyObservers(Product product, int oldQuantity, int newQuantity);

    void notifyRfidScanned(Product product, String tagId);
}
