package com.warehouse;

import com.warehouse.domain.Inventory;
import com.warehouse.dto.ApiResponse;
import com.warehouse.dto.ItemRequest;
import com.warehouse.dto.ReservationRequest;
import com.warehouse.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ReservationConcurrencyIntegrationTest {

    static {
        // Force the underlying docker-java client to negotiate using API version 1.43
        System.setProperty("docker.api.version", "1.43");
    }
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        // Reset stocks for testing
        Inventory inv = inventoryRepository.findById("C300").orElseThrow();
        Inventory resetInv = new Inventory("C300", 10, 10, 0);
        inventoryRepository.save(resetInv);
    }

    @Test
    void testConcurrentReservations_PreventOverselling() throws InterruptedException, ExecutionException {
        // Given: SKU C300 has total 10 units available.
        // Action: Two simultaneous transactions try to lock and reserve 6 units each.
        // Expectation: Only one succeeds. The other must fail with stock validation mismatch.

        ReservationRequest req1 = new ReservationRequest();
        req1.setOrderId("ORD-A");
        ItemRequest item1 = new ItemRequest();
        item1.setSku("C300");
        item1.setQuantity(6);
        req1.setItems(List.of(item1));

        ReservationRequest req2 = new ReservationRequest();
        req2.setOrderId("ORD-B");
        ItemRequest item2 = new ItemRequest();
        item2.setSku("C300");
        item2.setQuantity(6);
        req2.setItems(List.of(item2));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        Callable<ResponseEntity<ApiResponse>> task1 = () -> {
            barrier.await();
            return restTemplate.postForEntity("/api/v1/reservations", req1, ApiResponse.class);
        };

        Callable<ResponseEntity<ApiResponse>> task2 = () -> {
            barrier.await();
            return restTemplate.postForEntity("/api/v1/reservations", req2, ApiResponse.class);
        };

        Future<ResponseEntity<ApiResponse>> future1 = executor.submit(task1);
        Future<ResponseEntity<ApiResponse>> future2 = executor.submit(task2);

        ResponseEntity<ApiResponse> res1 = future1.get();
        ResponseEntity<ApiResponse> res2 = future2.get();

        executor.shutdown();

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        checkResponse(res1, successCount, failureCount);
        checkResponse(res2, successCount, failureCount);

        assertEquals(1, successCount.get(), "Exactly one reservation should succeed");
        assertEquals(1, failureCount.get(), "Exactly one reservation should fail");

        // Verify remaining DB stock state balances accurately
        Inventory finalInventory = inventoryRepository.findById("C300").orElseThrow();
        assertEquals(4, finalInventory.getAvailableStock());
        assertEquals(6, finalInventory.getReservedStock());
    }

    private void checkResponse(ResponseEntity<ApiResponse> response, AtomicInteger success, AtomicInteger failure) {
        if (response.getStatusCode().is2xxSuccessful()) {
            success.incrementAndGet();
        } else if (response.getStatusCode().is4xxClientError()) {
            failure.incrementAndGet();
        }
    }
}