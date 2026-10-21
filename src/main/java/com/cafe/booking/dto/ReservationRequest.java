package com.cafe.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationRequest(@NotNull Long tableId,
                                @NotNull @FutureOrPresent LocalDate date,
                                @NotNull LocalTime startTime ,
                                @NotNull LocalTime endTime ,
                                @NotNull @Min(1) Integer partySize){}
