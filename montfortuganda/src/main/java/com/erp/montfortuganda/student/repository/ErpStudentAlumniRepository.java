package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentAlumni;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ErpStudentAlumniRepository
        extends JpaRepository<ErpStudentAlumni, Long>,
        JpaSpecificationExecutor<ErpStudentAlumni> {

    /**
     * Loads one alumni record while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAlumni> findByAlumniIdAndBranch_BranchId(
            Long alumniId,
            Integer branchId
    );

    /**
     * Finds the alumni record belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAlumni> findByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds the active alumni record belonging to a student.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAlumni>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrue(
            Long studentId,
            Integer branchId
    );

    /**
     * Finds an alumni record using the admission number.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentAlumni>
    findByAdmissionNoIgnoreCaseAndBranch_BranchId(
            String admissionNo,
            Integer branchId
    );

    /**
     * Prevents more than one alumni record for the same student.
     */
    boolean existsByStudent_StudentId(
            Long studentId
    );

    /**
     * Branch-safe student alumni existence check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchId(
            Long studentId,
            Integer branchId
    );

    /**
     * Checks whether another alumni record exists during an update.
     */
    boolean existsByStudent_StudentIdAndAlumniIdNot(
            Long studentId,
            Long alumniId
    );

    /**
     * Checks whether a certificate number already exists in the branch.
     */
    boolean existsByCertificateNumberIgnoreCaseAndBranch_BranchIdAndActiveTrue(
            String certificateNumber,
            Integer branchId
    );

    /**
     * Certificate duplicate check during an update.
     */
    boolean existsByCertificateNumberIgnoreCaseAndBranch_BranchIdAndActiveTrueAndAlumniIdNot(
            String certificateNumber,
            Integer branchId,
            Long alumniId
    );

    /**
     * Lists all alumni records belonging to a branch.
     */
    Page<ErpStudentAlumni> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active alumni records belonging to a branch.
     */
    Page<ErpStudentAlumni> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists alumni by graduation year.
     */
    Page<ErpStudentAlumni>
    findByBranch_BranchIdAndGraduationYearAndActiveTrue(
            Integer branchId,
            Integer graduationYear,
            Pageable pageable
    );

    /**
     * Lists alumni whose graduation date falls within a date range.
     */
    Page<ErpStudentAlumni>
    findByBranch_BranchIdAndGraduationDateBetweenAndActiveTrue(
            Integer branchId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    /**
     * Lists alumni by final class.
     */
    Page<ErpStudentAlumni>
    findByBranch_BranchIdAndFinalClassIgnoreCaseAndActiveTrue(
            Integer branchId,
            String finalClass,
            Pageable pageable
    );

    long countByBranch_BranchId(
            Integer branchId
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndGraduationYearAndActiveTrue(
            Integer branchId,
            Integer graduationYear
    );
}