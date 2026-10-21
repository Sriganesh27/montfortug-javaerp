package com.erp.montfortuganda.auth.repository;

import com.erp.montfortuganda.auth.entity.ErpUserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpUserSessionRepository extends JpaRepository<ErpUserSession, Long> {

    Optional<ErpUserSession> findBySessionToken(String sessionToken);

    List<ErpUserSession> findByUser_IdAndActiveTrueAndExpiryTimeAfterOrderByLastActivityTimeDesc(
            Integer userId,
            LocalDateTime now
    );

    List<ErpUserSession> findByUser_IdAndActiveTrueAndSessionTokenNot(
            Integer userId,
            String currentSessionToken
    );
}
