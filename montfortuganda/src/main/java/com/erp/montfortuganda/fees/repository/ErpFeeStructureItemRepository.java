package com.erp.montfortuganda.fees.repository;

import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpFeeStructureItemRepository extends JpaRepository<ErpFeeStructureItem, Long> {

    @EntityGraph(attributePaths = {"feeStructure"})
    List<ErpFeeStructureItem> findAllByFeeStructure_FeeStructureIdOrderByDisplayOrderAscFeeStructureItemIdAsc(
            Long feeStructureId);

    @EntityGraph(attributePaths = {"feeStructure"})
    Optional<ErpFeeStructureItem> findByFeeStructureItemIdAndFeeStructure_FeeStructureId(
            Long itemId, Long feeStructureId);

    boolean existsByFeeStructure_FeeStructureIdAndFeeTypeIgnoreCaseAndFeeNameIgnoreCase(
            Long feeStructureId, String feeType, String feeName);

    boolean existsByFeeStructure_FeeStructureIdAndFeeTypeIgnoreCaseAndFeeNameIgnoreCaseAndFeeStructureItemIdNot(
            Long feeStructureId, String feeType, String feeName, Long itemId);

    List<ErpFeeStructureItem> findAllByFeeStructure_FeeStructureIdInOrderByFeeStructure_FeeStructureIdAscDisplayOrderAscFeeStructureItemIdAsc(
            List<Long> feeStructureIds);

    long countByFeeStructure_FeeStructureIdAndActiveTrue(Long feeStructureId);
}
