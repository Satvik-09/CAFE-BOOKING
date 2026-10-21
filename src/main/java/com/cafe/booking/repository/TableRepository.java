package com.cafe.booking.repository;

import com.cafe.booking.entity.CafeTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<CafeTable,Long> {
}
