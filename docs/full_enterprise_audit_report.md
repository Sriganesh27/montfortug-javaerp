# Enterprise Architecture Audit & Implementation Plan (READ-ONLY)

*Prepared by: Principal Enterprise Java Architect*
*Status: DRY RUN COMPLETE. NO FILES MODIFIED.*

---

## 1. Existing Architecture Report
The system is a Spring Boot Monolith transitioning into a Multi-Tenant SaaS ERP.
*   **Architecture Pattern:** Strict Layered Architecture (Controller → Service → Repository).
*   **Modules:** `admission`, `auth`, `branchadmin`, `infrastructure`, `notification`, `scholarship`, `school`, `settings`, `superadmin`.
*   **API Design:** RESTful APIs, utilizing standard DTO strategies (`ApiResponse`, `PagedResponse`).
*   **Authentication Flow:** JWT-based, managed by `JwtAuthenticationFilter` reading from Secure HTTP-only cookies and `Authorization` headers.

## 2. Package Dependency Report
*   **Vertical Cohesion:** Strong. Features like `admission` contain their own DTOs, Mappers, and Repositories.
*   **Horizontal Coupling:** Moderate. Cross-module dependencies exist (e.g., Controllers injecting `BranchAccessService` instead of relying on a dedicated internal proxy or shared kernel).

## 3. Reusability Report
**Reusable Components Discovered:**
*   `ApiResponse<T>`, `PagedResponse<T>`
*   `BranchAccessService`
*   `CurrentUserService`
*   `AuditableEntity` (Highly reusable for tracking `createdBy`/`updatedAt`).
*   `GlobalExceptionHandler`

## 4. Duplicate Code Report
*   **Validation Duplication:** Manual existence checks (`repository.existsBy...`) are duplicated across Services, rather than relying on Database `UNIQUE` constraints and centralizing the `DataIntegrityViolationException` mapping.
*   **Context Passing:** Multiple controllers manually pass `CurrentUserContext` into service methods, duplicating parameter signatures.

## 5. Dead Code Report
*   **Auth Mismatch:** `CurrentUserService` contains a comment indicating it is "Hardcoded for Phase 1 testing". This needs to be wired to the active JWT context to prevent dead-code paths in the future.

## 6. Security Report
*   **Authentication:** Robust JWT implementation leveraging `jjwt-api 0.11.5`. Protects against XSS via HTTP-only cookies.
*   **Authorization:** `@PreAuthorize` handles endpoint boundaries.
*   **Vulnerability Scan:** Mass Assignment is mitigated via DTOs. ID Enumeration is mitigated by Branch Isolation.

## 7. Performance Report
*   **N+1 Risks:** Identified cases where `.findById()` is called solely to set a foreign key. 
*   **Memory Waste:** Pagination is implemented correctly via `Pageable`.
*   **Cache Utilization:** Low. Missing `@Cacheable` on static master tables (Branches, Departments).

## 8. Database Report
*   **Entities & Relationships:** Correctly utilize `@ManyToOne(fetch = FetchType.LAZY)`.
*   **Soft Delete:** Handled manually via `active = false` flags. Needs transition to `@SQLDelete` and `@SQLRestriction` for safety.
*   **DDL Management:** `spring.jpa.hibernate.ddl-auto=none` is set. Schema is managed correctly outside of ORM auto-generation.

## 9. Transaction Report
*   **Boundaries:** `@Transactional` correctly placed at the Service layer. Read-heavy methods utilize `@Transactional(readOnly = true)`.
*   **Risks:** Synchronous third-party calls (Emails/Notifications) inside the database transaction boundary risk holding connections open and triggering partial rollbacks.

## 10. Event Architecture Report
*   **Current State:** Primarily synchronous. No widespread usage of Spring Application Events.
*   **Target State:** Requires an Outbox Pattern (`OutboxEvent`) and `@TransactionalEventListener` to decouple side effects (Notifications, Audits) from core domain transactions.

## 11. Multi-Tenant Security Report
*   **Tenant Isolation:** Horizontal isolation is managed excellently by `BranchAccessService.getAccessibleBranchId()`.
*   **Privilege Escalation:** Vertical escalation is prevented by strict Role evaluations (`SUPER_ADMIN` vs `BRANCH_ADMIN`).

## 12. Code Quality Report
*   **Strengths:** Clear separation of concerns. Solid usage of Java 21 properties.
*   **Weaknesses:** Magic strings for Roles (`"SUPER_ADMIN"`) and Statuses instead of global Enums.

## 13. Enterprise Compliance Report
| Metric | Score (1-10) | Notes |
| :--- | :---: | :--- |
| **SOLID** | 8 | Services are focused, but some act as "God Classes" handling validation, logic, and side effects. |
| **DRY** | 7 | Repetitive context passing and duplicate validation checks. |
| **Clean Architecture** | 8 | Mappers isolate DB entities from the web layer perfectly. |
| **CQRS Readiness** | 5 | Requires DTO Segregation (`CreateRequest` vs `UpdateRequest`). |
| **Event-Driven (EDA)** | 3 | Needs Domain Events to decouple logic. |

