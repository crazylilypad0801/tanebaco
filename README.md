# Inventory API with Observer Pattern

A Java Spring Boot REST API demonstrating the **Observer Design Pattern** for managing an inventory system with a PostgreSQL database.

The runnable Maven project is in [`inventory-observer-api/`](./inventory-observer-api/). It includes an RFID scan endpoint for reader integrations that can send decoded tag IDs over HTTP.

## 🏗️ Architecture & Design Pattern

The application implements the **Observer Design Pattern** to automatically decouple event generation (stock changes, low stock events) from event consumption (notifications, auditing, logging).

- **Subject (Publisher):** `InventorySubject` / `InventoryService`
- **Observer (Subscriber):** `InventoryObserver` interface, subscribed to stock updates and RFID scan events
- **Concrete Observers:**
  - `LowStockNotificationObserver`: Triggers alert notifications when product stock falls below a defined threshold.
  - `AuditLogObserver`: Records every inventory change into an audit log for tracking and compliance.

## 🛠️ Tech Stack

- **Java:** 17
- **Framework:** Spring Boot 3.x (Spring Data JPA, Spring Web)
- **Database:** PostgreSQL 15
- **Containerization:** Docker & Docker Compose

## 🚀 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/get-started) and Docker Compose installed.

### Running with Docker Compose

1. Clone or download the repository files.
2. Build and start the containers:
   ```bash
   docker-compose up --build -d
   ```
3. The API will be available at `http://localhost:8080`.
4. PostgreSQL will be accessible on port `5432`.

### Stopping the Application

```bash
docker-compose down -v
```

## 📡 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/inventory` | List all inventory items |
| `POST` | `/api/inventory` | Create a new inventory item (optionally include `rfidTagId`) |
| `PUT` | `/api/inventory/{id}/stock` | Update item stock quantity (triggers Observers) |
| `POST` | `/api/inventory/rfid-scans` | Resolve a scanned RFID tag and notify Observers |

RFID scan requests use JSON such as `{"tagId":"E2000017221101441890ABCD"}`. Register the tag on a product as `rfidTagId` when creating it. A scan identifies the product and publishes an event; it does not change stock automatically. The reader or its vendor bridge must submit the decoded UID to the endpoint.

## 🧪 Quick Test

**1. Create a Product:**
```bash
curl -X POST http://localhost:8080/api/inventory \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptop", "quantity": 15, "lowStockThreshold": 5}'
```

**2. Update Stock (Triggering Observers):**
```bash
curl -X PUT http://localhost:8080/api/inventory/1/stock?quantity=3
```
*Observe the application logs to see `AuditLogObserver` log the change and `LowStockNotificationObserver` trigger a low stock alert!*