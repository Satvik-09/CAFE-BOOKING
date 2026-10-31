package com.cafe.booking.repository;

import com.cafe.booking.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord,Long> {

    Optional<IdempotencyRecord> findByUsernameAndIdemKeyAndEndpoint(String username,String idemKey,String endpoint);
}
