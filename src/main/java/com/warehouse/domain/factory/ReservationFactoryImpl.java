package com.warehouse.domain.factory;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;
import com.warehouse.domain.state.PendingState;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ReservationFactoryImpl implements ReservationFactory {

    @Override
    public Reservation createPendingReservation(String orderId, List<ReservationItem> items) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Reservation must contain at least one item");
        }

        Reservation reservation = new Reservation();
        reservation.setId("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reservation.setOrderId(orderId);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.transitionTo(PendingState.INSTANCE);

        items.forEach(reservation::addItem);
        return reservation;
    }
}
