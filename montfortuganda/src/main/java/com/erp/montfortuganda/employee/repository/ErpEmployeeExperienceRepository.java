package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployeeExperience;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Branch-safe persistence operations for Employee experience records.

 * Every browser-supplied experience or Employee ID is resolved together with
 * the authenticated branch ID to prevent cross-branch access.
 */
@Repository
public interface ErpEmployeeExperienceRepository
        extends JpaRepository<ErpEmployeeExperience, Long> {

    /**
     * Returns all experience records for one Employee, with the most recent
     * employment first.
     */
    @EntityGraph(
            attributePaths = {
                    "employee",
                    "employeeExperienceVerifiedBy"
            }
    )
    List<ErpEmployeeExperience>
    findAllByEmployee_EmployeeIdAndEmployee_Branch_BranchIdOrderByEmployeeExperienceStartDateDescEmployeeExperienceIdDesc(
            Long employeeId,
            Integer branchId
    );

    /**
     * Branch-safe lookup used while viewing, updating or deleting one
     * experience record.
     */
    @EntityGraph(
            attributePaths = {
                    "employee",
                    "employeeExperienceVerifiedBy"
            }
    )
    Optional<ErpEmployeeExperience>
    findByEmployeeExperienceIdAndEmployee_EmployeeIdAndEmployee_Branch_BranchId(
            Long employeeExperienceId,
            Long employeeId,
            Integer branchId
    );

    /**
     * Prevents accidental duplicate experience entries for the same company,
     * designation and start date.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeExperienceCompanyNameIgnoreCaseAndEmployeeExperienceDesignationIgnoreCaseAndEmployeeExperienceStartDate(
            Long employeeId,
            Integer branchId,
            String companyName,
            String designation,
            LocalDate startDate
    );

    /**
     * Duplicate check used during updates while excluding the current record.
     */
    boolean
    existsByEmployee_EmployeeIdAndEmployee_Branch_BranchIdAndEmployeeExperienceCompanyNameIgnoreCaseAndEmployeeExperienceDesignationIgnoreCaseAndEmployeeExperienceStartDateAndEmployeeExperienceIdNot(
            Long employeeId,
            Integer branchId,
            String companyName,
            String designation,
            LocalDate startDate,
            Long employeeExperienceId
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