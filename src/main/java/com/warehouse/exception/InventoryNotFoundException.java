package com.warehouse.exception;

public class InventoryNotFoundException extends ResourceNotFoundException {
    public InventoryNotFoundException(String sku) {
        super("PRODUCT_NOT_FOUND", "No product/inventory found for SKU: " + sku);
    }
}
