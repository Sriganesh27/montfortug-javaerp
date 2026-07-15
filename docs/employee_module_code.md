# Employee Module Codebase

Copy and paste the following classes into your project under the new `com.erp.montfortuganda.employee` package.

## 1. Enums

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/Gender.java
package com.erp.montfortuganda.employee.enums;
public enum Gender { MALE, FEMALE, OTHER }
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/EmployeeCategory.java
package com.erp.montfortuganda.employee.enums;
public enum EmployeeCategory { TEACHING, NON_TEACHING, MANAGEMENT, SUPPORT_STAFF }
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/EmployeeType.java
package com.erp.montfortuganda.employee.enums;
public enum EmployeeType { PERMANENT, CONTRACT, TEMPORARY, PART_TIME, INTERN, VOLUNTEER }
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/EmploymentMode.java
package com.erp.montfortuganda.employee.enums;
public enum EmploymentMode { FULL_TIME, PART_TIME, REMOTE, ON_CALL }
```

```java
// File: src/main/java/com/erp/montfortuganda/employee/enums/EmploymentStatus.java
package com.erp.montfortuganda.employee.enums;
public enum EmploymentStatus { ACTIVE, PROBATION, ON_LEAVE, SUSPENDED, RESIGNED, RETIRED, TERMINATED }
```

---

## 2. Entity

```java
// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployee.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.employee.enums.*;
import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.entity.Designation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employees", indexes = {
        @Index(name = "idx_emp_branch", columnList = "branch_id"),
        @Index(name = "idx_emp_department", columnList = "department_id"),
        @Index(name = "idx_emp_designation", columnList = "designation_id"),
        @Index(name = "idx_emp_status", columnList = "employment_status"),
        @Index(name = "idx_emp_category", columnList = "employee_category")
})
public class ErpEmployee extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id")
    private Designation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_manager_id")
    private ErpEmployee reportingManager;

    @Column(name = "employee_no", nullable = false, unique = true, length = 50)
    private String employeeNo;

    @Column(length = 20)
    private String title;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "profile_photo", length = 500)
    private String profilePhoto;

    @Column(name = "signature_file", length = 500)
    private String signatureFile;

    @Column(length = 100)
    private String nationality;

    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Column(name = "passport_no", length = 100)
    private String passportNo;

    @Column(name = "tin_number", length = 100)
    private String tinNumber;

    @Column(name = "marital_status", length = 50)
    private String maritalStatus;

    @Column(name = "blood_group", length = 20)
    private String bloodGroup;

    @Column(length = 100)
    private String religion;

    @Column(name = "official_email", length = 150)
    private String officialEmail;

    @Column(name = "personal_email", length = 150)
    private String personalEmail;

    @Column(name = "mobile_no", length = 30)
    private String mobileNo;

    @Column(name = "alternate_mobile", length = 30)
    private String alternateMobile;

    @Column(name = "address_country", length = 100)
    private String addressCountry;

    @Column(name = "address_state", length = 100)
    private String addressState;

    @Column(name = "address_district", length = 100)
    private String addressDistrict;

    @Column(name = "address_village", length = 150)
    private String addressVillage;

    @Column(name = "address_street", length = 255)
    private String addressStreet;

    @Column(name = "postal_code", length = 30)
    private String postalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_category", length = 30)
    private EmployeeCategory employeeCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 30)
    private EmployeeType employeeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_mode", nullable = false, length = 30)
    private EmploymentMode employmentMode = EmploymentMode.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 30)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "probation_end_date")
    private LocalDate probationEndDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "retirement_date")
    private LocalDate retirementDate;

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "work_permit_number", length = 100)
    private String workPermitNumber;

    @Column(name = "work_permit_expiry_date")
    private LocalDate workPermitExpiryDate;

    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    @Column(name = "employment_end_date")
    private LocalDate employmentEndDate;

    @Column(name = "exit_reason", columnDefinition = "TEXT")
    private String exitReason;

    @Column(name = "employee_remarks", columnDefinition = "TEXT")
    private String employeeRemarks;

    @Column(name = "login_enabled", nullable = false)
    private Boolean loginEnabled = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @PrePersist
    @PreUpdate
    private void generateFullName() {
        this.fullName = (firstName != null ? firstName : "") + 
                        (middleName != null && !middleName.isBlank() ? " " + middleName : "") + 
                        (lastName != null && !lastName.isBlank() ? " " + lastName : "");
        this.fullName = this.fullName.trim();
    }
}
```

---

## 3. DTOs

```java
// File: src/main/java/com/erp/montfortuganda/employee/dto/response/EmployeeListResponse.java
package com.erp.montfortuganda.employee.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListResponse {
    private Long employeeId;
    private String employeeNo;
    private String fullName;
    private String departmentName;
    private String designationName;
    private String employeeCategory;
    private String employmentStatus;
    private String officialEmail;
    private String mobileNo;
    private Boolean active;
}
```

---

## 4. Repository

```java
// File: src/main/java/com/erp/montfortuganda/employee/repository/EmployeeRepository.java
package com.erp.montfortuganda.employee.repository;

