package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeContact;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-safe persistence operations for Employee contacts.

 * Every browser-supplied contact or Employee ID must be resolved together with
 * the authenticated branch ID. This prevents cross-branch contact access.
 */
@Repository
public interface ErpEmployeeContactRepository
        extends JpaRepository<ErpEmployeeContact, Long> {

    /**
     * Returns all contacts for one Employee, with the primary contact first.
     */
    @EntityGraph(
            attributePaths = {
                    "employee"
            }
    )
    List<ErpEmployeeContact>
    findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeContactIsPrimaryDescEmployeeContactNameAsc(
            Long employeeId,
            Integer branchId
    );

    /**
     * Branch-safe lookup used while updating or deleting one contact.
     */
    @EntityGraph(
            attributePaths = {
                    "employee"
            }
    )
    Optional<ErpEmployeeContact>
    findByEmployeeContactIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeContactId,
            Long employeeId,
            Integer branchId
    );

    /**
     * Checks whether the Employee already has an active primary contact.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeContactIsPrimaryTrueAndEmployeeContactActiveTrue(
            Long employeeId,
            Integer branchId
    );

    /**
     * Primary-contact duplicate check used during updates while excluding the
     * contact currently being edited.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeContactIsPrimaryTrueAndEmployeeContactActiveTrueAndEmployeeContactIdNot(
            Long employeeId,
            Integer branchId,
            Long employeeContactId
    );

    /**
     * Checks for a duplicate active mobile number within the same Employee.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeContactMobileIgnoreCaseAndEmployeeContactActiveTrue(
            Long employeeId,
            Integer branchId,
            String employeeContactMobile
    );

    /**
     * Duplicate-mobile check used during updates while excluding the current
     * contact.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeContactMobileIgnoreCaseAndEmployeeContactActiveTrueAndEmployeeContactIdNot(
            Long employeeId,
            Integer branchId,
            String employeeContactMobile,
            Long employeeContactId
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