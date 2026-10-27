# Inventory Observer API

A Java 17 Spring Boot API demonstrating the Observer pattern for inventory updates and RFID scans. PostgreSQL stores products and their associated RFID tag IDs.

## Architecture

- **Publisher:** `InventoryService` implements `InventorySubject`.
- **Subscriber contract:** `InventoryObserver` receives stock-update and RFID-scan events.
- **Subscribers:** `AuditLogObserver` logs both event types; `LowStockNotificationObserver` logs low-stock alerts.
- **RFID reader integration:** `POST /api/inventory/rfid-scans` accepts a tag ID from an RFID reader bridge or application. The reader protocol is not tied to a specific vendor or hardware model.

An RFID scan identifies a product and notifies subscribers. It does **not** change stock quantity; stock changes remain explicit through the stock endpoint.

## Run with Docker Compose

From this directory, build and start the API and PostgreSQL:

```bash
docker compose up --build -d
```

The API is available at `http://localhost:8080`. PostgreSQL is exposed on port `5432`.

## API

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/inventory` | List all products |
| `POST` | `/api/inventory` | Create a product; optionally provide `rfidTagId` |
| `PUT` | `/api/inventory/{id}/stock?quantity={quantity}` | Set a product's stock and notify observers |
| `POST` | `/api/inventory/rfid-scans` | Resolve an RFID tag and notify observers |

Create a product with its tag ID:

```http
POST /api/inventory
Content-Type: application/json

{
  "name": "Laptop",
  "quantity": 15,
  "lowStockThreshold": 5,
  "rfidTagId": "E2000017221101441890ABCD"
}
```

Submit a scan from the reader integration:

```http
POST /api/inventory/rfid-scans
Content-Type: application/json

{
  "tagId": "E2000017221101441890ABCD"
}
```

The tag ID is trimmed and matched case-insensitively. A blank or unknown tag returns an HTTP error rather than a success-shaped response. The scan endpoint returns the matched product and emits an RFID scan event to registered observers.

> Connect the RFID reader or its vendor bridge to submit each decoded tag ID to this endpoint. Direct serial/USB reader support is hardware-specific and is intentionally kept outside the core application.
