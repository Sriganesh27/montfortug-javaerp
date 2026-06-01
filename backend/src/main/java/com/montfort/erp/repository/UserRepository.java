package com.montfort.erp.repository;

import com.montfort.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsActive(String username, Integer isActive);
}
