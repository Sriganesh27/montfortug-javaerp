# Complete Employee Backend Code

Here is the complete backend codebase for the Employee module. It includes the DTOs, Validator, Specification Builder, Service, and Controller. 

Please copy these files into their respective packages under `com.erp.montfortuganda.employee`.

## 1. DTOs (`com.erp.montfortuganda.employee.dto`)

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeCreateRequest.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeCreateRequest {
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    @NotNull(message = "Designation is required")
    private Long designationId;
    
    private Long reportingManagerId;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String middleName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String title;
    private String gender;
    private LocalDate dateOfBirth;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String phone;
    private String alternatePhone;
    
    @NotNull(message = "Employee category is required")
    private EmployeeCategory category;
    
    @NotNull(message = "Employee type is required")
    private EmployeeType employeeType;
    
    @NotNull(message = "Employment mode is required")
    private EmploymentMode employmentMode;
    
    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;
    
    private String nationalId;
    private String nationality;
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeUpdateRequest.java
package com.erp.montfortuganda.employee.dto;

import lombok.Data;

@Data
public class EmployeeUpdateRequest extends EmployeeCreateRequest {
    // Inherits all fields from CreateRequest.
    // We omit employee_id here because it should be passed as a @PathVariable in the Controller
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeResponse.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmployeeType;
import com.erp.montfortuganda.employee.enums.EmploymentMode;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeResponse {
    private Long employeeId;
    private String employeeNo;
    private String fullName;
    private String email;
    private String phone;
    private Long departmentId;
    private Long designationId;
    private Long reportingManagerId;
    private EmployeeCategory category;
    private EmployeeType employeeType;
    private EmploymentMode employmentMode;
    private EmploymentStatus status;
    private LocalDate joiningDate;
}
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeSearchCriteria.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.enums.EmploymentStatus;
import lombok.Data;

@Data
public class EmployeeSearchCriteria {
    private String keyword; // Searches name, email, employeeNo
    private Long departmentId;
    private Long designationId;
    private EmployeeCategory category;
    private EmploymentStatus status;
}
```

---

## 2. Validator (`com.erp.montfortuganda.employee.validation`)

```java
// File: src/main/java/com/erp/montfortuganda/employee/validation/EmployeeValidator.java
package com.erp.montfortuganda.employee.validation;

import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import com.erp.montfortuganda.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
public class EmployeeValidator {

    private final EmployeeRepository employeeRepository;

    public void validateCreation(EmployeeCreateRequest request, Integer branchId) {
        // 1. Age Validation (Must be at least 18)
        if (request.getDateOfBirth() != null) {
            int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 18) {
                throw new BadRequestException("Employee must be at least 18 years old.");
            }
        }
        
        // 2. Joining Date Validation
        if (request.getJoiningDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Joining date cannot be in the future.");
        }

        // 3. Email Uniqueness (Per branch or global depending on rules)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (employeeRepository.existsByEmailAndBranch_BranchId(request.getEmail(), branchId)) {
                throw new BadRequestException("An employee with this email already exists in this branch.");
            }
        }
    }

    public void validateUpdate(Long employeeId, EmployeeCreateRequest request, Integer branchId) {
        // Prevent Circular Reporting
        if (request.getReportingManagerId() != null && request.getReportingManagerId().equals(employeeId)) {
            throw new BadRequestException("An employee cannot report to themselves.");
        }
    }
}
```

*(Note: You will need to add `boolean existsByEmailAndBranch_BranchId(String email, Integer branchId);` to your `EmployeeRepository`)*

---

## 3. Specification Builder (`com.erp.montfortuganda.employee.specification`)

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
            
            // 2. Active filter (Assuming soft delete flag isActive = 1)
            predicates.add(cb.equal(root.get("isActive"), 1));

            if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
                String pattern = "%" + criteria.getKeyword().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), pattern);
                Predicate codeMatch = cb.like(cb.lower(root.get("employeeNo")), pattern);
                predicates.add(cb.or(nameMatch, emailMatch, codeMatch));
            }

            if (criteria.getDepartmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), criteria.getDepartmentId()));
            }

            if (criteria.getDesignationId() != null) {
                predicates.add(cb.equal(root.get("designationId"), criteria.getDesignationId()));
            }

            if (criteria.getCategory() != null) {
                predicates.add(cb.equal(root.get("category"), criteria.getCategory()));
            }

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

---

## 4. Service (`com.erp.montfortuganda.employee.service`)

```java
// File: src/main/java/com/erp/montfortuganda/employee/service/EmployeeService.java
package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.dto.EmployeeResponse;
import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.dto.EmployeeUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeCreateRequest request);
    EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request);
    EmployeeResponse getEmployeeById(Long employeeId);
    Page<EmployeeResponse> searchEmployees(EmployeeSearchCriteria criteria, Pageable pageable);
    void deleteEmployee(Long employeeId);
}
```

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
import com.erp.montfortuganda.school.BranchRepository;
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
        Integer branchId = branchAccessService.getCurrentBranchId();
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        validator.validateCreation(request, branchId);

        ErpEmployee employee = new ErpEmployee();
        BeanUtils.copyProperties(request, employee);
        
        // Build Full Name
        employee.setFullName(buildFullName(request.getFirstName(), request.getMiddleName(), request.getLastName()));
        
        // Set secure data
        employee.setBranch(branch);
        employee.setStatus(EmploymentStatus.ACTIVE);
        employee.setIsActive(1);
        
        // Generate secure Code
        String generatedCode = codeGenerator.generateCode(branchId, request.getCategory(), request.getJoiningDate());
        employee.setEmployeeNo(generatedCode);

        ErpEmployee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeUpdateRequest request) {
        Integer branchId = branchAccessService.getCurrentBranchId();
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Ensure cross-tenant isolation
        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        validator.validateUpdate(employeeId, request, branchId);

        BeanUtils.copyProperties(request, employee, "employeeId", "employeeNo", "branch", "status", "isActive");
        employee.setFullName(buildFullName(request.getFirstName(), request.getMiddleName(), request.getLastName()));

        ErpEmployee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long employeeId) {
        Integer branchId = branchAccessService.getCurrentBranchId();
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
        Integer branchId = branchAccessService.getCurrentBranchId();
        Specification<ErpEmployee> spec = specificationBuilder.build(criteria, branchId);
        
        return employeeRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long employeeId) {
        Integer branchId = branchAccessService.getCurrentBranchId();
        ErpEmployee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (!employee.getBranch().getBranchId().equals(branchId)) {
            throw new ResourceNotFoundException("Employee not found in your branch");
        }

        employee.setIsActive(0);
        employee.setStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    private String buildFullName(String first, String middle, String last) {
        String name = first;
        if (middle != null && !middle.trim().isEmpty()) name += " " + middle.trim();
        if (last != null && !last.trim().isEmpty()) name += " " + last.trim();
        return name;
    }

    private EmployeeResponse mapToResponse(ErpEmployee entity) {
        EmployeeResponse dto = new EmployeeResponse();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
```

---

## 5. Controller (`com.erp.montfortuganda.employee.controller`)

```java
// File: src/main/java/com/erp/montfortuganda/employee/controller/EmployeeController.java
package com.erp.montfortuganda.employee.controller;

import com.erp.montfortuganda.common.response.ApiResponse;
import com.erp.montfortuganda.employee.dto.EmployeeCreateRequest;
import com.erp.montfortuganda.employee.dto.EmployeeResponse;
import com.erp.montfortuganda.employee.dto.EmployeeSearchCriteria;
import com.erp.montfortuganda.employee.dto.EmployeeUpdateRequest;
import com.erp.montfortuganda.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/branchadmin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('BRANCH_ADMIN', 'HR_MANAGER')") // Adjust roles as needed
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id, 
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched successfully", response));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> searchEmployees(
            @RequestBody EmployeeSearchCriteria criteria, 
            Pageable pageable) {
        Page<EmployeeResponse> responses = employeeService.searchEmployees(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee removed successfully", null));
    }
}
```
