package com.cafe.booking.controller;

import com.cafe.booking.Service.OrderService;
import com.cafe.booking.dto.OrderResponse;
import com.cafe.booking.dto.UpdateOrderStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping (name = "/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
            private final OrderService service;

    @PatchMapping ("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @Valid @RequestBody UpdateOrderStatusRequest req) {
        return service.updateStatus(id, req.status());
    }
}
