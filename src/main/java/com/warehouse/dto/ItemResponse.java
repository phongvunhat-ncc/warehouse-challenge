package com.warehouse.dto;

import com.warehouse.domain.ReservationItem;

public class ItemResponse {
    private String sku;
    private Integer quantity;

    public static ItemResponse fromDomain(ReservationItem item) {
        ItemResponse dto = new ItemResponse();
        dto.setSku(item.getSku());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}