package com.cafe.booking.dto;

import com.cafe.booking.entity.Enum.ReservationStatus;
import com.cafe.booking.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationResponse(Long id, Integer tableNumber, LocalDate date, LocalTime startTime,
                                  LocalTime endTime, Integer partySize, ReservationStatus status){

         public static ReservationResponse from(Reservation r){
             return new ReservationResponse(r.getId(),r.getTable().getTableNumber(),
                     r.getReservationDate(),
                     r.getStartTime(),r.getEndTime(),r.getPartySize(),r.getStatus());
         }
}
