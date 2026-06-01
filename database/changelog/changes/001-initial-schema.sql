-- changeset warehouse:1
CREATE TABLE products (
    sku VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE inventory (
    sku VARCHAR(50) PRIMARY KEY,
    total_stock INT NOT NULL DEFAULT 0,
    available_stock INT NOT NULL DEFAULT 0,
    reserved_stock INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0, -- Added for JPA Optimistic Locking
    CONSTRAINT fk_inventory_product FOREIGN KEY (sku) REFERENCES products(sku),
    CONSTRAINT chk_positive_available CHECK (available_stock >= 0),
    CONSTRAINT chk_positive_reserved CHECK (reserved_stock >= 0),
    CONSTRAINT chk_stock_balance CHECK (total_stock = available_stock + reserved_stock)
);

CREATE TABLE reservations (
    id VARCHAR(50) PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- added column
    CONSTRAINT uq_order_id UNIQUE(order_id) -- prevents duplicate orders
);

CREATE TABLE reservation_items (
    reservation_id VARCHAR(50) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (reservation_id, sku),
    CONSTRAINT fk_items_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_items_product FOREIGN KEY (sku) REFERENCES products(sku),
    CONSTRAINT chk_positive_quantity CHECK (quantity > 0)
);

-- Optimize queries on status checking and order mapping
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_order_id ON reservations(order_id);