package com.cafe.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long menuitemid,
        @NotNull @Min(1) Integer quantity
) {
}
