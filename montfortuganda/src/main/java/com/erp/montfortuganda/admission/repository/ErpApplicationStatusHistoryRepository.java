package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ErpApplicationStatusHistoryRepository extends JpaRepository<ErpApplicationStatusHistory, Long> {

    List<ErpApplicationStatusHistory> findByApplication_ApplicationIdOrderByChangedAtDesc(Long applicationId);
}