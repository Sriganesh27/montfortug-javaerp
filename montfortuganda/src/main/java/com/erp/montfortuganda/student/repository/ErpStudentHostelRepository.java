package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentHostel;
import com.erp.montfortuganda.student.entity.ErpStudentHostel.AllocationStatus;
import com.erp.montfortuganda.student.entity.ErpStudentHostel.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentHostelRepository
        extends JpaRepository<ErpStudentHostel, Long> {

    /**
     * Loads one hostel allocation while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentHostel>
    findByHostelAllocationIdAndBranch_BranchId(
            Long hostelAllocationId,
            Integer branchId
    );

    /**
     * Finds a student's hostel allocation for an academic year.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentHostel>
    findByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCase(
            Long studentId,
            Integer branchId,
            String academicYear
    );

    /**
     * Finds the student's active hostel allocation.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentHostel>
    findByStudent_StudentIdAndBranch_BranchIdAndActiveTrueAndAllocationStatus(
            Long studentId,
            Integer branchId,
            AllocationStatus allocationStatus
    );

    /**
     * Finds hostel allocation using the admission number and academic year.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentHostel>
    findByAdmissionNoIgnoreCaseAndBranch_BranchIdAndAcademicYearIgnoreCase(
            String admissionNo,
            Integer branchId,
            String academicYear
    );

    /**
     * Prevents duplicate hostel allocation for the same student and year.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCase(
            Long studentId,
            String academicYear
    );

    /**
     * Branch-safe duplicate allocation check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCase(
            Long studentId,
            Integer branchId,
            String academicYear
    );

    /**
     * Duplicate check during hostel allocation update.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCaseAndHostelAllocationIdNot(
            Long studentId,
            String academicYear,
            Long hostelAllocationId
    );

    /**
     * Checks whether a bed is already actively assigned.
     */
    boolean existsByBranch_BranchIdAndAcademicYearIgnoreCaseAndBedIdAndActiveTrueAndAllocationStatus(
            Integer branchId,
            String academicYear,
            Long bedId,
            AllocationStatus allocationStatus
    );

    /**
     * Checks bed allocation during an update.
     */
    boolean existsByBranch_BranchIdAndAcademicYearIgnoreCaseAndBedIdAndActiveTrueAndAllocationStatusAndHostelAllocationIdNot(
            Integer branchId,
            String academicYear,
            Long bedId,
            AllocationStatus allocationStatus,
            Long hostelAllocationId
    );

    /**
     * Lists all hostel allocations belonging to a branch.
     */
    Page<ErpStudentHostel> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active hostel allocations belonging to a branch.
     */
    Page<ErpStudentHostel> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active allocations for an academic year.
     */
    Page<ErpStudentHostel>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear,
            Pageable pageable
    );

    /**
     * Lists allocations belonging to a hostel.
     */
    Page<ErpStudentHostel>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndHostelIdAndActiveTrue(
            Integer branchId,
            String academicYear,
            Long hostelId,
            Pageable pageable
    );

    /**
     * Lists allocations belonging to a room.
     */
    Page<ErpStudentHostel>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndRoomIdAndActiveTrue(
            Integer branchId,
            String academicYear,
            Long roomId,
            Pageable pageable
    );

    /**
     * Lists allocations by allocation status.
     */
    Page<ErpStudentHostel>
    findByBranch_BranchIdAndAllocationStatusAndActiveTrue(
            Integer branchId,
            AllocationStatus allocationStatus,
            Pageable pageable
    );

    /**
     * Lists allocations by payment status.
     */
    Page<ErpStudentHostel>
    findByBranch_BranchIdAndPaymentStatusAndActiveTrue(
            Integer branchId,
            PaymentStatus paymentStatus,
            Pageable pageable
    );

    /**
     * Finds active allocations ending on or before a date.
     */
    List<ErpStudentHostel>
    findByBranch_BranchIdAndAllocationEndDateLessThanEqualAndActiveTrue(
            Integer branchId,
            LocalDate allocationEndDate
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndAcademicYearIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear
    );

    long countByBranch_BranchIdAndAllocationStatusAndActiveTrue(
            Integer branchId,
            AllocationStatus allocationStatus
    );

    long countByBranch_BranchIdAndPaymentStatusAndActiveTrue(
            Integer branchId,
            PaymentStatus paymentStatus
    );
}