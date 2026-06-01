package com.warehouse.domain.factory;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;

import java.util.List;

public interface ReservationFactory {
    Reservation createPendingReservation(String orderId, List<ReservationItem> items);
}