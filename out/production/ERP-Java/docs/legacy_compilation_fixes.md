# Legacy Compatibility & Spring Data 3 Fixes

You are running into two interesting edge cases here! 

1. Your old dashboard and admission services are still trying to pass the `CurrentUserContext` directly to `branchAccessService.validateBranchAccess(ctx)` and `getValidatedBranchId(ctx)`. I changed those methods to use the ambient context behind the scenes, which broke your old services. 
2. You are using a modern version of Spring Data JPA (3.x) where `Specification.where(null)` is suddenly an ambiguous method call due to a newly added `PredicateSpecification` interface in the Spring framework.

Here is the exact code to fix both issues gracefully.

---

### 1. `BranchAccessService.java` (Legacy Adapters)
**Location:** `src/main/java/com/erp/montfortuganda/auth/service/BranchAccessService.java`

Simply paste these **two methods** into your `BranchAccessService` class. These act as backward-compatibility adapters so you don't have to rewrite your old Dashboard and Admission services right now!

```java
    /**
     * Legacy adapter for BranchDashboardServiceImpl
     */
    @Deprecated
    public Integer getValidatedBranchId(CurrentUserContext ctx) {
        return getAccessibleBranchId(null);
    }

    /**
     * Legacy adapter for BranchAdmissionServiceImpl
     */
    @Deprecated
    public Integer validateBranchAccess(CurrentUserContext ctx) {
        return getAccessibleBranchId(null);
    }
```

---

### 2. `DepartmentSpecification.java` (Ambiguity Fix)
**Location:** `src/main/java/com/erp/montfortuganda/school/repository/DepartmentSpecification.java`

Replace your existing `getSearchSpecification` method with this one. Notice that I changed `Specification.where(null)` to `Specification.<Department>where(null)` to explicitly tell the compiler which method to use.

```java
package com.erp.montfortuganda.school.repository;

import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.entity.Department;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class DepartmentSpecification {

    public static Specification<Department> getSearchSpecification(
            String keyword, Integer branchId, RecordStatus status, Boolean active, Boolean isAcademic) {
        
        return (root, query, cb) -> {
            // FIX: Explicitly cast to <Department> to resolve Spring Data 3.x compiler ambiguity
            Specification<Department> spec = Specification.<Department>where(null);

            if (branchId != null) {
                spec = spec.and((r, q, c) -> c.equal(r.get("branch").get("branchId"), branchId));
            }

            if (status != null) {
                spec = spec.and((r, q, c) -> c.equal(r.get("status"), status));
            }

            if (active != null) {
                spec = spec.and((r, q, c) -> c.equal(r.get("active"), active));
            }
            
            if (isAcademic != null) {
                spec = spec.and((r, q, c) -> c.equal(r.get("isAcademic"), isAcademic));
            }

            if (StringUtils.hasText(keyword)) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                spec = spec.and((r, q, c) -> c.or(
                        c.like(c.lower(r.get("departmentName")), likePattern),
                        c.like(c.lower(r.get("departmentCode")), likePattern)
                ));
            }

            return spec.toPredicate(root, query, cb);
        };
    }
}
```
