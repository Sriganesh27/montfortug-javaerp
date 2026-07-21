package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Branch-safe persistence operations for the Employee master table.

 * Search filtering is implemented separately with JpaSpecificationExecutor.
 * Service methods must use branch-scoped lookups for browser-supplied
 * Employee IDs.
 */
@Repository
public interface ErpEmployeeRepository
        extends JpaRepository<ErpEmployee, Long>,
        JpaSpecificationExecutor<ErpEmployee> {

    /**
     * Loads the complete Employee master relationships required by the detail
     * response while also enforcing branch ownership.
     */
    @EntityGraph(
            attributePaths = {
                    "branch",
                    "department",
                    "designation",
                    "reportingManager",
                    "reportingManager.department",
                    "reportingManager.designation",
                    "user",
                    "user.userRoles",
                    "user.userRoles.role"
            }
    )
    Optional<ErpEmployee> findByEmployeeIdAndBranch_BranchId(
            Long employeeId,
            Integer branchId
    );

    /**
     * Branch-safe lookup for an active reporting manager.
     */
    @EntityGraph(
            attributePaths = {
                    "branch",
                    "department",
                    "designation"
            }
    )
    Optional<ErpEmployee> findByEmployeeIdAndBranch_BranchIdAndActiveTrue(
            Long employeeId,
            Integer branchId
    );

    Optional<ErpEmployee> findByEmployeeNoIgnoreCase(
            String employeeNo
    );

    Optional<ErpEmployee> findByEmployeeNoIgnoreCaseAndBranch_BranchId(
            String employeeNo,
            Integer branchId
    );

    Optional<ErpEmployee> findByUser_Id(
            Integer userId
    );

    boolean existsByEmployeeNoIgnoreCase(
            String employeeNo
    );

    boolean existsByUser_Id(
            Integer userId
    );

    boolean existsByBranch_BranchIdAndOfficialEmailIgnoreCase(
            Integer branchId,
            String officialEmail
    );

    boolean existsByBranch_BranchIdAndOfficialEmailIgnoreCaseAndEmployeeIdNot(
            Integer branchId,
            String officialEmail,
            Long employeeId
    );

    boolean existsByBranch_BranchIdAndNationalIdIgnoreCase(
            Integer branchId,
            String nationalId
    );

    boolean existsByBranch_BranchIdAndNationalIdIgnoreCaseAndEmployeeIdNot(
            Integer branchId,
            String nationalId,
            Long employeeId
    );

    boolean existsByBranch_BranchIdAndPassportNoIgnoreCase(
            Integer branchId,
            String passportNo
    );

    boolean existsByBranch_BranchIdAndPassportNoIgnoreCaseAndEmployeeIdNot(
            Integer branchId,
            String passportNo,
            Long employeeId
    );

    boolean existsByBranch_BranchIdAndTinNumberIgnoreCase(
            Integer branchId,
            String tinNumber
    );

    boolean existsByBranch_BranchIdAndTinNumberIgnoreCaseAndEmployeeIdNot(
            Integer branchId,
            String tinNumber,
            Long employeeId
    );

    boolean existsByBranch_BranchIdAndWorkPermitNumberIgnoreCase(
            Integer branchId,
            String workPermitNumber
    );

    boolean existsByBranch_BranchIdAndWorkPermitNumberIgnoreCaseAndEmployeeIdNot(
            Integer branchId,
            String workPermitNumber,
            Long employeeId
    );

    boolean existsByBranch_BranchIdAndReportingManager_EmployeeId(
            Integer branchId,
            Long reportingManagerId
    );

    List<ErpEmployee> findAllByBranch_BranchIdAndActiveTrueOrderByFullNameAsc(
            Integer branchId
    );

    long countByBranch_BranchId(
            Integer branchId
    );
}