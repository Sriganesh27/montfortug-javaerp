package com.erp.montfortuganda.auth.repository;

import com.erp.montfortuganda.auth.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @EntityGraph(attributePaths = "assignedBranch")
    @Query("""
        SELECT user
        FROM User user
        WHERE user.username = :username
    """)
    Optional<User> findByUsernameWithAssignedBranch(
            @Param("username") String username
    );
}