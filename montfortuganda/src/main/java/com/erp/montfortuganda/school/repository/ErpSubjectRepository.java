package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.ErpSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ErpSubjectRepository
        extends JpaRepository<ErpSubject, Long> {

    /**
     * Branch Subject dashboard list.
     *
     * active=true represents a usable/non-deleted master row.
     * Status may still be ACTIVE or INACTIVE.
     */
    List<ErpSubject> findAllByBranch_BranchIdAndActiveTrueOrderByDisplayOrderAscSubjectNameAsc(
            Integer branchId
    );

    /**
     * Entrance Test subject list.
     *
     * Only subjects belonging to the authenticated branch and explicitly
     * marked ACTIVE are exposed for mark entry.
     */
    List<ErpSubject> findAllByBranch_BranchIdAndActiveTrueAndStatusOrderByDisplayOrderAscSubjectNameAsc(
            Integer branchId,
            ErpSubject.Status status
    );

    /**
     * Secure branch-scoped lookup.
     */
    Optional<ErpSubject> findBySubjectIdAndBranch_BranchIdAndActiveTrue(
            Long subjectId,
            Integer branchId
    );

    /**
     * Subject code is unique inside a branch.
     */
    boolean existsByBranch_BranchIdAndSubjectCodeIgnoreCase(
            Integer branchId,
            String subjectCode
    );

    /**
     * Subject code duplicate validation while editing.
     */
    boolean existsByBranch_BranchIdAndSubjectCodeIgnoreCaseAndSubjectIdNot(
            Integer branchId,
            String subjectCode,
            Long subjectId
    );
}
