package com.erp.montfortuganda.student.repository;

import com.erp.montfortuganda.student.entity.ErpStudentTransport;
import com.erp.montfortuganda.student.entity.ErpStudentTransport.PaymentStatus;
import com.erp.montfortuganda.student.entity.ErpStudentTransport.TransportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ErpStudentTransportRepository
        extends JpaRepository<ErpStudentTransport, Long> {

    /**
     * Loads one transport record while enforcing branch ownership.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentTransport> findByTransportIdAndBranch_BranchId(
            Long transportId,
            Integer branchId
    );

    /**
     * Finds a student's transport record for an academic year.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentTransport>
    findByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCase(
            Long studentId,
            Integer branchId,
            String academicYear
    );

    /**
     * Finds an active transport record for a student and academic year.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentTransport>
    findByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCaseAndActiveTrue(
            Long studentId,
            Integer branchId,
            String academicYear
    );

    /**
     * Finds transport information using admission number and academic year.
     */
    @EntityGraph(attributePaths = {"student", "branch"})
    Optional<ErpStudentTransport>
    findByAdmissionNoIgnoreCaseAndBranch_BranchIdAndAcademicYearIgnoreCase(
            String admissionNo,
            Integer branchId,
            String academicYear
    );

    /**
     * Prevents duplicate transport records for the same student and year.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCase(
            Long studentId,
            String academicYear
    );

    /**
     * Branch-safe duplicate transport check.
     */
    boolean existsByStudent_StudentIdAndBranch_BranchIdAndAcademicYearIgnoreCase(
            Long studentId,
            Integer branchId,
            String academicYear
    );

    /**
     * Duplicate check during transport-record update.
     */
    boolean existsByStudent_StudentIdAndAcademicYearIgnoreCaseAndTransportIdNot(
            Long studentId,
            String academicYear,
            Long transportId
    );

    /**
     * Checks whether a seat is already assigned to another active student.
     */
    boolean existsByBranch_BranchIdAndAcademicYearIgnoreCaseAndVehicleIdAndSeatNumberIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear,
            Long vehicleId,
            String seatNumber
    );

    /**
     * Checks seat assignment while updating an existing record.
     */
    boolean existsByBranch_BranchIdAndAcademicYearIgnoreCaseAndVehicleIdAndSeatNumberIgnoreCaseAndActiveTrueAndTransportIdNot(
            Integer branchId,
            String academicYear,
            Long vehicleId,
            String seatNumber,
            Long transportId
    );

    /**
     * Lists all transport records belonging to a branch.
     */
    Page<ErpStudentTransport> findByBranch_BranchId(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active transport records belonging to a branch.
     */
    Page<ErpStudentTransport> findByBranch_BranchIdAndActiveTrue(
            Integer branchId,
            Pageable pageable
    );

    /**
     * Lists active transport records for an academic year.
     */
    Page<ErpStudentTransport>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear,
            Pageable pageable
    );

    /**
     * Lists students assigned to a route.
     */
    Page<ErpStudentTransport>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndRouteIdAndActiveTrue(
            Integer branchId,
            String academicYear,
            Long routeId,
            Pageable pageable
    );

    /**
     * Lists students assigned to a vehicle.
     */
    Page<ErpStudentTransport>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndVehicleIdAndActiveTrue(
            Integer branchId,
            String academicYear,
            Long vehicleId,
            Pageable pageable
    );

    /**
     * Lists students assigned to a pickup point.
     */
    Page<ErpStudentTransport>
    findByBranch_BranchIdAndAcademicYearIgnoreCaseAndPickupPointIdAndActiveTrue(
            Integer branchId,
            String academicYear,
            Long pickupPointId,
            Pageable pageable
    );

    /**
     * Lists records by transport status.
     */
    Page<ErpStudentTransport>
    findByBranch_BranchIdAndTransportStatusAndActiveTrue(
            Integer branchId,
            TransportStatus transportStatus,
            Pageable pageable
    );

    /**
     * Lists records by payment status.
     */
    Page<ErpStudentTransport>
    findByBranch_BranchIdAndPaymentStatusAndActiveTrue(
            Integer branchId,
            PaymentStatus paymentStatus,
            Pageable pageable
    );

    /**
     * Finds active transport records ending on or before a date.
     */
    List<ErpStudentTransport>
    findByBranch_BranchIdAndTransportEndDateLessThanEqualAndActiveTrue(
            Integer branchId,
            LocalDate transportEndDate
    );

    long countByBranch_BranchIdAndActiveTrue(
            Integer branchId
    );

    long countByBranch_BranchIdAndAcademicYearIgnoreCaseAndActiveTrue(
            Integer branchId,
            String academicYear
    );

    long countByBranch_BranchIdAndTransportStatusAndActiveTrue(
            Integer branchId,
            TransportStatus transportStatus
    );

    long countByBranch_BranchIdAndPaymentStatusAndActiveTrue(
            Integer branchId,
            PaymentStatus paymentStatus
    );
}