import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.entity.ErpEmployee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<ErpEmployee, Long>, JpaSpecificationExecutor<ErpEmployee> {

    // Projection query for high performance dropdowns (e.g. for Admission Interviews)
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

    boolean existsByEmployeeNoAndBranch_BranchId(String employeeNo, Integer branchId);
}
```

---

## 5. Service Interface

```java
// File: src/main/java/com/erp/montfortuganda/employee/service/EmployeeService.java
package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;

import java.util.List;

public interface EmployeeService {
    
    // Core method needed right now to unblock Admission Module interviews
    List<EmployeeListResponse> getActiveTeachers(CurrentUserContext ctx);
}
```

---

## 6. Service Implementation

```java
// File: src/main/java/com/erp/montfortuganda/employee/service/EmployeeServiceImpl.java
package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.enums.EmployeeCategory;
import com.erp.montfortuganda.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BranchAccessService branchAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeListResponse> getActiveTeachers(CurrentUserContext ctx) {
        // 1. Strictly enforce branch access
        Integer branchId = branchAccessService.getValidatedBranchId(ctx);
        
        // 2. Fetch using projection to avoid N+1 and massive payloads
        return employeeRepository.findActiveEmployeesByCategory(branchId, EmployeeCategory.TEACHING);
    }
}
```

---

## 7. Controller

```java
// File: src/main/java/com/erp/montfortuganda/employee/controller/BranchEmployeeController.java
package com.erp.montfortuganda.employee.controller;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.common.dto.ApiResponse;
import com.erp.montfortuganda.employee.dto.response.EmployeeListResponse;
import com.erp.montfortuganda.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branchadmin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BRANCH_ADMIN')")
public class BranchEmployeeController {

    private final EmployeeService employeeService;
    private final CurrentUserService currentUserService;

    @GetMapping("/teachers")
    public ResponseEntity<ApiResponse<List<EmployeeListResponse>>> getActiveTeachers(Authentication principal) {
        CurrentUserContext ctx = currentUserService.getCurrentUserContext(principal);
        List<EmployeeListResponse> teachers = employeeService.getActiveTeachers(ctx);
        return ResponseEntity.ok(ApiResponse.success("Teachers fetched successfully", teachers));
    }
}
```

---

## 8. Excel Import (Foundation)

Since bulk excel import relies heavily on Apache POI and your `EmployeeValidator`, here is the architectural skeleton you can inject your POI logic into.

```java
// File: src/main/java/com/erp/montfortuganda/employee/service/EmployeeImportService.java
package com.erp.montfortuganda.employee.service;

import com.erp.montfortuganda.auth.service.CurrentUserContext;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface EmployeeImportService {
    // Returns a map of success count and failure logs
    Map<String, Object> processExcelImport(MultipartFile file, CurrentUserContext ctx);
}
```
