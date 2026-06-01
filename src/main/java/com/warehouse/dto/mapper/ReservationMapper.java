package com.warehouse.dto.mapper;

import com.warehouse.domain.Inventory;
import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;
import com.warehouse.dto.*;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse dto = new ReservationResponse();
        dto.setId(reservation.getId());
        dto.setOrderId(reservation.getOrderId());
        dto.setStatus(reservation.getStatus());
        dto.setCreatedAt(reservation.getCreatedAt());
        dto.setItems(reservation.getItems().stream().map(this::toResponse).toList());
        return dto;
    }

    public ItemResponse toResponse(ReservationItem item) {
        ItemResponse dto = new ItemResponse();
        dto.setSku(item.getSku());
        dto.setQuantity(item.getQuantity());
        return dto;
    }

    public InventoryResponse toResponse(Inventory inventory) {
        InventoryResponse dto = new InventoryResponse();
        dto.setSku(inventory.getSku());
        dto.setTotalStock(inventory.getTotalStock());
        dto.setAvailableStock(inventory.getAvailableStock());
        dto.setReservedStock(inventory.getReservedStock());
        return dto;
    }
}
