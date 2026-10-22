package com.cafe.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemResponse(String name, Integer quantity,
                                BigDecimal unitprice,BigDecimal lineTotal) {
}