## 14. Risk Analysis
*   **Highest Risk:** Deploying synchronous transaction boundaries. A failing email server will cause an ERP save to roll back.
*   **Medium Risk:** Forgetting to append `active = true` to manual queries, leaking soft-deleted data.

## 15. Breaking Change Analysis
*   Extracting `BranchAccessService` and `ApiResponse` to a `common` module will break imports across the entire project.
*   Segregating DTOs (`Create` vs `Update`) will require refactoring API consumers (Frontend).

## 16. Rollback Strategy
*   Every phase in the implementation plan operates on Git feature branches. If an architectural upgrade fails, rollback is a simple Git Revert, as no DDL schema destruction occurs.

## 17. Suggested Implementation Order
1.  **Phase 1:** Shared Kernel Extraction.
2.  **Phase 2:** Ambient Security Context Refactoring.
3.  **Phase 3:** ORM Hardening (Soft Deletes & Locking).
4.  **Phase 4:** CQRS-lite DTO Segregation.
5.  **Phase 5:** Event-Driven Architecture (Outbox Pattern).

---

# IMPLEMENTATION ROADMAP (WAITING FOR APPROVAL)

### Phase 1: Shared Kernel Extraction
*   **Reason:** Prevents code duplication by centralizing `BranchAccessService`, Exceptions, and standard DTOs.
*   **Priority:** High
*   **Risk:** Low
*   **Breaking / Non-breaking:** Non-breaking (Requires IDE import updates).
*   **Files affected:** `BranchAccessService.java`, `ApiResponse.java`, Controllers.
*   **Dependencies:** None.
*   **Estimated effort:** 4 Hours.
*   **Reusability score:** 10/10 (Provides the foundation for all future ERP modules).
*   **Backward compatibility impact:** 100% compatible.

### Phase 2: Ambient Security Context Refactoring
*   **Reason:** Removes `CurrentUserContext` from controller/service method signatures to clean up APIs.
*   **Priority:** Medium
*   **Risk:** Low
*   **Breaking / Non-breaking:** Breaking (Method signatures will change).
*   **Files affected:** `CurrentUserService.java`, all `*ServiceImpl.java` files.
*   **Dependencies:** Requires `CurrentUserService` to pull dynamically from `SecurityContextHolder`.
*   **Estimated effort:** 1 Day.
*   **Reusability score:** 9/10.
*   **Backward compatibility impact:** Frontend is unaffected; backend methods require updating.

### Phase 3: ORM Hardening (Soft Deletes & Locking)
*   **Reason:** Eliminates the risk of developers forgetting to filter soft-deleted rows.
*   **Priority:** High
*   **Risk:** Medium (Requires testing to ensure historical data behaves correctly).
*   **Breaking / Non-breaking:** Non-breaking API.
*   **Files affected:** `AuditableEntity.java`, `Department.java`, `Branch.java`.
*   **Dependencies:** Hibernate 6.x features (`@SQLRestriction`).
*   **Estimated effort:** 1 Day.
*   **Reusability score:** 10/10 (Global application to all entities).
*   **Backward compatibility impact:** 100% compatible.

### Phase 4: CQRS-lite DTO Segregation & Caching
*   **Reason:** Reduces payload sizes, tightens validation (`Create` vs `Update`), and speeds up read-heavy APIs.
*   **Priority:** Medium
*   **Risk:** Medium
*   **Breaking / Non-breaking:** Breaking (Frontend payloads may need adjustment).
*   **Files affected:** `DepartmentDTO.java` (splits to 3 files), Controllers, Mappers.
*   **Dependencies:** Spring Cache abstraction.
*   **Estimated effort:** 2 Days.
*   **Reusability score:** 8/10.
*   **Backward compatibility impact:** Will require frontend form updates.

### Phase 5: Event-Driven Architecture & Outbox
*   **Reason:** Solves distributed transaction failures. Ensures side-effects (Emails, Audits) never block or roll back core domain saves.
*   **Priority:** High
*   **Risk:** High
*   **Breaking / Non-breaking:** Non-breaking.
*   **Files affected:** Core `ServiceImpl` classes, new `OutboxEvent` entity, new `*EventListener` classes.
*   **Dependencies:** Spring Application Events.
*   **Estimated effort:** 3-5 Days.
*   **Reusability score:** 10/10 (Reusable Outbox for the entire ERP).
*   **Backward compatibility impact:** 100% compatible.

---

**AUDIT COMPLETE.**
**STATUS: HALTED.**
**Waiting for explicit command: "GENERATE CODE"**
