package com.erp.montfortuganda.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Spring Boot sees the name "findByUsername" and automatically writes a SQL query:
    // SELECT * FROM erp_users WHERE username = ?
    Optional<User> findByUsername(String username);
}