package com.warehouse.domain.state;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;

public class PendingState implements ReservationState {
    public static final PendingState INSTANCE = new PendingState();
    
    private PendingState() {} // Private constructor prevents new allocations

    @Override
    public void confirm(Reservation reservation) {
        reservation.transitionTo(ConfirmedState.INSTANCE);
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.transitionTo(CancelledState.INSTANCE);
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.PENDING;
    }
}