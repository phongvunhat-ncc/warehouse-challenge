package com.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ItemRequest {
    @NotBlank(message = "SKU is required")
    private String sku;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}