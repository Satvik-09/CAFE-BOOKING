package com.cafe.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username","idem_key","endpoint"}))
@Getter @Setter @NoArgsConstructor
public class IdempotencyRecord {

     @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     @Column(nullable = false,length = 50)
    private String username;

     @Column(name = "idem_key",nullable = false,length = 64)
      private String idemKey;

     @Column(nullable = false,length = 40)
    private String endpoint;

     @Column(nullable = false,length = 64)
    private String requestHash;

     private Long resourceId;
     private LocalDateTime createdAt;

     @PrePersist
    void onCreate(){   createdAt = LocalDateTime.now();}
}
