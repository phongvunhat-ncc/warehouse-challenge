package com.warehouse.service;

import com.warehouse.domain.*;
import com.warehouse.exception.InsufficientStockException;
import com.warehouse.repository.InventoryRepository;
import com.warehouse.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private InventoryRepository inventoryRepository;
    private ReservationRepository reservationRepository;
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        inventoryRepository = Mockito.mock(InventoryRepository.class);
        reservationRepository = Mockito.mock(ReservationRepository.class);
        reservationService = new ReservationService(inventoryRepository, reservationRepository);
    }

    @Test
    void testCreateReservation_Successful() {
        String sku = "A100";
        Inventory inventory = new Inventory(sku, 100, 100, 0);
        ReservationItem requestedItem = new ReservationItem(sku, 5);

        when(inventoryRepository.findAllBySkuInWithLock(List.of(sku))).thenReturn(List.of(inventory));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.createReservation("ORD-1001", List.of(requestedItem));

        assertNotNull(result);
        assertEquals(ReservationStatus.PENDING, result.getStatus());
        assertEquals(95, inventory.getAvailableStock());
        assertEquals(5, inventory.getReservedStock());
        verify(inventoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateReservation_InsufficientStock() {
        String sku = "A100";
        Inventory inventory = new Inventory(sku, 10, 10, 0);
        ReservationItem requestedItem = new ReservationItem(sku, 15);

        when(inventoryRepository.findAllBySkuInWithLock(List.of(sku))).thenReturn(List.of(inventory));

        assertThrows(InsufficientStockException.class, () -> 
            reservationService.createReservation("ORD-1001", List.of(requestedItem))
        );
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void testConfirmReservation_StateTransition() {
        String resId = "RES-1234";
        Reservation reservation = new Reservation();
        reservation.setId(resId);
        reservation.setOrderId("ORD-1");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.addItem(new ReservationItem("A100", 5));

        Inventory inventory = new Inventory("A100", 100, 95, 5);

        when(reservationRepository.findById(resId)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findAllBySkuInWithLock(List.of("A100"))).thenReturn(List.of(inventory));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.confirmReservation(resId);

        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        assertEquals(0, inventory.getReservedStock());
        assertEquals(95, inventory.getTotalStock());
    }

    @Test
    void testCancelReservation_StateTransition() {
        String resId = "RES-1234";
        Reservation reservation = new Reservation();
        reservation.setId(resId);
        reservation.setOrderId("ORD-1");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.addItem(new ReservationItem("A100", 5));

        Inventory inventory = new Inventory("A100", 100, 95, 5);

        when(reservationRepository.findById(resId)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findAllBySkuInWithLock(List.of("A100"))).thenReturn(List.of(inventory));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.cancelReservation(resId);

        assertEquals(ReservationStatus.CANCELLED, result.getStatus());
        assertEquals(0, inventory.getReservedStock());
        assertEquals(100, inventory.getAvailableStock());
    }

    @Test
    void testDoubleConfirm_ShouldFail() {
        String resId = "RES-1234";
        Reservation reservation = new Reservation();
        reservation.setId(resId);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(resId)).thenReturn(Optional.of(reservation));

        assertThrows(IllegalStateException.class, () -> reservationService.confirmReservation(resId));
    }
}