package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<ErpEmployee, Long>, JpaSpecificationExecutor<ErpEmployee> {

    @Query("SELECT new com.erp.montfortuganda.employee.dto.response.EmployeeListResponse(" +
            "e.employeeId, e.employeeNo, e.fullName, d.departmentName, des.designationName, " +
            "CAST(e.employeeCategory AS string), CAST(e.employmentStatus AS string), " +
            "e.officialEmail, e.mobileNo, e.active) " +
            "FROM ErpEmployee e " +
            "LEFT JOIN e.department d " +
            "LEFT JOIN e.designation des " +
            "WHERE e.branch.branchId = :branchId " +
            "AND e.active = true " +
            "AND e.employeeCategory = :category")
    List<EmployeeListResponse> findActiveEmployeesByCategory(
            @Param("branchId") Integer branchId,
            @Param("category") com.erp.montfortuganda.employee.enums.EmployeeCategory category);

    //boolean existsByEmployeeNoAndBranch_BranchId(String employeeNo, Integer branchId);
    
    // Checks for duplicate email (Matches officialEmail in old entity)
    boolean existsByOfficialEmailAndBranch_BranchId(String officialEmail, Integer branchId);

    // Fetches the highest existing sequence
    @Query("SELECT MAX(e.employeeNo) FROM ErpEmployee e WHERE e.branch.branchId = :branchId AND e.employeeNo LIKE :prefix%")
    String findMaxEmployeeNoByPrefix(@Param("branchId") Integer branchId, @Param("prefix") String prefix);
}