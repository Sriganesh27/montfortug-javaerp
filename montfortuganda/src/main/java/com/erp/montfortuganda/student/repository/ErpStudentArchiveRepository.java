package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentArchive;
import com.erp.montfortuganda.student.entity.ErpStudentArchive.ArchiveReason;
import com.erp.montfortuganda.student.entity.ErpStudentArchive.ArchiveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentArchiveRepository
        extends JpaRepository<ErpStudentArchive, Long> {

    /**
     * Loads one archive record while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentArchive> findByArchiveIdAndBranch_BranchId(
            Long archiveId,
            Integer branchId
    );

    /**
     * Returns the complete archive and restoration history of one student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentArchive>
    findByStudent_StudentIdAndBranch_BranchIdOrderByCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Returns the latest archive or restoration record for one student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentArchive>
    findFirstByStudent_StudentIdAndBranch_BranchIdOrderByCreatedAtDesc(
            Long studentId,
            Integer branchId
    );

    /**
     * Returns archive records using the admission number.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    List<ErpStudentArchive>
    findByAdmissionNoIgnoreCaseAndBranch_BranchIdOrderByCreatedAtDesc(
            String admissionNo,
            Integer branchId
    );

    /**
     * Checks whether the student currently has an archived record.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndArchiveStatus(
            Long studentId,
            Integer branchId,
            ArchiveStatus archiveStatus
    );

    /**
     * Lists all lifecycle archive records in a branch.
     */
    Page<ErpStudentArchive> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists records by archive status.
     */
    Page<ErpStudentArchive>
    findByBranch_BranchIdAndArchiveStatus(
            Integer branchId,
            ArchiveStatus archiveStatus,
            Pageable pageable
    );

    /**
     * Lists records by archive reason.
     */
    Page<ErpStudentArchive>
    findByBranch_BranchIdAndArchiveReason(
            Integer branchId,
            ArchiveReason archiveReason,
            Pageable pageable
    );

    /**
     * Lists students who left during the supplied date range.
     */
    Page<ErpStudentArchive>
    findByBranch_BranchIdAndDateOfLeavingBetween(
            Integer branchId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndArchiveStatus(
            Integer branchId,
            ArchiveStatus archiveStatus
    );

    long countByBranch_BranchIdAndArchiveReason(
            Integer branchId,
            ArchiveReason archiveReason
    );
}