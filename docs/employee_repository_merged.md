# Fully Merged Employee Repository

Thank you for pasting that! I see a few things:
1. You are missing the `existsByEmailAndBranch_BranchId` method required by `EmployeeValidator`.
2. You are missing the `findMaxEmployeeNoByPrefix` required by the `EmployeeCodeGenerator`.
3. **CRITICAL:** Your `@Query` uses the database column names (like `officialEmail`, `mobileNo`) instead of the Java property names (like `email`, `phone`). JPQL queries must use the exact Java property names from the `ErpEmployee` entity, otherwise the application will crash when it tries to run that query!

Here is your **completely merged and corrected** `EmployeeRepository.java`. Please replace the entire file with this:

```java
// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<ErpEmployee, Long>, JpaSpecificationExecutor<ErpEmployee> {

    // PROJECTION QUERY (Corrected to use the exact Java property names from ErpEmployee entity)
    @Query("SELECT new com.erp.montfortuganda.employee.dto.response.EmployeeListResponse(" +
            "e.employeeId, e.employeeNo, e.fullName, d.departmentName, des.designationName, " +
            "CAST(e.category AS string), CAST(e.status AS string), " +
            "e.email, e.phone, e.isActive) " +
            "FROM ErpEmployee e " +
            "LEFT JOIN e.department d " +
            "LEFT JOIN e.designation des " +
            "WHERE e.branch.branchId = :branchId " +
            "AND e.isActive = 1 " +
            "AND e.category = :category")
    List<EmployeeListResponse> findActiveEmployeesByCategory(
            @Param("branchId") Integer branchId,
            @Param("category") com.erp.montfortuganda.employee.enums.EmployeeCategory category);

    // Checks for duplicate employee code
    boolean existsByEmployeeNoAndBranch_BranchId(String employeeNo, Integer branchId);
    
    // Checks for duplicate email (Required by EmployeeValidator)
    boolean existsByEmailAndBranch_BranchId(String email, Integer branchId);

    // Fetches the highest existing sequence (Required by EmployeeCodeGenerator)
    @Query("SELECT MAX(e.employeeNo) FROM ErpEmployee e WHERE e.branch.branchId = :branchId AND e.employeeNo LIKE :prefix%")
    String findMaxEmployeeNoByPrefix(@Param("branchId") Integer branchId, @Param("prefix") String prefix);
}
```
