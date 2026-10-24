package com.cafe.booking.dto;

import com.cafe.booking.entity.Enum.OrderStatus;
import com.cafe.booking.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(Long id, OrderStatus status, BigDecimal TotalAmount, LocalDateTime CreatedAt,
                            List<OrderItemResponse> items) {
public static OrderResponse from(Order o) {
    List<OrderItemResponse> lines = o.getItems().stream()
            .map(i -> new OrderItemResponse(
                    i.getMenuItem().getName(),
                   i.getQuantity(),
                    i.getPriceAtOrder(),
                    i.getPriceAtOrder().multiply(BigDecimal.valueOf(i.getQuantity()))
            )).toList();
    return  new OrderResponse(o.getId(),o.getStatus(),o.getTotalAmount(),o.getCreatedAt(),lines);
}
}
