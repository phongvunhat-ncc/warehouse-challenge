package com.warehouse.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    private String sku;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    @Column(name = "available_stock", nullable = false)
    private Integer availableStock;

    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock;

    @Version
    private Long version;

    public Inventory() {}

    public Inventory(String sku, Integer totalStock, Integer availableStock, Integer reservedStock) {
        this.sku = sku;
        this.totalStock = totalStock;
        this.availableStock = availableStock;
        this.reservedStock = reservedStock;
    }

    public void reserve(int quantity) {
        if (this.availableStock < quantity) {
            throw new IllegalArgumentException(
                    String.format("SKU %s has only %d units available, %d were requested", sku, availableStock, quantity)
            );
        }
        this.availableStock -= quantity;
        this.reservedStock += quantity;
    }

    public void release(int quantity) {
        if (this.reservedStock < quantity) {
            throw new IllegalArgumentException(
                    String.format("Cannot release %d units for SKU %s, only %d units are currently reserved", quantity, sku, reservedStock)
            );
        }
        this.reservedStock -= quantity;
        this.availableStock += quantity;
    }

    public void confirm(int quantity) {
        if (this.reservedStock < quantity) {
            throw new IllegalArgumentException(
                    String.format("Cannot confirm %d units for SKU %s, only %d units are currently reserved", quantity, sku, reservedStock)
            );
        }
        this.reservedStock -= quantity;
        this.totalStock -= quantity;
    }

    // Getters and Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getTotalStock() { return totalStock; }
    public Integer getAvailableStock() { return availableStock; }
    public Integer getReservedStock() { return reservedStock; }
    public Long getVersion() { return version; }
}