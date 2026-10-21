package com.cafe.booking.dto;

import com.cafe.booking.entity.Enum.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuItemRequest(@NotBlank String name, String description,
                              @NotNull @DecimalMin("0.01")BigDecimal price,
                              @NotNull Category category, Boolean available) {}
