# Final Backend Fixes

I see exactly what is happening! 

1. **Service Method:** Your `BranchAccessService` uses `getAccessibleBranchId(null)` instead of `getCurrentBranchId()`.
2. **Entity Properties:** You are using the original `ErpEmployee` entity from our Admissions mockup, which has slightly different property names (e.g., `employmentStatus` instead of `status`, `active` boolean instead of `isActive` integer, `officialEmail` instead of `email`, and `employeeCategory` instead of `category`).

Because `BeanUtils.copyProperties` relies on the names being strictly identical, we just need to manually map those specific fields, and update the method calls!

Here are the fully corrected files to match your exact existing structure.

---

## 1. Updated EmployeeServiceImpl
Replace your entire `EmployeeServiceImpl.java` with this. It fixes the `BranchAccessService` calls, uses the correct entity setters (`setActive`, `setEmploymentStatus`), and handles the mismatched property names manually!

```java
// File: src/main/java/com/erp/montfortuganda/employee/service/impl/EmployeeServiceImpl.java
package com.erp.montfortuganda.employee.service.impl;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.dto.EmployeeResponse;
import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.dto.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import com.erp.montfortuganda.employee.generator.EmployeeCodeGenerator;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import com.erp.montfortuganda.employee.service.EmployeeService;
import com.erp.montfortuganda.employee.specification.EmployeeSpecificationBuilder;
import com.erp.montfortuganda.employee.validation.EmployeeValidator;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final BranchAccessService branchAccessService;
    private final EmployeeCodeGenerator codeGenerator;
    private final EmployeeValidator validator;
    private final EmployeeSpecificationBuilder specificationBuilder;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        // Fix 1: Use getAccessibleBranchId(null)
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        validator.validateCreation(request, branchId);

        ErpEmployee employee = new ErpEmployee();
        BeanUtils.copyProperties(request, employee);
        
        // Manual mapping for mismatched names between DTO and your ErpEmployee entity
        employee.setOfficialEmail(request.getEmail());
        employee.setMobileNo(request.getPhone());
        employee.setEmployeeCategory(request.getCategory());
        
        // Set secure data
        employee.setBranch(branch);
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setActive(true);
        
        // Generate secure Code
        String generatedCode = codeGenerator.generateCode(branchId, request.getCategory(), request.getJoiningDate());
        employee.setEmployeeNo(generatedCode);

        ErpEmployee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        validator.validateUpdate(employeeId, request, branchId);

        BeanUtils.copyProperties(request, employee, "employeeId", "employeeNo", "branch", "employmentStatus", "active");
        
        // Manual mapping for mismatched names
        employee.setOfficialEmail(request.getEmail());
        employee.setMobileNo(request.getPhone());
        employee.setEmployeeCategory(request.getCategory());

        ErpEmployee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        return mapToResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        Specification<ErpEmployee> spec = specificationBuilder.build(criteria, branchId);
        
        return employeeRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        employee.setActive(false);
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.erp.montfortuganda.employee.dto.response.EmployeeListResponse> getActiveTeachers() {
        Integer branchId = branchAccessService.getAccessibleBranchId(null);
        return employeeRepository.findActiveEmployeesByCategory(
                branchId, 
                com.erp.montfortuganda.employee.enums.EmployeeCategory.TEACHING
        );
    }

    private EmployeeResponse mapToResponse(ErpEmployee entity) {
        EmployeeResponse dto = new EmployeeResponse();
        BeanUtils.copyProperties(entity, dto);
        
        // Map back to DTO
        dto.setEmail(entity.getOfficialEmail());
        dto.setPhone(entity.getMobileNo());
        dto.setCategory(entity.getEmployeeCategory());
        dto.setStatus(entity.getEmploymentStatus());
        
        if (entity.getDepartment() != null) dto.setDepartmentId(entity.getDepartment().getDepartmentId());
        if (entity.getDesignation() != null) dto.setDesignationId(entity.getDesignation().getDesignationId());
        if (entity.getReportingManager() != null) dto.setReportingManagerId(entity.getReportingManager().getEmployeeId());
        
        return dto;
    }
}
```

---

## 2. Updated EmployeeSpecificationBuilder
Replace your `EmployeeSpecificationBuilder.java`. The JPQL search must also use your actual property names (`officialEmail`, `employeeCategory`, `employmentStatus`, `active`).

```java
// File: src/main/java/com/erp/montfortuganda/employee/specification/EmployeeSpecificationBuilder.java
package com.erp.montfortuganda.employee.specification;

import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeeSpecificationBuilder {

    public Specification<ErpEmployee> build(EmployeeSearchCriteria criteria, Integer branchId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Always filter by branch!
            predicates.add(cb.equal(root.get("branch").get("branchId"), branchId));
            
            // 2. Active filter (Boolean true)
            predicates.add(cb.isTrue(root.get("active")));

            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("officialEmail")), pattern);
                Predicate codeMatch = cb.like(cb.lower(root.get("employeeNo")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, codeMatch));
            }

            if (criteria.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("department").get("departmentId"), criteria.getDepartmentId()));
            }

            if (criteria.getDesignationId() != null) {
                predicates.add(cb.equal(root.get("designation").get("designationId"), criteria.getDesignationId()));
            }

            if (criteria.getCategory() != null) {
                predicates.add(cb.equal(root.get("employeeCategory"), criteria.getCategory()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("employmentStatus"), criteria.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```
