# Final Compilation Fixes

You are extremely close! The build is failing simply because those two files are missing the `import` statements at the top of the file to pull in the new classes we created.

Here are the complete files with the **exact imports** you need to resolve those errors.

---

### 1. `DepartmentDeletionService.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/service/DepartmentDeletionService.java`
*(Notice the `import com.erp.montfortuganda.school.repository.DepartmentRepository;` at the top)*

```java
package com.erp.montfortuganda.school.service;

import com.erp.montfortuganda.auth.service.BranchAccessService;
import com.erp.montfortuganda.auth.service.CurrentUserService;
import com.erp.montfortuganda.exception.ResourceNotFoundException;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Department;
import com.erp.montfortuganda.school.repository.DepartmentRepository; // <--- FIXES YOUR ERROR
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

---

### 2. `GlobalExceptionHandler.java` (Imports & New Handlers)
**Location:** `src/main/java/com/erp/montfortuganda/exception/GlobalExceptionHandler.java`

You just need to add the `ApiResponse` import to the top of your existing `GlobalExceptionHandler.java` file, and ensure your new handler methods are inside the class.

**Add these imports at the very top of the file:**
```java
import com.erp.montfortuganda.common.dto.ApiResponse; // <--- FIXES YOUR ERROR
import com.erp.montfortuganda.exception.BranchAccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
```

**And make sure your handler methods look exactly like this inside the class:**
```java
    @ExceptionHandler(BranchAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleBranchAccessDenied(BranchAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLocking(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Record was modified by another user. Please refresh and try again."));
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("A database constraint was violated (e.g., duplicate entry or missing data)."));
    }
```
