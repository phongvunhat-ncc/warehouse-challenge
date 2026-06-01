package com.warehouse.domain.state;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;

public interface ReservationState {
    void confirm(Reservation reservation);
    void cancel(Reservation reservation);
    ReservationStatus getStatus();
}