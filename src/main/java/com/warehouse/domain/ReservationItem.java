package com.warehouse.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "reservation_items")
@IdClass(ReservationItemId.class)
public class ReservationItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Id
    @Column(name = "sku")
    private String sku;

    @Column(nullable = false)
    private Integer quantity;

    public ReservationItem() {}

    public ReservationItem(String sku, Integer quantity) {
        this.sku = sku;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

class ReservationItemId implements Serializable {
    private String reservation;
    private String sku;

    public ReservationItemId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReservationItemId that)) return false;
        return Objects.equals(reservation, that.reservation) && Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservation, sku);
    }
}