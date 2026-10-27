```
inventory-observer-api/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── example/
    │   │           └── inventory/
    │   │               ├── Application.java
    │   │               ├── controller/
    │   │               │   ├── InventoryController.java
    │   │               │   └── RfidScanController.java
    │   │               ├── model/
    │   │               │   └── Product.java
    │   │               ├── observer/
    │   │               │   ├── InventoryObserver.java
    │   │               │   ├── InventorySubject.java
    │   │               │   └── impl/
    │   │               │       ├── AuditLogObserver.java
    │   │               │       └── LowStockNotificationObserver.java
    │   │               ├── repository/
    │   │               │   └── ProductRepository.java
    │   │               └── service/
    │   │                   └── InventoryService.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
            └── com/
                └── example/
                    └── inventory/
                        └── ApplicationTests.java
```

### Key Package Breakdown

* **`com.example.inventory.observer`**: Holds the core Observer pattern interfaces (`InventoryObserver` and `InventorySubject`). Subscribers receive stock updates and RFID scan notifications.
* **`com.example.inventory.observer.impl`**: Contains the concrete observer implementations (`AuditLogObserver` and `LowStockNotificationObserver`).
* **`com.example.inventory.service`**: Contains `InventoryService`, the concrete **Subject** that manages observers and publishes stock and RFID scan events.
* **`com.example.inventory.controller.RfidScanController`**: Accepts reader/bridge scan events at `POST /api/inventory/rfid-scans`; tag IDs identify products by their registered `rfidTagId`.
* **RFID behavior**: Scanning resolves the product and publishes an event; it does not decrement stock automatically.
* **Root Configuration & Containerization**: `Dockerfile`, `docker-compose.yml`, and `pom.xml` sit at the project root level alongside `README.md`.