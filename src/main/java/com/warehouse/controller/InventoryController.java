package com.warehouse.controller;

import com.warehouse.domain.Inventory;
import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.InventoryResponse;
import com.warehouse.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final ReservationService reservationService;

    public InventoryController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable String sku) {
        Inventory inventory = reservationService.getInventory(sku);
        return ResponseEntity.ok(ApiResponse.success(InventoryResponse.fromDomain(inventory)));
    }
}