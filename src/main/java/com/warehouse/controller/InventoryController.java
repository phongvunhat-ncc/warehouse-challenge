package com.warehouse.controller;

import com.warehouse.domain.Inventory;
import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.InventoryResponse;
import com.warehouse.dto.mapper.ReservationMapper;
import com.warehouse.service.ReservationServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final ReservationServiceImpl reservationServiceImpl;
    private final ReservationMapper reservationMapper;

    public InventoryController(ReservationServiceImpl reservationServiceImpl, ReservationMapper reservationMapper) {
        this.reservationServiceImpl = reservationServiceImpl;
        this.reservationMapper = reservationMapper;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable String sku) {
        Inventory inventory = reservationServiceImpl.getInventory(sku);
        return ResponseEntity.ok(ApiResponse.success(reservationMapper.toResponse(inventory)));
    }
}