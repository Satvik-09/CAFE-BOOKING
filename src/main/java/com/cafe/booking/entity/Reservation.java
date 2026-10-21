package com.cafe.booking.entity;

import com.cafe.booking.entity.Enum.ReservationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservations")
@Getter@Setter@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY ,optional = false)
    @JoinColumn( name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "table_id",nullable = false)
    private CafeTable table;

    @Column(nullable = false) private LocalDate reservationDate;
    @Column(nullable = false) private LocalTime startTime;
    @Column(nullable = false) private LocalTime endTime;
    @Column(nullable = false) private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    private LocalDateTime CreatedAt;

    @PrePersist
    private void Oncreate(){
        CreatedAt = LocalDateTime.now();
    }
}
