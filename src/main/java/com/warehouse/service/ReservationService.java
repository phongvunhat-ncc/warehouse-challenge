package com.warehouse.service;

import com.warehouse.domain.Inventory;
import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;
import java.util.List;

public interface ReservationService {
    Reservation createReservation(String orderId, List<ReservationItem> requestedItems);
    Reservation confirmReservation(String id);
    Reservation cancelReservation(String id);
    Reservation getReservation(String id);
    Inventory getInventory(String sku);
}