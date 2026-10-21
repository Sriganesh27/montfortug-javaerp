package com.erp.montfortuganda.auth.repository;

import com.erp.montfortuganda.auth.entity.ErpLoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpLoginHistoryRepository extends JpaRepository<ErpLoginHistory, Long> {

    List<ErpLoginHistory> findTop50ByUser_IdOrderByLoginTimeDesc(Integer userId);

    Optional<ErpLoginHistory> findFirstByUser_IdAndRemarksContainingOrderByLoginTimeDesc(
            Integer userId,
            String sessionMarker
    );

    Optional<ErpLoginHistory> findFirstByUser_IdAndLoginTimeAndLogoutTimeIsNullOrderByLoginHistoryIdDesc(
            Integer userId,
            java.time.LocalDateTime loginTime
    );
}
