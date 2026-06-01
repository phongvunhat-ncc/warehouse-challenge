# Warehouse Inventory Reservation System

A production-ready Spring Boot microservice designed to process concurrent warehouse inventory reservations. The system enforces relational integrity and prevents product overselling through database-level isolation under parallel workloads.

---

## 1. Why Challenge 1 Was Chosen
Managing shared state under concurrency is a fundamental challenge in enterprise systems. Implementing Challenge 1 allows us to demonstrate how to build a highly consistent system using standard relational database transactions, explicit locking patterns, and domain-level state machine designs.

---

## 2. Architecture Overview
This application follows a **Clean Domain-Driven Architecture**. By decoupling domain entities and business rules from persistence schemas, controllers, and transport objects, we establish a robust structure that is easily testable and maintainable.

```text
src/main/java/com/warehouse
├── controller        # REST controllers handling HTTP transport and response wrappers
├── dto               # Data Transfer Objects capturing strict API requests and responses
├── exception         # Custom domain exceptions and global HTTP exception mappings
├── repository        # Spring Data JPA repositories handling database communication
├── service           # Application orchestrator managing transactions and locking sequences
└── domain            # Pure business domain entities, independent of frameworks
    ├── factory       # Creation logic for system aggregates (Factory Pattern)
    └── state         # Reservation lifecycle transitions and constraints (State Pattern)
```

---

## 3. Database Design

The schema is built to isolate available, reserved, and total stock. This design ensures that we can lock and manipulate stock balances safely without losing track of physical quantities before shipping.

### Entity Relationship Diagram (ERD)

```text
  +------------------+                   +------------------+
  |     products     |                   |    inventory     |
  +------------------+                   +------------------+
  | PK  sku (VARCHAR)| <---------------+ | PK  sku (VARCHAR)|
  |     name         |                   |     total_stock  |
  |     description  |                   |   available_stock|
  +------------------+                   |   reserved_stock |
           |                             +------------------+
           | 1
           |
           | 0..*
  +-------------------------+            +------------------+
  |    reservation_items    |            |   reservations   |
  +-------------------------+            +------------------+
  | PK,FK1  reservation_id  | ---------> | PK  id (VARCHAR) |
  | PK,FK2  sku (VARCHAR)   |            |     order_id     |
  |         quantity        |            |     status       |
  +-------------------------+            |     created_at   |
                                         +------------------+
```

### Table Definitions & Integrity Constraints

#### `products`
Stores product-specific static details.
*   `sku` (VARCHAR(50), Primary Key): Unique alphanumeric product identifier.
*   `name` (VARCHAR(255), Not Null): The name of the product.
*   `description` (TEXT): Detailed description of the product.

#### `inventory`
Stores real-time stock balances per SKU.
*   `sku` (VARCHAR(50), Primary Key, Foreign Key -> `products.sku`)
*   `total_stock` (INT, Default 0): Total physically present units.
*   `available_stock` (INT, Default 0): Units available for purchase.
*   `reserved_stock` (INT, Default 0): Units temporarily allocated to pending reservations.
*   **Check Constraints**:
    *   `chk_positive_available`: `available_stock >= 0` (Prevents overselling at the database engine level)
    *   `chk_positive_reserved`: `reserved_stock >= 0`
    *   `chk_stock_balance`: `total_stock = available_stock + reserved_stock` (Ensures mathematical balance consistency)

#### `reservations`
Tracks the aggregate lifecycle of an order's reservation.
*   `id` (VARCHAR(50), Primary Key): Business-keyed identifier (e.g., `RES-F93A1B28`).
*   `order_id` (VARCHAR(100), Not Null): Associated client order identifier.
*   `status` (VARCHAR(20), Not Null): Current lifecycle status (`PENDING`, `CONFIRMED`, `CANCELLED`).
*   `created_at` (TIMESTAMP, Not Null): Timestamp when the reservation was created.

#### `reservation_items`
Composite table mapping reservation items.
*   `reservation_id` (VARCHAR(50), Primary Key, Foreign Key -> `reservations.id`)
*   `sku` (VARCHAR(50), Primary Key, Foreign Key -> `products.sku`)
*   `quantity` (INT, Not Null): Number of reserved items.
*   **Check Constraints**:
    *   `chk_positive_quantity`: `quantity > 0`

---

## 4. REST API Endpoints

All success and error responses are returned inside a standard JSON wrapper to ensure a consistent experience for API clients.

### Success Wrapper Format
```json
{
  "data": { ... },
  "error": null
}
```

### Error Wrapper Format
```json
{
  "data": null,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "SKU A100 has only 30 units available, 50 were requested"
  }
}
```

---

### Endpoint Reference

#### 1. Reserve Inventory
Create a temporary `PENDING` reservation and reduce available stock.

