package com.erp.montfortuganda.auth.repository;

import com.erp.montfortuganda.auth.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository
        extends JpaRepository<User, Integer> {

    @EntityGraph(
            attributePaths = {
                    "userRoles",
                    "userRoles.role"
            }
    )
    @Query("""
            SELECT user
            FROM User user
            WHERE LOWER(user.username) = LOWER(:username)
            """)
    Optional<User> findByUsername(
            @Param("username") String username
    );

    @Query("""
            SELECT CASE
                WHEN COUNT(user) > 0 THEN true
                ELSE false
            END
            FROM User user
            WHERE LOWER(user.username) = LOWER(:username)
            """)
    boolean existsByUsername(
            @Param("username") String username
    );

    @EntityGraph(attributePaths = "assignedBranch")
    @Query("""
            SELECT user
            FROM User user
            WHERE LOWER(user.username) = LOWER(:username)
            """)
    Optional<User> findByUsernameWithAssignedBranch(
            @Param("username") String username
    );

    @EntityGraph(attributePaths = "assignedBranch")
    @Query("""
            SELECT user
            FROM User user
            ORDER BY user.id DESC
            """)
    List<User> findAllWithAssignedBranch();
}
