package com.warehouse.domain.state;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;

public class PendingState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        reservation.transitionTo(new ConfirmedState());
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.transitionTo(new CancelledState());
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.PENDING;
    }
}