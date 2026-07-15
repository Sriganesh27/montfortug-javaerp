# EmployeeServiceImpl Implementation Plan

Per your 5 strict refinements, the service layer modifications have been re-architected. 

- **Refinement 1 (Transactions):** `@Transactional` will encapsulate the entire `createEmployee` and `updateEmployee` workflows to ensure atomic rollback.
- **Refinement 2 (ID-Based Updates):** We will abandon "delete-and-replace". The update logic will iterate over the passed list, lookup by ID if provided (updating existing), insert if no ID, and ignore missing ones (or soft-delete them, but for this first iteration we will focus on inserts/updates).
- **Refinement 3 (SRP Mappers):** Separate `mapContact()`, `mapQualification()`, etc., will be created to separate mapping concerns from persistence loops.
- **Refinement 4 (Audit Fields):** The `AuditableEntity` fields will be left alone to allow JPA/Hibernate to auto-populate them.
- **Refinement 5 (FK Validation):** Department, Designation, and Reporting Manager lookups will happen *before* the parent employee object is populated and saved.

Here is the exact action plan for `EmployeeServiceImpl.java`:

| Method | Action | Risk | Notes |
|--------|--------|------|-------|
| `createEmployee()` | Modify | Medium | Reorder to validate FKs first. Save parent + children in one `@Transactional` transaction. |
| `updateEmployee()` | Modify | High | Handle child collection updates carefully using ID-based logic (Update if ID exists, insert if null). |
| `saveContacts()` | New private helper | Low | Iterates over payload, maps and persists contacts. |
| `mapContact()` | New private helper | Low | Extracts Option B manual mapping into an SRP function. |
| `saveQualifications()`| New private helper | Low | Iterates over payload, maps and persists qualifications. |
| `mapQualification()` | New private helper | Low | Extracts Option B manual mapping into an SRP function. |
| `saveExperiences()` | New private helper | Low | Iterates over payload, maps and persists experiences. |
| `mapExperience()` | New private helper | Low | Extracts Option B manual mapping into an SRP function. |
| `saveDocuments()` | New private helper | Low | Iterates over payload, maps and persists documents. |
| `mapDocument()` | New private helper | Low | Extracts Option B manual mapping into an SRP function. |

Awaiting your final approval on this matrix so I can modify `EmployeeServiceImpl.java`.
