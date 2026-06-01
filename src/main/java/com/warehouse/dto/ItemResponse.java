package com.warehouse.dto;

import com.warehouse.domain.ReservationItem;

public class ItemResponse {
    private String sku;
    private Integer quantity;

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}