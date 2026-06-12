package com.montfort.erp.modules.auth.repository;

import com.montfort.erp.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsActive(String username, Integer isActive);
}

