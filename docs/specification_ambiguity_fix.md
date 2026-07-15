# Spring Data 3 Ambiguity Fix

Ah, my apologies! Because `null` itself is typeless, the compiler still doesn't know if `null` is a `Specification` or a `PredicateSpecification`. We have to explicitly cast the `null` value itself!

Here is the fully resolved `DepartmentSpecification.java`.

---

### `DepartmentSpecification.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/repository/DepartmentSpecification.java`

Notice the fix on line 15: `Specification.where((Specification<Department>) null);`

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
            
            // FIX: Explicitly cast null to Specification<Department> to resolve ambiguity
            Specification<Department> spec = Specification.where((Specification<Department>) null);

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
