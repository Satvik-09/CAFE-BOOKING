package com.cafe.booking.controller;

import com.cafe.booking.Service.IdempotencyService;
import com.cafe.booking.Service.OrderService;
import com.cafe.booking.dto.OrderRequest;
import com.cafe.booking.dto.OrderResponse;
import com.cafe.booking.entity.IdempotencyRecord;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final IdempotencyService idempotency;

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
    public OrderResponse cancel(Authentication auth, @PathVariable  Long id){
        return service.cancelMine(auth.getName(), id);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> place (Authentication auth,
                      @RequestHeader(value = "idempotency-Key",required = false)String key,
                      @Valid @RequestBody OrderRequest req){
        String user = auth.getName();
        if(key == null || key.isBlank()){
            return ResponseEntity.status(HttpStatus.CREATED).body(service.place(user,req));
        }

        IdempotencyService.Result r = idempotency.run(user,key,"POST /orders", req,
                () -> service.place(user,req).id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Idempotent-Replayed", String.valueOf(r.replayed()))
                .body(service.getMine(user,r.resourceId()));
    }

}
