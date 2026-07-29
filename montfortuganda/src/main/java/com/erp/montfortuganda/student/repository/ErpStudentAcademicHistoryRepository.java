package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory;
import com.erp.montfortuganda.student.entity.ErpStudentAcademicHistory.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpStudentAcademicHistoryRepository
        extends JpaRepository<ErpStudentAcademicHistory, Long> {

    /**
     * Loads one academic-history record while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAcademicHistory>
    findByAcademicHistoryIdAndBranch_BranchId(
            Long academicHistoryId,
            Integer branchId
    );

    /**
     * Finds the academic-history record belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAcademicHistory>
    findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds the active academic-history record belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAcademicHistory>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds academic history using the student's admission number.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAcademicHistory>
    findByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Prevents multiple academic-history records for the same student.
     */
    boolean existsByStudent_StudentId(
            Long studentId
    );

    /**
     * Branch-safe existence check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Used during update to ensure another academic-history row does not exist.
     */
    boolean existsByStudent_StudentIdAndAcademicHistoryIdNot(
            Long studentId,
            Long academicHistoryId
    );

    /**
     * Finds a record by PLE index number within a branch.
     */
    Optional<ErpStudentAcademicHistory>
    findByPleIndexNumberIgnoreCaseAndBranch_BranchId(
            String pleIndexNumber,
            Integer branchId
    );

    /**
     * Finds a record by UCE index number within a branch.
     */
    Optional<ErpStudentAcademicHistory>
    findByUceIndexNumberIgnoreCaseAndBranch_BranchId(
            String uceIndexNumber,
            Integer branchId
    );

    /**
     * Finds a record by UACE index number within a branch.
     */
    Optional<ErpStudentAcademicHistory>
    findByUaceIndexNumberIgnoreCaseAndBranch_BranchId(
            String uaceIndexNumber,
            Integer branchId
    );

    /**
     * Lists all academic-history records in a branch.
     */
    Page<ErpStudentAcademicHistory> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active academic-history records in a branch.
     */
    Page<ErpStudentAcademicHistory>
    findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists records by verification status.
     */
    Page<ErpStudentAcademicHistory>
    findByBranch_BranchIdAndVerificationStatusAndActiveTrue(
            Integer branchId,
            VerificationStatus verificationStatus,
            Pageable pageable
    );

    /**
     * Lists records belonging to a former school.
     */
    Page<ErpStudentAcademicHistory>
    findByBranch_BranchIdAndFormerSchoolCodeIgnoreCaseAndActiveTrue(
            Integer branchId,
            String formerSchoolCode,
            Pageable pageable
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndVerificationStatusAndActiveTrue(
            Integer branchId,
            VerificationStatus verificationStatus
    );
}