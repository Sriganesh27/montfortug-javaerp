package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeQualification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-safe persistence operations for Employee qualifications.

 * Every browser-supplied qualification or Employee ID is resolved together
 * with the authenticated branch ID to prevent cross-branch access.
 */
@Repository
public interface ErpEmployeeQualificationRepository
        extends JpaRepository<ErpEmployeeQualification, Long> {

    /**
     * Returns all qualifications for one Employee, with the latest completion
     * year first.
     */
    @EntityGraph(
            attributePaths = {
                    "employee",
                    "employeeQualificationVerifiedBy"
            }
    )
    List<ErpEmployeeQualification>
    findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeQualificationCompletionYearDescEmployeeQualificationIdDesc(
            Long employeeId,
            Integer branchId
    );

    /**
     * Branch-safe lookup used while viewing, updating or deleting one
     * qualification.
     */
    @EntityGraph(
            attributePaths = {
                    "employee",
                    "employeeQualificationVerifiedBy"
            }
    )
    Optional<ErpEmployeeQualification>
    findByEmployeeQualificationIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeQualificationId,
            Long employeeId,
            Integer branchId
    );

    /**
     * Checks for a duplicate certificate number within the same Employee.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeQualificationCertificateNumberIgnoreCase(
            Long employeeId,
            Integer branchId,
            String certificateNumber
    );

    /**
     * Duplicate certificate-number check used during updates while excluding
     * the qualification currently being edited.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeQualificationCertificateNumberIgnoreCaseAndEmployeeQualificationIdNot(
            Long employeeId,
            Integer branchId,
            String certificateNumber,
            Long employeeQualificationId
    );

    /**
     * Checks for a duplicate registration number within the same Employee.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeQualificationRegistrationNumberIgnoreCase(
            Long employeeId,
            Integer branchId,
            String registrationNumber
    );

    /**
     * Duplicate registration-number check used during updates while excluding
     * the qualification currently being edited.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeQualificationRegistrationNumberIgnoreCaseAndEmployeeQualificationIdNot(
            Long employeeId,
            Integer branchId,
            String registrationNumber,
            Long employeeQualificationId
    );

    long countByEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeId,
            Integer branchId
    );

    void deleteAllByEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeId,
            Integer branchId
    );
}