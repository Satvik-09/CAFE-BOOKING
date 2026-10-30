package com.cafe.booking.repository;

import com.cafe.booking.entity.Enum.ReservationStatus;
import com.cafe.booking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation ,Long> {

    @Query("""
          select case when count(r) > 0 then true else false end
          from Reservation r 
          where r.table.id = :tableId
          and r.reservationDate = :date
          and r.status = :status
          and r.startTime < :end
          and r.endTime > :start
           """)

    Boolean existsOverlap(@Param("tableId") Long tableId ,
                           @Param("date")LocalDate date ,
                           @Param("start")LocalTime start,
                           @Param("end") LocalTime end,
                           @Param("status") ReservationStatus status);
    List<Reservation> findByUserIdOrderByReservationDateDescStartTimeDesc(Long userid);

    long countByTable_IdAndReservationDateAndStatus(Long tableId, LocalDate date,ReservationStatus status);



}
