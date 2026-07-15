# Backend Warning Fixes

Since you requested not to take write access, here are the code adjustments to resolve the IDE warnings. These warnings mostly relate to scaffolding we created for future modules (Excel Import, Child Entities) that aren't fully hooked up yet, as well as redundant Hibernate defaults.

### 1. Remove Unused Declarations (Safe Deletes)

You can safely delete the following files and methods, as they are not currently used in the active Employee CRUD flow. (You can recreate them when we build the Bulk Import and Child Entity modules later).

*   **Delete File:** `src/main/java/com/erp/montfortuganda/employee/dto/EmployeeQualificationDTO.java`
*   **Delete File:** `src/main/java/com/erp/montfortuganda/employee/repository/EmployeeDocumentRepository.java`
*   **Delete File:** `src/main/java/com/erp/montfortuganda/employee/service/EmployeeImportService.java` (and its implementation `EmployeeImportServiceImpl` if it exists).

**Modify File:** `src/main/java/com/erp/montfortuganda/employee/repository/EmployeeRepository.java`
*   Remove line 28:
```java
// Delete this line:
boolean existsByEmployeeNoAndBranch_BranchId(String employeeNo, Integer branchId);
```

### 2. Fix "Redundant default parameter value assignment"

This warning occurs because Hibernate's `@Column` annotation has a default `length` of `255`, and a default `nullable` of `true`. Explicitly writing `length = 255` or `nullable = true` triggers this warning. 

**The absolute fastest way to fix this in IntelliJ:**
1. Click on one of the highlighted `@Column(length = 255)` warnings.
2. Press `Alt + Enter` (or `Option + Enter` on Mac).
3. Select **"Remove redundant default parameter value"**.
4. Press the right arrow key on that popup and select **"Fix all 'Redundant default parameter value assignment' problems in project"**.

IntelliJ will instantly clean up all 14 occurrences across your entity files in one second!

*(If you prefer to do it manually, you will need to open `ErpEmployee.java`, `ErpEmployeeContact.java`, `ErpApplicationDocument.java`, etc., and delete `, length = 255` from every `@Column` annotation).*
