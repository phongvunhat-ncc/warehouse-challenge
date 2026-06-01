package com.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.domain.Reservation;
import com.warehouse.domain.ReservationStatus;
import com.warehouse.dto.ItemRequest;
import com.warehouse.dto.ReservationRequest;
import com.warehouse.dto.mapper.ReservationMapper;
import com.warehouse.service.ReservationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@Import(ReservationMapper.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationServiceImpl reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    private Reservation createMockReservation() {
        Reservation reservation = new Reservation();
        reservation.setId("RES-123");
        reservation.setOrderId("ORD-123");
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservation;
    }

    @Test
    void shouldReturn400WhenOrderIdIsBlank() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setOrderId(""); // Invalid
        ItemRequest item = new ItemRequest();
        item.setSku("A100");
        item.setQuantity(5);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn400WhenItemsAreEmpty() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setOrderId("ORD-123");
        request.setItems(Collections.emptyList()); // Invalid

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn400WhenSkuIsBlank() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setOrderId("ORD-123");
        ItemRequest item = new ItemRequest();
        item.setSku(""); // Invalid
        item.setQuantity(5);
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn400WhenQuantityIsZero() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setOrderId("ORD-123");
        ItemRequest item = new ItemRequest();
        item.setSku("A100");
        item.setQuantity(0); // Invalid
        request.setItems(List.of(item));

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn201WhenReservationIsCreated() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setOrderId("ORD-123");
        ItemRequest item = new ItemRequest();
        item.setSku("A100");
        item.setQuantity(5);
        request.setItems(List.of(item));

        Reservation mockReservation = createMockReservation();
        when(reservationService.createReservation(anyString(), any())).thenReturn(mockReservation);

        mockMvc.perform(post("/api/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderId").value("ORD-123"));
    }

    @Test
    void shouldReturn200WhenConfirmReservation() throws Exception {
        Reservation mockReservation = createMockReservation();
        mockReservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationService.confirmReservation("RES-123")).thenReturn(mockReservation);

        mockMvc.perform(post("/api/v1/reservations/RES-123/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void shouldReturn200WhenCancelReservation() throws Exception {
        Reservation mockReservation = createMockReservation();
        mockReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationService.cancelReservation("RES-123")).thenReturn(mockReservation);

        mockMvc.perform(post("/api/v1/reservations/RES-123/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("ORD-123"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void shouldReturn200WhenGetReservation() throws Exception {
        Reservation mockReservation = createMockReservation();
        when(reservationService.getReservation("RES-123")).thenReturn(mockReservation);

        mockMvc.perform(get("/api/v1/reservations/RES-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderId").value("ORD-123"));
    }
}
