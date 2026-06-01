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
                           CONSTRAINT fk_inventory_product FOREIGN KEY (sku) REFERENCES products(sku),
                           CONSTRAINT chk_positive_available CHECK (available_stock >= 0),
                           CONSTRAINT chk_positive_reserved CHECK (reserved_stock >= 0),
                           CONSTRAINT chk_stock_balance CHECK (total_stock = available_stock + reserved_stock)
);

CREATE TABLE reservations (
                              id VARCHAR(50) PRIMARY KEY,
                              order_id VARCHAR(100) NOT NULL,
                              status VARCHAR(20) NOT NULL,
                              created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
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

-- Seed Initial Data
INSERT INTO products (sku, name, description) VALUES
                                                  ('A100', 'Wireless Mouse', 'Ergonomic 2.4Ghz wireless mouse'),
                                                  ('B200', 'Mechanical Keyboard', 'RGB Backlit mechanical blue-switch keyboard'),
                                                  ('C300', 'USB-C Hub', 'Multiport adapter with HDMI and USB ports');

INSERT INTO inventory (sku, total_stock, available_stock, reserved_stock) VALUES
                                                                              ('A100', 100, 100, 0),
                                                                              ('B200', 50, 50, 0),
                                                                              ('C300', 10, 10, 0);