package com.cafe.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @NotEmpty @Valid List<OrderItemRequest> items
        ) {
}
