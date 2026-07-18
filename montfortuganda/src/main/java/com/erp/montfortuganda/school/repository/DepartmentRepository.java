package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.school.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, Long>,
        JpaSpecificationExecutor<Department> {

    boolean existsByBranch_BranchIdAndDepartmentCodeIgnoreCase(
            Integer branchId,
            String departmentCode
    );

    boolean existsByBranch_BranchIdAndDepartmentNameIgnoreCase(
            Integer branchId,
            String departmentName
    );

    boolean existsByBranch_BranchIdAndDepartmentCodeIgnoreCaseAndDepartmentIdNot(
            Integer branchId,
            String departmentCode,
            Long departmentId
    );

    boolean existsByBranch_BranchIdAndDepartmentNameIgnoreCaseAndDepartmentIdNot(
            Integer branchId,
            String departmentName,
            Long departmentId
    );
}