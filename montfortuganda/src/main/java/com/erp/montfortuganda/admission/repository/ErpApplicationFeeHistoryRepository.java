package com.erp.montfortuganda.admission.repository;

import com.erp.montfortuganda.admission.entity.ErpApplicationFeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErpApplicationFeeHistoryRepository
        extends JpaRepository<ErpApplicationFeeHistory, Long> {

    List<ErpApplicationFeeHistory>
    findByApplicationIdAndBranchIdOrderByChangedAtDesc(
            Long applicationId,
            Integer branchId
    );

    List<ErpApplicationFeeHistory>
    findByFeeIdOrderByChangedAtDesc(
            Long feeId
    );
}
