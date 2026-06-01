-- changeset warehouse:2
INSERT INTO products (sku, name, description) VALUES
('A100', 'Wireless Mouse', 'Ergonomic 2.4Ghz wireless mouse'),
('B200', 'Mechanical Keyboard', 'RGB Backlit mechanical blue-switch keyboard'),
('C300', 'USB-C Hub', 'Multiport adapter with HDMI and USB ports');

INSERT INTO inventory (sku, total_stock, available_stock, reserved_stock, version) VALUES
('A100', 100, 100, 0, 0),
('B200', 50, 50, 0, 0),
('C300', 10, 10, 0, 0);
