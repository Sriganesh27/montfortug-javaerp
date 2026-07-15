# FULL ENTERPRISE ARCHITECTURE AUDIT

## 1. Cross-Module Scan Results
A recursive scan of `employee, auth, security, user, role, branch, department, designation, common, master, shared, util, mapper, validation, specification, repository, service, controller, dto, entity, enums` yields the following structural dependencies relevant to our current milestone:

**Entities:** `ErpEmployee`, `ErpUser` (auth/user), `ErpRole` (auth/role), `Branch` (branch), `Department` (department), `Designation` (designation)
**DTOs:** `EmployeeContactDTO`, `EmployeeDocumentDTO`, `EmployeeExperienceDTO`, `EmployeeQualificationDTO`, `EmployeeCreateRequest`, `EmployeeUpdateRequest`
**Repositories:** `EmployeeRepository`, `UserRepository`, `RoleRepository`
**Specifications:** `EmployeeSpecificationBuilder`
**Services:** `EmployeeService`, `EmployeeServiceImpl`
**Mappers:** `DepartmentMapper`, `DesignationMapper` (school module)

## 2. Duplicate Analysis & Merge Conflicts
As identified in the previous scan, parallel architectures exist in the DTO layer for child entities.
- **Conflict:** `EmployeeContactRequest` vs `EmployeeContactDTO`
- **Resolution:** Purge `*Request` child classes. All custom validations (`@Pattern`, `@Size`, lifecycle Enums) will be merged into the existing `*DTO` files. No new DTO classes will be created.

---

## 3. STRICT MODIFICATION PLAN (Next Steps)
In compliance with Rule 1 and Rule 7, no new files will be created unless absolutely necessary. We will modify the existing foundation.

### Task A: Consolidate Child DTOs
1. **Existing file path:** `src/main/java/com/erp/montfortuganda/employee/dto/EmployeeContactDTO.java`
2. **Reason for modification:** Consolidate validation rules and lifecycle fields to eliminate the need for a separate Request class.
3. **Action:** MODIFY
4. **Justification:** N/A (Modification)

*(This pattern applies identically to `EmployeeDocumentDTO`, `EmployeeExperienceDTO`, and `EmployeeQualificationDTO`).*

### Task B: Update Core Payload
1. **Existing file path:** `src/main/java/com/erp/montfortuganda/employee/dto/EmployeeCreateRequest.java`
2. **Reason for modification:** Update child collections to reference the consolidated `*DTO` classes instead of the deleted `*Request` classes.
3. **Action:** MODIFY
4. **Justification:** N/A (Modification)

### Task C: Implement Mapper
1. **Existing file path:** `src/main/java/com/erp/montfortuganda/employee/mapper/EmployeeMapper.java`
2. **Reason for modification:** To convert consolidated DTOs to Entities.
3. **Action:** CREATE
4. **Justification:** Absolute requirement. The full project scan confirms that while `DepartmentMapper` exists in `school`, there is no `EmployeeMapper` in the `employee` module or `common` module to handle this specific domain logic.

### Task D: Enhance Repository Layer
1. **Existing file path:** `src/main/java/com/erp/montfortuganda/employee/repository/EmployeeRepository.java`
2. **Reason for modification:** Add custom Spring Data JPA queries (e.g., duplicate checks, branch-isolated lookups) required by the Service layer.
3. **Action:** MODIFY
4. **Justification:** N/A (Modification)

### Task E: Enhance Specifications
1. **Existing file path:** `src/main/java/com/erp/montfortuganda/employee/specification/EmployeeSpecificationBuilder.java`
2. **Reason for modification:** Implement dynamic search filters (Age ranges, Join Dates, Department, Designation) for the paginated grid.
3. **Action:** MODIFY
4. **Justification:** N/A (Modification)

---
**Status:** Awaiting authorization to begin executing Task A & B (DTO Consolidation).
