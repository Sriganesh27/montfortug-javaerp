package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErpEmployeeRepository extends JpaRepository<ErpEmployee, Long> {
    long countByDepartment_DepartmentIdAndActiveTrue(Long departmentId);

    // ADD THIS LINE
    long countByDesignation_DesignationIdAndActiveTrue(Long designationId);
}