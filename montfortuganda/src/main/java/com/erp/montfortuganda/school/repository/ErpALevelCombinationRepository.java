package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.ErpALevelCombination;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-scoped persistence operations for A' Level combinations.
 */
@Repository
public interface ErpALevelCombinationRepository
        extends JpaRepository<ErpALevelCombination, Long> {

    /**
     * Loads active combinations and their four subjects for branch display.
     */
    @EntityGraph(attributePaths = {
            "branch",
            "principalSubject1",
            "principalSubject2",
            "principalSubject3",
            "subsidiarySubject"
    })
    List<ErpALevelCombination>
    findAllByBranch_BranchIdAndActiveTrueOrderByCombinationCodeAsc(
            Integer branchId
    );

    /** Loads all combinations, including inactive records, for Branch Admin management. */
    @EntityGraph(attributePaths = {
            "branch", "principalSubject1", "principalSubject2",
            "principalSubject3", "subsidiarySubject"
    })
    List<ErpALevelCombination> findAllByBranch_BranchIdOrderByCombinationCodeAsc(
            Integer branchId
    );

    /**
     * Branch-safe combination lookup.
     */
    @EntityGraph(attributePaths = {
            "branch",
            "principalSubject1",
            "principalSubject2",
            "principalSubject3",
            "subsidiarySubject"
    })
    Optional<ErpALevelCombination> findByCombinationIdAndBranch_BranchId(
            Long combinationId,
            Integer branchId
    );

    /**
     * Branch-safe lookup restricted to active combinations.
     */
    @EntityGraph(attributePaths = {
            "branch",
            "principalSubject1",
            "principalSubject2",
            "principalSubject3",
            "subsidiarySubject"
    })
    Optional<ErpALevelCombination>
    findByCombinationIdAndBranch_BranchIdAndActiveTrue(
            Long combinationId,
            Integer branchId
    );

    /**
     * Checks whether a combination code already exists in a branch.
     */
    boolean existsByBranch_BranchIdAndCombinationCodeIgnoreCase(
            Integer branchId,
            String combinationCode
    );

    /**
     * Checks for duplicate code while excluding the current combination.
     */
    boolean existsByBranch_BranchIdAndCombinationCodeIgnoreCaseAndCombinationIdNot(
            Integer branchId,
            String combinationCode,
            Long combinationId
    );
}