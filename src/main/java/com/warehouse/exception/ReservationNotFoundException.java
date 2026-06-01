package com.warehouse.exception;

public class ReservationNotFoundException extends ResourceNotFoundException {
    public ReservationNotFoundException(String id) {
        super("RESERVATION_NOT_FOUND", "Reservation not found with ID: " + id);
    }
}
