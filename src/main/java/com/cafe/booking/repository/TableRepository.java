package com.cafe.booking.repository;

import com.cafe.booking.entity.CafeTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TableRepository extends JpaRepository<CafeTable,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from CafeTable t where t.id = :id")
    Optional<CafeTable> findByIdForUpdate(@Param("id") Long id);
}