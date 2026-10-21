package com.cafe.booking.repository;

import com.cafe.booking.entity.Enum.Category;
import com.cafe.booking.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    Page<MenuItem> findByCategory(Category category, Pageable pageable);
    boolean existsByName(String name);
}