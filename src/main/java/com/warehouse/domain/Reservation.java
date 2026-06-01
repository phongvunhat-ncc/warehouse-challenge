package com.warehouse.domain;

import com.warehouse.domain.state.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ReservationItem> items = new ArrayList<>();

    @Transient
    private ReservationState state;

    public Reservation() {}

    @PostLoad
    protected void initInternalState() {
        switch (this.status) {
            case PENDING -> this.state = new PendingState();
            case CONFIRMED -> this.state = new ConfirmedState();
            case CANCELLED -> this.state = new CancelledState();
        }
    }

    public void transitionTo(ReservationState newState) {
        this.state = newState;
        this.status = newState.getStatus();
    }

    public void confirm() {
        if (state == null) { initInternalState(); }
        state.confirm(this);
    }

    public void cancel() {
        if (state == null) { initInternalState(); }
        state.cancel(this);
    }

    public void addItem(ReservationItem item) {
        items.add(item);
        item.setReservation(this);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ReservationItem> getItems() { return items; }
}