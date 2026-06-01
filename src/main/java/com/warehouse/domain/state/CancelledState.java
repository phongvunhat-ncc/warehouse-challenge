package com.warehouse.domain.state;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;

public class CancelledState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new IllegalStateException("Reservation is CANCELLED and cannot be confirmed.");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new IllegalStateException("Reservation is already CANCELLED.");
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.CANCELLED;
    }
}