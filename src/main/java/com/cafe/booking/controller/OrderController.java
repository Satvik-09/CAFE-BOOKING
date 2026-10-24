package com.cafe.booking.controller;

import com.cafe.booking.Service.OrderService;
import com.cafe.booking.dto.OrderRequest;
import com.cafe.booking.dto.OrderResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(Authentication auth, @Valid @RequestBody OrderRequest req){
        return service.place(auth.getName(),req);
    }

    @GetMapping("/my")
    public List<OrderResponse> mine(Authentication auth){
        return service.mine(auth.getName());
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(Authentication auth, Long id){
        return service.cancelMine(auth.getName(), id);
    }

}
