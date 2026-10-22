package com.cafe.booking.controller;

import com.cafe.booking.Service.ReservationService;
import com.cafe.booking.dto.ReservationRequest;
import com.cafe.booking.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {
             private final ReservationService service;

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
      public ReservationResponse create(Authentication auth, @Valid @RequestBody ReservationRequest req){
            return service.create(auth.getName(),req);
        }

        @GetMapping("/my")
      public List<ReservationResponse> mine(Authentication auth){
            return service.mine(auth.getName());
        }

        @DeleteMapping("/cancel")
      public ReservationResponse cancel(Authentication auth, @RequestParam Long id){
            return service.cancel(auth.getName(),id);
        }
}
