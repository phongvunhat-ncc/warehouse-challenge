package com.warehouse.domain.state;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;

public class ConfirmedState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new IllegalStateException("Reservation is already CONFIRMED and cannot be updated.");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new IllegalStateException("Reservation is already CONFIRMED and cannot be cancelled.");
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.CONFIRMED;
    }
}