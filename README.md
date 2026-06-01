# Warehouse Inventory Reservation System

A production-ready Spring Boot microservice designed to handle concurrent inventory reservations. It ensures stock consistency under parallel transaction requests, preventing overselling or deadlocks.

---

## 1. Why Challenge 1 Was Chosen
This challenge was chosen because resource locking and distributed data consistency are fundamental challenges in high-throughput enterprise architectures. Correctly managing concurrent state transitions and protecting relational integrity under high thread contention allows us to demonstrate clean architecture alongside database isolation patterns.

---

## 2. Architecture Overview
This application follows a **Clean Domain-Driven Architecture**. By segregating business domain models from persistence schemas and controllers, logic layers are decoupled and easier to mock:

*   **Domain layer** (`com.warehouse.domain`): Completely decoupled from outer orchestration. Contains domain invariants, state behaviors, and factories.
*   **Infrastructure layer** (`com.warehouse.repository`, `db/changelog`): Manages relational mappings and schema migrations.
*   **Service Application layer** (`com.warehouse.service`): Coordinates transactional mutations, locks database items, and enforces business flow execution.
*   **Presentation layer** (`com.warehouse.controller`, `com.warehouse.dto`): Translates REST commands and encapsulates outputs inside uniform response envelopes.

---

## 3. Design Patterns Applied

### State Pattern
*   **Where it appears:** Inside `com.warehouse.domain.state` (`ReservationState`, `PendingState`, `ConfirmedState`, `CancelledState`) and integrated dynamically inside `Reservation`.
*   **Reasoning:** Reservation states undergo transition restrictions (`PENDING -> CONFIRMED`, `PENDING -> CANCELLED`). Hardcoded condition blocks in service layers tend to grow convoluted. The State Pattern isolates status changes into separate behavior strategies, rejecting invalid transitions through standard exception models.

### Factory Pattern
*   **Where it appears:** Inside `com.warehouse.domain.factory.ReservationFactory`.
*   **Reasoning:** Instantiating complex entities like `Reservation` requires setting initial dates, validating inputs, generating business IDs, and binding internal item aggregates. The Factory isolates these initial construction routines, keeping the core domain clean.

---

## 4. SOLID Principles Observed

*   **Single Responsibility Principle (SRP):**
    Each class focuses on a single task. For example, `ReservationController` only parses HTTP inputs, `ReservationState` subclasses handle state changes, and `ReservationService` coordinates transactions.
*   **Open/Closed Principle (OCP):**
    New reservation states (such as `EXPIRED` or `BACKORDERED`) can be introduced by implementing `ReservationState` without modifying existing state transition classes.
*   **Liskov Substitution Principle (LSP):**
    Subclasses of `ReservationState` (e.g. `ConfirmedState`) can substitute the base interface seamlessly without altering the validation flow in the domain.
*   **Interface Segregation Principle (ISP):**
    `InventoryRepository` uses targeted, refined query definitions to avoid exposing unnecessary operations.
*   **Dependency Inversion Principle (DIP):**
    `ReservationService` relies on high-level abstractions like JPA repository interfaces, rather than direct database driver implementations.

---

## 5. Database Schema Decisions
*   **Inventory Isolation:** The `inventory` table separates `total_stock`, `available_stock`, and `reserved_stock`. This allows the application to capture reserved items without actually shipping them, supporting transitions to `CONFIRMED` or `CANCELLED`.
*   **DB Constraints:** We added `CHECK` constraints on `available_stock >= 0`, `reserved_stock >= 0`, and `total_stock = available_stock + reserved_stock`. These constraints act as a final layer of protection to keep stock calculations correct.

---

## 6. How to Run the System

To build and start the application and database containers:

```bash
docker compose up --build