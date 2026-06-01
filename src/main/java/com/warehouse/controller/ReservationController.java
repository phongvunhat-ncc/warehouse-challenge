package com.warehouse.controller;

import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;
import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.ReservationRequest;
import com.warehouse.dto.ReservationResponse;
import com.warehouse.dto.mapper.ReservationMapper;
import com.warehouse.service.ReservationServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationServiceImpl reservationServiceImpl;
    private final ReservationMapper reservationMapper;

    public ReservationController(ReservationServiceImpl reservationServiceImpl, ReservationMapper reservationMapper) {
        this.reservationServiceImpl = reservationServiceImpl;
        this.reservationMapper = reservationMapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@Valid @RequestBody ReservationRequest request) {
        List<ReservationItem> items = request.getItems().stream()
                .map(i -> new ReservationItem(i.getSku(), i.getQuantity()))
                .toList();

        Reservation reservation = reservationServiceImpl.createReservation(request.getOrderId(), items);
        ReservationResponse response = reservationMapper.toResponse(reservation);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirmReservation(@PathVariable String id) {
        Reservation reservation = reservationServiceImpl.confirmReservation(id);
        return ResponseEntity.ok(ApiResponse.success(reservationMapper.toResponse(reservation)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(@PathVariable String id) {
        Reservation reservation = reservationServiceImpl.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.success(reservationMapper.toResponse(reservation)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservation(@PathVariable String id) {
        Reservation reservation = reservationServiceImpl.getReservation(id);
        return ResponseEntity.ok(ApiResponse.success(reservationMapper.toResponse(reservation)));
    }
}