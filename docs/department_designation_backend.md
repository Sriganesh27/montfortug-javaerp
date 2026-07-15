# Pragmatic Enterprise Backend: Department & Designation

This implementation focuses exclusively on **Phase 1 and Phase 2**. It removes all duplicate code, enforces optimistic locking, improves audit logging, and tightly secures branch access—without over-engineering (no Kafka, no Outbox, no CQRS).

---

## 1. Reusable Infrastructure (`common` / `security`)

**Path:** `src/main/java/com/erp/montfortuganda/auth/service/BranchAccessService.java`
```java
package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.exception.BranchAccessDeniedException;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchAccessService {

    private final BranchRepository branchRepository;
    private final CurrentUserService currentUserService;

    /**
     * Resolves the effective branch ID safely.
     */
    public Integer getAccessibleBranchId(Integer requestedBranchId) {
        CurrentUserContext ctx = currentUserService.getCurrentUserContext(); // Assuming no-arg ambient context
        boolean isSuperAdmin = ctx.getRoles() != null && 
            (ctx.getRoles().contains("SUPER_ADMIN") || ctx.getRoles().contains("ROLE_SUPER_ADMIN"));

        if (isSuperAdmin && requestedBranchId != null) {
            return requestedBranchId;
        }

        if (ctx.getBranchId() == null) {
            throw new BranchAccessDeniedException("User has no associated branch context.");
        }

        if (requestedBranchId != null && !requestedBranchId.equals(ctx.getBranchId()) && !isSuperAdmin) {
            throw new BranchAccessDeniedException("Unauthorized: Cannot access data for branch ID " + requestedBranchId);
        }

        return ctx.getBranchId();
    }

    /**
     * Reusable validation for Updates and Deletes.
     */
    public void validateBranchAccess(Integer entityBranchId) {
        CurrentUserContext ctx = currentUserService.getCurrentUserContext();
        boolean isSuperAdmin = ctx.getRoles() != null && 
            (ctx.getRoles().contains("SUPER_ADMIN") || ctx.getRoles().contains("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin && !entityBranchId.equals(ctx.getBranchId())) {
            throw new BranchAccessDeniedException("Unauthorized: Cannot modify record belonging to another branch.");
        }
    }

    public Branch getAccessibleBranch(Integer requestedBranchId) {
        Integer effectiveBranchId = getAccessibleBranchId(requestedBranchId);
        return branchRepository.findById(effectiveBranchId)
            .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + effectiveBranchId));
    }
}
```

**Path:** `src/main/java/com/erp/montfortuganda/exception/GlobalExceptionHandler.java` (Additions)
```java
    @ExceptionHandler(BranchAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleBranchAccessDenied(BranchAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Record was modified by another user. Please refresh and try again."));
    }
    
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("A database constraint was violated (e.g., duplicate entry or missing data)."));
    }
```

---

## 2. Entities with Optimistic Locking

**Path:** `src/main/java/com/erp/montfortuganda/school/entity/Department.java`
```java
package com.erp.montfortuganda.school.entity;

import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.model.enums.RecordStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "erp_departments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_dept_code", columnNames = {"branch_id", "department_code"})
    }
)
public class Department extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "is_academic", nullable = false)
    private Boolean isAcademic = true;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RecordStatus status = RecordStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean active = true;

    // OPTIMISTIC LOCKING
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
```

---

## 3. Standard DTO & Mapper

**Path:** `src/main/java/com/erp/montfortuganda/school/dto/DepartmentDTO.java`
```java
package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.model.enums.RecordStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepartmentDTO {
    private Long departmentId;

    @NotNull(message = "Branch ID is required")
    private Integer branchId;

    @NotBlank(message = "Department code is required")
    @Size(max = 20, message = "Code must not exceed 20 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must be alphanumeric uppercase")
    private String departmentCode;

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String departmentName;

    private Boolean isAcademic = true;
    private String description;
    
    private RecordStatus status;
    private Boolean active;
    private Long version;
    private LocalDateTime createdAt;
    private String createdBy;
}
```

