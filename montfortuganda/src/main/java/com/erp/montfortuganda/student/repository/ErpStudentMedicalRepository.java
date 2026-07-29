package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentMedical;
import com.erp.montfortuganda.student.entity.ErpStudentMedical.BloodGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ErpStudentMedicalRepository
        extends JpaRepository<ErpStudentMedical, Long> {

    /**
     * Loads a medical record by ID while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentMedical> findByMedicalIdAndBranch_BranchId(
            Long medicalId,
            Integer branchId
    );

    /**
     * Finds the medical record belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentMedical> findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds the active medical record belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentMedical>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds a medical record using the student's admission number.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentMedical>
    findByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Prevents more than one medical record for the same student.
     */
    boolean existsByStudent_StudentId(
            Long studentId
    );

    /**
     * Branch-safe medical-record existence check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Used while updating to ensure another medical row does not exist.
     */
    boolean existsByStudent_StudentIdAndMedicalIdNot(
            Long studentId,
            Long medicalId
    );

    /**
     * Lists all medical records belonging to a branch.
     */
    Page<ErpStudentMedical> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active medical records belonging to a branch.
     */
    Page<ErpStudentMedical> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists students having a particular blood group.
     */
    Page<ErpStudentMedical>
    findByBranch_BranchIdAndBloodGroupAndActiveTrue(
            Integer branchId,
            BloodGroup bloodGroup,
            Pageable pageable
    );

    /**
     * Lists students marked as fit or not fit for sports.
     */
    Page<ErpStudentMedical>
    findByBranch_BranchIdAndFitForSportsAndActiveTrue(
            Integer branchId,
            Boolean fitForSports,
            Pageable pageable
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndBloodGroupAndActiveTrue(
            Integer branchId,
            BloodGroup bloodGroup
    );

    long countByBranch_BranchIdAndFitForSportsAndActiveTrue(
            Integer branchId,
            Boolean fitForSports
    );
}