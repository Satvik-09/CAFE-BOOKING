package com.cafe.booking.controller;

import com.cafe.booking.Service.IdempotencyService;
import com.cafe.booking.Service.ReservationService;
import com.cafe.booking.dto.ReservationRequest;
import com.cafe.booking.dto.ReservationResponse;
import com.cafe.booking.entity.Reservation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
             private final ReservationService service;
             private final IdempotencyService idempotency;

        @GetMapping("/my")
      public List<ReservationResponse> mine(Authentication auth){
            return service.mine(auth.getName());
        }

        @DeleteMapping("/{id}")
      public ReservationResponse cancel(Authentication auth, @PathVariable Long id){
            return service.cancel(auth.getName(),id);
        }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(Authentication auth,
                                                     @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                                     @Valid @RequestBody ReservationRequest req) {
        String user = auth.getName();
        if (key == null || key.isBlank()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user, req));
        }
        IdempotencyService.Result r = idempotency.run(user, key, "POST /reservations", req,
                () -> service.create(user, req).id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Idempotent-Replayed", String.valueOf(r.replayed()))
                .body(service.getMine(user, r.resourceId()));
    }
}
