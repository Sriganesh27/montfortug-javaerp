package com.erp.montfortuganda.fees.repository;

import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ErpFeeStructureItemRepository extends JpaRepository<ErpFeeStructureItem, Long> {

    @EntityGraph(attributePaths = {"branch"})
    List<ErpFeeStructureItem> findAllByBranch_BranchId(Integer branchId);

    List<ErpFeeStructureItem> findAllByBranch_BranchIdAndActiveTrue(Integer branchId);

    Optional<ErpFeeStructureItem> findByFeeStructureItemIdAndBranch_BranchId(
            Long feeStructureItemId,
            Integer branchId
    );

    boolean existsByBranch_BranchIdAndFeeTypeIgnoreCaseAndFeeNameIgnoreCaseAndActiveTrue(
            Integer branchId,
            String feeType,
            String feeName
    );
}
