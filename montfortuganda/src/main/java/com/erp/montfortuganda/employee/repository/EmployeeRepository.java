package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<ErpEmployee, Long>,
        JpaSpecificationExecutor<ErpEmployee> {

    /**
     * Loads an employee only when it belongs to the authenticated branch.
     */
    @EntityGraph(attributePaths = {
            "branch",
            "department",
            "designation",
            "reportingManager",
            "user"
    })
    Optional<ErpEmployee> findByEmployeeIdAndBranch_BranchId(
            Long employeeId,
            Integer branchId
    );

    /**
     * Checks duplicate official email during employee creation.
     */
    boolean existsByOfficialEmailIgnoreCaseAndBranch_BranchId(
            String officialEmail,
            Integer branchId
    );

    /**
     * Checks duplicate official email during employee update,
     * excluding the employee currently being edited.
     */
    boolean existsByOfficialEmailIgnoreCaseAndBranch_BranchIdAndEmployeeIdNot(
            String officialEmail,
            Integer branchId,
            Long employeeId
    );

    /**
     * Confirms whether a user account is already linked to an employee.
     */
    boolean existsByUser_Id(
            Integer userId
    );

    /**
     * Returns active teaching staff.

     * The supplied categories will be:
     * TEACHING and MANAGEMENT_TEACHING.
     */
    @Query("""
            SELECT new com.erp.montfortuganda.employee.dto.response.EmployeeListResponse(
                e.employeeId,
                e.employeeNo,
                e.fullName,
                d.departmentName,
                des.designationName,
                CAST(e.employeeCategory AS string),
                CAST(e.employmentStatus AS string),
                e.officialEmail,
                e.mobileNo,
                e.active
            )
            FROM ErpEmployee e
            LEFT JOIN e.department d
            LEFT JOIN e.designation des
            WHERE e.branch.branchId = :branchId
              AND e.active = true
              AND e.employeeCategory IN :categories
            ORDER BY e.fullName ASC
            """)
    List<EmployeeListResponse> findActiveEmployeesByCategories(
            @Param("branchId") Integer branchId,
            @Param("categories")
            Collection<EmployeeCategory> categories
    );
    @Query("""
        SELECT new com.erp.montfortuganda.employee.dto.response.EmployeeListResponse(
            e.employeeId,
            e.employeeNo,
            e.fullName,
            d.departmentName,
            des.designationName,
            CAST(e.employeeCategory AS string),
            CAST(e.employmentStatus AS string),
            e.officialEmail,
            e.mobileNo,
            e.active
        )
        FROM ErpEmployee e
        LEFT JOIN e.department d
        LEFT JOIN e.designation des
        WHERE e.branch.branchId = :branchId
          AND e.active = true
          AND e.employeeCategory = :category
        ORDER BY e.fullName ASC
        """)
    List<EmployeeListResponse> findActiveEmployeesByCategory(
            @Param("branchId") Integer branchId,
            @Param("category") EmployeeCategory category
    );
}