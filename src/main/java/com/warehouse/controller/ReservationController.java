package com.warehouse.controller;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;
import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.ReservationRequest;
import com.warehouse.dto.ReservationResponse;
import com.warehouse.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@Valid @RequestBody ReservationRequest request) {
        List<ReservationItem> items = request.getItems().stream()
                .map(i -> new ReservationItem(i.getSku(), i.getQuantity()))
                .toList();

        Reservation reservation = reservationService.createReservation(request.getOrderId(), items);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.fromDomain(reservation)));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable String id) {
        Reservation reservation = reservationService.confirmReservation(id);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.fromDomain(reservation)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable String id) {
        Reservation reservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.fromDomain(reservation)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(@PathVariable String id) {
        Reservation reservation = reservationService.getReservation(id);
        return ResponseEntity.ok(ApiResponse.success(ReservationResponse.fromDomain(reservation)));
    }
}