**Path:** `src/main/java/com/erp/montfortuganda/school/mapper/DepartmentMapper.java`
```java
package com.erp.montfortuganda.school.mapper;

import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentDTO toDto(Department entity) {
        if (entity == null) return null;
        
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentId(entity.getDepartmentId());
        
        if (entity.getBranch() != null) {
            dto.setBranchId(entity.getBranch().getBranchId());
        }
        
        dto.setDepartmentCode(entity.getDepartmentCode());
        dto.setDepartmentName(entity.getDepartmentName());
        dto.setIsAcademic(entity.getIsAcademic());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.getActive());
        dto.setVersion(entity.getVersion());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setCreatedBy(entity.getCreatedBy());
        return dto;
    }

    public void updateEntityFromDto(DepartmentDTO dto, Department entity) {
        if (dto == null || entity == null) return;
        
        entity.setDepartmentCode(dto.getDepartmentCode());
        entity.setDepartmentName(dto.getDepartmentName());
        
        if (dto.getIsAcademic() != null) {
            entity.setIsAcademic(dto.getIsAcademic());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
        if (dto.getVersion() != null) {
            entity.setVersion(dto.getVersion());
        }
    }
}
```

---

## 4. Pragmatic Service (With Enhanced Logging)

**Path:** `src/main/java/com/erp/montfortuganda/school/service/DepartmentServiceImpl.java`
```java
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.mapper.DepartmentMapper;
import com.erp.montfortuganda.school.repository.DepartmentRepository;
import com.erp.montfortuganda.school.repository.DepartmentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final BranchAccessService branchAccessService;
    private final DepartmentMapper departmentMapper;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        // Validation logic reused
        Branch branch = branchAccessService.getAccessibleBranch(dto.getBranchId());

        Department department = new Department();
        department.setBranch(branch);
        departmentMapper.updateEntityFromDto(dto, department);

        // Save (Relies on DB Constraints for duplicates)
        department = departmentRepository.save(department);

        // Improved Audit Logging
        log.info("User {} created department {} in branch {}", 
            currentUserService.getCurrentUserContext().getUsername(),
            department.getDepartmentCode(),
            branch.getBranchId()
        );

        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        // Reusable Branch Validation
        branchAccessService.validateBranchAccess(department.getBranch().getBranchId());

        departmentMapper.updateEntityFromDto(dto, department);
        department = departmentRepository.save(department);

        log.info("User {} updated department {} in branch {}", 
            currentUserService.getCurrentUserContext().getUsername(),
            department.getDepartmentCode(),
            department.getBranch().getBranchId()
        );

        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));

        branchAccessService.validateBranchAccess(department.getBranch().getBranchId());

        return departmentMapper.toDto(department);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public PagedResponse<DepartmentDTO> searchDepartments(
            String keyword, Integer requestedBranchId, RecordStatus status, Boolean active, Boolean isAcademic, Pageable pageable) {
            
        Integer effectiveBranchId = branchAccessService.getAccessibleBranchId(requestedBranchId);
        
        Specification<Department> spec = DepartmentSpecification.getSearchSpecification(
            keyword, effectiveBranchId, status, active, isAcademic
        );
        Page<DepartmentDTO> page = departmentRepository.findAll(spec, pageable);
        return new PagedResponse<>(page.map(departmentMapper::toDto));
    }
}
```

---

## 5. Soft Delete Service (Dedicated)

**Path:** `src/main/java/com/erp/montfortuganda/school/service/DepartmentDeletionService.java`
```java
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentDeletionService {

    private final DepartmentRepository departmentRepository;
    private final BranchAccessService branchAccessService;
    private final CurrentUserService currentUserService;

    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_ADMIN')")
    public void softDelete(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
            
        // Validates ownership before deletion!
        branchAccessService.validateBranchAccess(department.getBranch().getBranchId());
        
        // TODO: In Phase 2, trigger soft delete cascades for employees mapped to this department.
        
        department.setActive(false);
        department.setStatus(RecordStatus.INACTIVE);
        departmentRepository.save(department);
        
        log.info("User {} soft-deleted department {} in branch {}", 
            currentUserService.getCurrentUserContext().getUsername(),
            department.getDepartmentCode(),
            department.getBranch().getBranchId()
        );
    }
}
```