*   **URL**: `/api/v1/reservations`
*   **Method**: `POST`
*   **Headers**: `Content-Type: application/json`
*   **Request Payload**:
```json
{
  "orderId": "ORD-1001",
  "items": [
    { "sku": "A100", "quantity": 5 },
    { "sku": "B200", "quantity": 3 }
  ]
}
```
*   **Response (200 OK)**:
```json
{
  "data": {
    "id": "RES-8A7F2D1B",
    "orderId": "ORD-1001",
    "status": "PENDING",
    "createdAt": "2024-04-12T14:30:00",
    "items": [
      { "sku": "A100", "quantity": 5 },
      { "sku": "B200", "quantity": 3 }
    ]
  },
  "error": null
}
```

#### 2. Confirm a Reservation
Transition a reservation from `PENDING` to `CONFIRMED`, converting reserved stock into a permanent deduction.

*   **URL**: `/api/v1/reservations/{id}/confirm`
*   **Method**: `POST`
*   **Response (200 OK)**:
```json
{
  "data": {
    "id": "RES-8A7F2D1B",
    "orderId": "ORD-1001",
    "status": "CONFIRMED",
    "createdAt": "2024-04-12T14:30:00",
    "items": [
      { "sku": "A100", "quantity": 5 },
      { "sku": "B200", "quantity": 3 }
    ]
  },
  "error": null
}
```

#### 3. Cancel a Reservation
Cancel a `PENDING` reservation, releasing the reserved stock back to `available_stock`.

*   **URL**: `/api/v1/reservations/{id}/cancel`
*   **Method**: `POST`
*   **Response (200 OK)**:
```json
{
  "data": {
    "id": "RES-8A7F2D1B",
    "orderId": "ORD-1001",
    "status": "CANCELLED",
    "createdAt": "2024-04-12T14:30:00",
    "items": [
      { "sku": "A100", "quantity": 5 },
      { "sku": "B200", "quantity": 3 }
    ]
  },
  "error": null
}
```

#### 4. Get a Reservation
Retrieve the details of a specific reservation.

*   **URL**: `/api/v1/reservations/{id}`
*   **Method**: `GET`
*   **Response (200 OK)**:
```json
{
  "data": {
    "id": "RES-8A7F2D1B",
    "orderId": "ORD-1001",
    "status": "PENDING",
    "createdAt": "2024-04-12T14:30:00",
    "items": [
      { "sku": "A100", "quantity": 5 }
    ]
  },
  "error": null
}
```

#### 5. Get Current Stock for a SKU
Retrieve the stock levels for a given product.

*   **URL**: `/api/v1/inventory/{sku}`
*   **Method**: `GET`
*   **Response (200 OK)**:
```json
{
  "data": {
    "sku": "A100",
    "totalStock": 100,
    "availableStock": 95,
    "reservedStock": 5
  },
  "error": null
}
```

---

## 5. Design Patterns Applied

### Pattern 1: State Pattern (Reservation Lifecycle transitions)
The `Reservation` entity delegates behavior to a polymorphic `ReservationState` reference. This isolates state-specific validation rules (such as blocking cancellations on already-confirmed reservations) inside clean, dedicated classes.

#### State Machine Flowchart
```text
                 +-------------------+
                 |      PENDING      |
                 +-------------------+
                   /               \
                  /                 \
         confirm()                   cancel()
                /                     \
               v                       v
      +-----------------+     +-----------------+
      |    CONFIRMED    |     |    CANCELLED    |
      +-----------------+     +-----------------+
      | Terminal State  |     | Terminal State  |
      |   No changes    |     |   No changes    |
      +-----------------+     +-----------------+
```

#### Code Structure Class Diagram
```text
            +-------------------------+
            | <<interface>>           |
            | ReservationState        |
            +-------------------------+
            | + confirm(r: Reservation) |
            | + cancel(r: Reservation)  |
            | + getStatus(): Status   |
            +-------------------------+
             ^           ^           ^
            /            |            \
           /             |             \
  +--------------+ +---------------+ +---------------+
  | PendingState | | ConfirmedState| | CancelledState|
  +--------------+ +---------------+ +---------------+
```
*   **File References:**
    *   Interface: `com.warehouse.domain.state.ReservationState`
    *   Concrete states: `PendingState`, `ConfirmedState`, `CancelledState`
    *   Context Class: `com.warehouse.domain.Reservation` (methods `confirm()` and `cancel()`)

---

### Pattern 2: Factory Pattern (Reservation Creation)
The `ReservationFactory` encapsulates the logic for initializing a fresh `Reservation` aggregate. This separates the entity's constructor from the business rules around initialization (such as ID generation, date stamping, and setting the default state).

```text
  +-------------------------+
  |   ReservationRequest    |
  +-------------------------+
               |
               | provides Order ID & Item list
               v
  +-------------------------+        instantiates        +------------------------+
  |   ReservationFactory    | -------------------------> |      Reservation       |
  +-------------------------+                            +------------------------+
  | + createPendingRes(...) |                            | - id: "RES-8A7F2D1B"   |
  +-------------------------+                            | - status: PENDING      |
                                                         | - items: mapped        |
                                                         +------------------------+
```
*   **File References:**
    *   `com.warehouse.domain.factory.ReservationFactory`

