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
        System.setProperty("docker.api.version", "1.43");
    }
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        // Verify this path is correct for your project structure
        registry.add("spring.liquibase.change-log", () -> "file:database/changelog/db.changelog-master.yaml");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        // Clear previous state and re-initialize test data
        inventoryRepository.deleteAllInBatch(); 
        Inventory inv = new Inventory("C300", 10, 10, 0);
        inventoryRepository.save(inv);
    }

    @Test
    void testConcurrentReservations_PreventOverselling() throws InterruptedException, ExecutionException, TimeoutException {
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
            // Prevent indefinite blocking if one thread fails early
            barrier.await(10, TimeUnit.SECONDS);
            return restTemplate.postForEntity("/api/v1/reservations", req1, ApiResponse.class);
        };

        Callable<ResponseEntity<ApiResponse>> task2 = () -> {
            barrier.await(10, TimeUnit.SECONDS);
            return restTemplate.postForEntity("/api/v1/reservations", req2, ApiResponse.class);
        };

        try {
            Future<ResponseEntity<ApiResponse>> future1 = executor.submit(task1);
            Future<ResponseEntity<ApiResponse>> future2 = executor.submit(task2);

            // Fetch results with a timeout to keep the test from hanging if server threads lock up
            ResponseEntity<ApiResponse> res1 = future1.get(15, TimeUnit.SECONDS);
            ResponseEntity<ApiResponse> res2 = future2.get(15, TimeUnit.SECONDS);

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

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

    private void checkResponse(ResponseEntity<ApiResponse> response, AtomicInteger success, AtomicInteger failure) {
        if (response.getStatusCode().is2xxSuccessful()) {
            success.incrementAndGet();
        } else {
            // Treats 4xx and any unhandled 5xx as failures
            failure.incrementAndGet();
        }
    }
}