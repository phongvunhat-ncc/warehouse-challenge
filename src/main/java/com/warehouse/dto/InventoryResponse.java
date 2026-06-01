package com.warehouse.dto;

import com.warehouse.domain.Inventory;

public class InventoryResponse {
    private String sku;
    private Integer totalStock;
    private Integer availableStock;
    private Integer reservedStock;

    public static InventoryResponse fromDomain(Inventory inventory) {
        InventoryResponse dto = new InventoryResponse();
        dto.setSku(inventory.getSku());
        dto.setTotalStock(inventory.getTotalStock());
        dto.setAvailableStock(inventory.getAvailableStock());
        dto.setReservedStock(inventory.getReservedStock());
        return dto;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getTotalStock() { return totalStock; }
    public void setTotalStock(Integer totalStock) { this.totalStock = totalStock; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public Integer getReservedStock() { return reservedStock; }
    public void setReservedStock(Integer reservedStock) { this.reservedStock = reservedStock; }
}