package com.example.inventory.controller;

import com.example.inventory.model.Product;
import com.example.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory/rfid-scans")
public class RfidScanController {
    private final InventoryService inventoryService;

    public RfidScanController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public Product scan(@RequestBody RfidScanRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RFID scan body is required");
        }
        return inventoryService.processRfidScan(request.tagId());
    }

    public record RfidScanRequest(String tagId) {
    }
}
