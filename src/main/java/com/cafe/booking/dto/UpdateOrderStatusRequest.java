package com.cafe.booking.dto;

import com.cafe.booking.entity.Enum.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
