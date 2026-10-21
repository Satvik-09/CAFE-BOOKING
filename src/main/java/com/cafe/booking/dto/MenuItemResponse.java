package com.cafe.booking.dto;

import com.cafe.booking.entity.MenuItem;

import java.math.BigDecimal;

public record MenuItemResponse(Long id, String name, String description, BigDecimal price,boolean available){
    public static MenuItemResponse from(MenuItem m){
        return new MenuItemResponse(m.getId(),m.getName(),m.getDescription(),m.getPrice(),m.isAvailable());
    }
}
