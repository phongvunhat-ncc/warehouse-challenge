package com.warehouse.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ReservationRequest {
    @NotBlank(message = "OrderId is required")
    private String orderId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<ItemRequest> items;

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }
}