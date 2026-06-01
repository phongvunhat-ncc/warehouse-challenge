package com.warehouse.service;

import com.warehouse.domain.Inventory;
import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationItem;
import com.warehouse.domain.factory.ReservationFactory;
import com.warehouse.exception.InsufficientStockException;
import com.warehouse.exception.ResourceNotFoundException;
import com.warehouse.repository.InventoryRepository;
import com.warehouse.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(InventoryRepository inventoryRepository, ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation createReservation(String orderId, List<ReservationItem> requestedItems) {
        // Sort SKUs to enforce deterministic locking order and prevent deadlocks
        List<String> skus = requestedItems.stream()
                .map(ReservationItem::getSku)
                .sorted()
                .toList();

        // Obtain write locks on the targeted rows
        List<Inventory> inventories = inventoryRepository.findAllBySkuInWithLock(skus);
        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSku, i -> i));

        // Validation checking
        for (ReservationItem requestedItem : requestedItems) {
            Inventory inventory = inventoryMap.get(requestedItem.getSku());
            if (inventory == null) {
                throw new ResourceNotFoundException("PRODUCT_NOT_FOUND", "Product stock records missing for SKU: " + requestedItem.getSku());
            }
            if (inventory.getAvailableStock() < requestedItem.getQuantity()) {
                throw new InsufficientStockException(
                        "INSUFFICIENT_STOCK",
                        String.format("SKU %s has only %d units available, %d were requested",
                                inventory.getSku(), inventory.getAvailableStock(), requestedItem.getQuantity())
                );
            }
        }

        // Deduct stock levels inside transaction scope
        for (ReservationItem requestedItem : requestedItems) {
            Inventory inventory = inventoryMap.get(requestedItem.getSku());
            inventory.reserve(requestedItem.getQuantity());
        }

        inventoryRepository.saveAll(inventories);

        Reservation reservation = ReservationFactory.createPendingReservation(orderId, requestedItems);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation confirmReservation(String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reservation not found with ID: " + id));

        // Use state pattern transition
        reservation.confirm();

        List<String> skus = reservation.getItems().stream()
                .map(ReservationItem::getSku)
                .sorted()
                .toList();

        List<Inventory> inventories = inventoryRepository.findAllBySkuInWithLock(skus);
        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSku, i -> i));

        for (ReservationItem item : reservation.getItems()) {
            Inventory inventory = inventoryMap.get(item.getSku());
            if (inventory != null) {
                inventory.confirm(item.getQuantity());
            }
        }

        inventoryRepository.saveAll(inventories);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation cancelReservation(String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reservation not found with ID: " + id));

        // Use state pattern transition
        reservation.cancel();

        List<String> skus = reservation.getItems().stream()
                .map(ReservationItem::getSku)
                .sorted()
                .toList();

        List<Inventory> inventories = inventoryRepository.findAllBySkuInWithLock(skus);
        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSku, i -> i));

        for (ReservationItem item : reservation.getItems()) {
            Inventory inventory = inventoryMap.get(item.getSku());
            if (inventory != null) {
                inventory.release(item.getQuantity());
            }
        }

        inventoryRepository.saveAll(inventories);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getReservation(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RESERVATION_NOT_FOUND", "Reservation not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public Inventory getInventory(String sku) {
        return inventoryRepository.findById(sku)
                .orElseThrow(() -> new ResourceNotFoundException("PRODUCT_NOT_FOUND", "No product/inventory found for SKU: " + sku));
    }
}