---

## 6. SOLID Principles Applied

The following object-oriented principles are visible across the codebase:

*   **Single Responsibility Principle (SRP):** 
    Each element focuses strictly on one core concern. `ReservationMapper` transforms payloads between representations, `Reservation` entities evaluate invariant operations, and `ReservationController` translates transport commands.
*   **Open/Closed Principle (OCP):** 
    Extending systems behavior does not require altering established code. Introducing alternative states (such as `EXPIRED`) is accomplished by adding implementations of `ReservationState` without modifying the context class.
*   **Liskov Substitution Principle (LSP):** 
    Subclasses must be substitutable for their superclasses. `PendingState`, `ConfirmedState`, and `CancelledState` adhere to this contract; they implement the interface signature without breaking its semantic behavior.
*   **Interface Segregation Principle (ISP):** 
    The concrete repository `ReservationRepository` inherits from targeted, small JPA interfaces, preventing the application service layer from accessing internal database operations.
*   **Dependency Inversion Principle (DIP):** 
    High-level business coordinators depend strictly on abstract interface layers. `ReservationController` relies on `ReservationService`, which resolves to `ReservationServiceImpl` dynamically at runtime via Spring Dependency Injection.

---

## 7. Trade-offs & Improvements

### Concurrency Strategy: Pessimistic Locking (`PESSIMISTIC_WRITE`)
To prevent overselling, the application locks matching `inventory` rows during reservation requests (`SELECT ... FOR UPDATE`).

*   **Trade-off (Consistency over Latency):** 
    Using pessimistic locks ensures strong data consistency directly in the database, preventing race conditions. However, parallel requests for the same SKU are forced to queue, which can increase API response times under high load.
*   **Deadlock Prevention:** 
    To prevent deadlocks, the list of requested SKUs is always sorted alphabetically before acquiring locks. This ensures all transactions request locks in the exact same order (e.g., locking SKU `A100` before SKU `B200`), eliminating circular wait conditions.

### Architectural Improvements with More Time:
1.  **Asynchronous Expiring Locks:** 
    Instead of keeping database locks open, we could implement a local Redis lock with a Time-To-Live (TTL). This would allow us to hold temporary inventory allocations in memory, releasing them automatically if the order is not confirmed within 15 minutes.
2.  **Archiving Completed Reservations:** 
    As the `reservations` table grows, query performance can degrade. We could implement an asynchronous archiving service to move `CONFIRMED` and `CANCELLED` reservations over 30 days old into a separate historical database.

---

## 8. What Would Break at Scale & How to Fix It

### 1. Database Connection Pool Starvation (Hot SKU Contention)
*   **The Problem:** 
    If thousands of customers try to buy a single highly popular product at the same time, they will all try to acquire a pessimistic lock on the same database row. This causes database connection pool threads to block, leading to connection timeouts and application-wide slow response times.
*   **The Fix (In-Memory Pre-filtering):** 
    We can use Redis to maintain a real-time count of available stock. Reservation requests are decremented atomically in Redis first using Lua scripts. If Redis indicates there is enough stock, the request is placed in a message queue (e.g., RabbitMQ or Kafka) to write to the database asynchronously. If Redis indicates a SKU is out of stock, the request is rejected immediately without hitting the database.

### 2. High Lock Latency with Large Orders
*   **The Problem:** 
    Orders containing many different SKUs must lock a larger number of rows. This increases the transaction processing time and blocks other transactions trying to access any of those same items.
*   **The Fix (Inventory Sharding / Partitioning):** 
    We can shard the database tables horizontally by SKU category or split inventory into multiple virtual pools. This ensures that transactions for different product categories are processed by separate database nodes, distributing the locking overhead.

### 3. Read-Write Query Contention
*   **The Problem:** 
    Running analytics reports or looking up reservation histories shares the same database pool as active checkout processes, slowing down inventory reservation writes.
*   **The Fix (Read/Write Segregation):** 
    We can split database traffic into a primary instance for writes (reservations, confirmations, cancellations) and one or more read replicas for lookups (`GET /api/v1/inventory/{sku}` and query reporting).

---

## 9. Running the System

### Prerequisites
*   Docker and Docker Compose installed and running.
*   Port `8080` (API) and Port `5435` (Database) must be free on your host machine.

### Launching the Application
Run the following command from the root directory:
```bash
docker compose down -v
docker compose up --build
```

The system will start up, run the database migrations automatically, and begin listening for API requests at `http://localhost:8080`.

---

## 10. Running the Tests

### Unit Tests
These tests run in isolation using Mockito, without launching a Spring Boot or database context:
```bash
mvn test
```

### Integration Tests
These tests run against a real PostgreSQL database inside a temporary Docker container using **Testcontainers**:
```bash
mvn test -Dtest=*IntegrationTest
```
