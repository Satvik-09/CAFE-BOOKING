package com.cafe.booking.repository;

import com.cafe.booking.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository  extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUsername(String username);

}
