package com.warehouse.dto;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationResponse {
    private String id;
    private String orderId;
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private List<ItemResponse> items;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ItemResponse> getItems() { return items; }
    public void setItems(List<ItemResponse> items) { this.items = items; }
}