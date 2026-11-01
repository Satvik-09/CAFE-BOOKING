package com.cafe.booking;

import com.cafe.booking.Service.IdempotencyService;
import com.cafe.booking.Service.OrderService;
import com.cafe.booking.dto.OrderItemRequest;
import com.cafe.booking.dto.OrderRequest;
import com.cafe.booking.entity.Enum.Role;                // adjust to your role enum
import com.cafe.booking.entity.UserEntity;
import com.cafe.booking.repository.IdempotencyRepository;
import com.cafe.booking.repository.OrderRepository;
import com.cafe.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class IdempotencyRaceTest {

    private static final String USER = "idem-user";
    private static final int THREADS = 8;

    @Autowired IdempotencyService idempotency;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepo;
    @Autowired IdempotencyRepository idemRepo;
    @Autowired UserRepository userRepo;

    @BeforeEach
    void ensureUser() {
        if (userRepo.findByUsername(USER).isEmpty()) {
            UserEntity u = new UserEntity();
            u.setUsername(USER);
            u.setPassword("not-used");
            u.setRole(Role.USER);          // adjust to your role type
            userRepo.save(u);
        }
    }

    @Test
    void sameKeyInParallelCreatesExactlyOneOrder() throws Exception {
        Long userId = userRepo.findByUsername(USER).orElseThrow().getId();
        int before = orderRepo.findByUserIdOrderByCreatedAtDesc(userId).size();

        String key = UUID.randomUUID().toString();
        OrderRequest req = new OrderRequest(List.of(new OrderItemRequest(1L, 1)));

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Long>> results = new ArrayList<>();
        try {
            for (int i = 0; i < THREADS; i++) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    return idempotency.run(USER, key, "POST /orders", req,
                            () -> orderService.place(USER, req).id()).resourceId();
                }));
            }
            ready.await();
            go.countDown();

            Set<Long> ids = new HashSet<>();
            for (Future<Long> f : results) ids.add(f.get(30, TimeUnit.SECONDS));

            List<Long> created = orderRepo.findByUserIdOrderByCreatedAtDesc(userId)
                    .stream().map(o -> o.getId()).toList();
            System.out.printf("distinct ids returned: %d, orders created: %d%n",
                    ids.size(), created.size() - before);

            assertEquals(1, ids.size(), "all parallel calls must return the same order");
            assertEquals(before + 1, created.size(), "exactly one new order must exist");

            created.stream().limit(1).forEach(orderRepo::deleteById);   // clean up the order
            idemRepo.findByUsernameAndIdemKeyAndEndpoint(USER, key, "POST /orders")
                    .ifPresent(idemRepo::delete);
        } finally {
            pool.shutdown();
        }
    }
}