# Enterprise Backend Audit Report

## 1. Executive Summary

- Project Name: Montfort Uganda Multi-School ERP
- Scan Date: 2026-07-14
- Total Java Files: ~205
- Total SQL Files: 52
- Total Database Tables: 52
- Overall Backend Completion (%): ~60% (Foundation is solid, Workflow/Task engines pending)

---

## 2. Existing Database Tables

| Table | Purpose | Status | Used By | Action |
| :--- | :--- | :--- | :--- | :--- |
| `erp_applications` | Core admission application data | Active | Admission | KEEP |
| `erp_application_documents` | Verification docs for applicants | Active | Admission | KEEP |
| `erp_application_status_history`| Tracks application stage changes | Active | Admission | MODIFY (Migrate to unified history) |
| `erp_students` | Core student registry | Active | Student | KEEP |
| `erp_employees` | Staff/Teacher registry | Active | Employee | KEEP |
| `erp_users` / `erp_roles` / `erp_permissions` | RBAC Security Foundation | Active | Security | KEEP |
| `erp_scholarship_applications` | Manages financial aid requests | Active | Scholarship | KEEP |
| `erp_departments` / `erp_designations` | Organizational structure | Active | School / HR | KEEP |
| `erp_tasks` | Unified Task Engine | Missing | N/A | CREATE |

---

## 3. Existing Java Entities

| Entity | Table | Package | Status | Action |
| :--- | :--- | :--- | :--- | :--- |
| `ErpApplication` | `erp_applications` | `admission.entity` | Active | KEEP |
| `ErpApplicationStatusHistory` | `erp_application_status_history`| `admission.entity` | Active | MODIFY |
| `ErpStudent` | `erp_students` | `student.entity` | Active | KEEP |
| `ErpEmployee` | `erp_employees` | `employee.entity` | Active | KEEP |
| `ErpUser` / `Role` | `erp_users` / `erp_roles` | `auth.entity` | Active | KEEP |
| `AuditableEntity` | (Base Class MappedSuperclass) | `model.entity` | Active | KEEP |

---

## 4. Existing Repositories

| Repository | Entity | Status | Action |
| :--- | :--- | :--- | :--- |
| `ErpApplicationRepository` | `ErpApplication` | Active | KEEP |
| `ErpStudentRepository` | `ErpStudent` | Active | KEEP |
| `ErpEmployeeRepository` | `ErpEmployee` | Active | KEEP |
| `UserRepository` | `ErpUser` | Active | KEEP |

---

## 5. Existing Services

| Service | Purpose | Status | Action |
| :--- | :--- | :--- | :--- |
| `BranchAdmissionServiceImpl` | Handles current admission logic | Active | MODIFY (Extract workflow logic) |
| `EmployeeService` | Manages employee lifecycle | Active | KEEP |
| `CustomUserDetailsService` | Spring Security auth | Active | KEEP |
| `AdmissionApplicationService` | Dedicated Business Layer | Missing | CREATE |
| `WorkflowActionService` | Central Command Orchestrator | Missing | CREATE |

---

## 6. Existing Controllers

| Controller | Endpoints | Status | Action |
| :--- | :--- | :--- | :--- |
| `BranchAdmissionController` | `/api/branch/admissions/**` | Active | MODIFY (Shift to Action APIs) |
| `EmployeeApiController` | `/api/employees/**` | Active | KEEP |
| `AuthController` | `/api/auth/**` | Active | KEEP |
| `AdmissionActionController` | (Proposed unified endpoint) | Missing | CREATE |

---

## 7. Existing DTOs

| DTO | Purpose | Status |
| :--- | :--- | :--- |
| `ApplicationFormDTO` | Form submission payload | Active |
| `EmployeeDTO` | Data transfer for staff | Active |
| `LoginRequestDTO` | Auth payload | Active |

---

## 8. Existing Enums

| Enum | Purpose | Status |
| :--- | :--- | :--- |
| `ApplicationStatus` | `DRAFT, SUBMITTED, UNDER_REVIEW...` | Inside Entity - Extract |
| `Gender` | `MALE, FEMALE` | Active |
| `AdmissionAction` | (Workflow Triggers) | Missing - Create |

---

## 9. Existing Security Components

List:
- Spring Security: `SecurityConfig.java` configured for JWT.
- JWT: `JwtUtils`, `AuthTokenFilter`.
- Authentication: `CustomUserDetailsService`, `AuthController`.
- Authorization: Standard `@PreAuthorize` used sporadically.
- Permission Validation: Driven by `erp_permissions` mapped to `erp_role_permissions`.

---

## 10. Existing Workflow Components

List:
- Workflow Services: Missing (Hardcoded in AdmissionServiceImpl currently).
- Task Engine: Missing (`erp_tasks` needed).
- Status History: Exists (`erp_application_status_history`).
- Audit: Base `AuditableEntity` tracks standard CRUD via `@CreatedBy`.
- Notifications/Email: Direct coupling in services; lacks pub/sub Queue.

---

## 11. Dependency Map

**Admission Module:**
`BranchAdmissionController` -> `BranchAdmissionServiceImpl` -> `ErpApplicationRepository` -> `ErpApplication`
*(Note: Workflow logic is currently tightly coupled inside the Service)*

---

## 12. Missing Components

### Database
- `erp_tasks` (Unified task engine)
- `erp_workflow_logs` (If expanding beyond application history)

### Entities
- `ErpTask`
- `AdmissionState`, `AdmissionAction` (Enums)

### Services
- `WorkflowActionService`
- `AdmissionWorkflowStrategy`
- `TaskService`
- `RuleEngine / Validators`

### Controllers
- `AdmissionActionController`
- `CaseWorkspaceController`

---

## 13. Duplicate Components

- Currently, status transitions and history tracking are tightly bound inside `BranchAdmissionServiceImpl`.
- Email sending is directly invoked rather than event-driven, causing coupling duplication across modules.

---

## 14. Components That Must NOT Be Modified

- `AuditableEntity`: Forms the bedrock of the JPA audit trail. Modifying this breaks all ~50 tables.
- `SecurityConfig` & `JwtUtils`: Authentication is stable and functional. Do not risk breaking login.

---

## 15. Components Recommended For Refactoring

- `BranchAdmissionServiceImpl`: Should be stripped of state-machine logic. It should only handle pure data persistence, while `WorkflowActionService` handles transitions.
- Controller Endpoints: Move away from specific `POST /verify` to a unified `POST /actions` pattern to support the generic Case Workspace Drawer.

---

## 16. Recommended Backend Package Structure

```
com.erp.montfortuganda
├── core
│   ├── audit
│   ├── task
│   ├── workflow
│   ├── event
│   └── security
└── admission
    ├── controller
    ├── service
    │   ├── AdmissionApplicationService
    │   └── AdmissionWorkflowStrategy
    ├── rules
    │   └── DocumentVerificationRule
    ├── repository
    └── entity
```

---

## 17. Final Implementation Checklist

| Component | Existing | Modify | Create |
| :--- | :--- | :--- | :--- |
| `ErpTask` Engine | | | 🆕 Create |
| `WorkflowActionService` | | | 🆕 Create |
| `AdmissionApplicationService` | | | 🆕 Create |
| `BranchAdmissionController` | ✅ Existing | 🔄 Modify | |
| Rule Engine (Validators) | | | 🆕 Create |
| Spring Events (Pub/Sub) | | | 🆕 Create |

---

## 18. Recommended Development Order

1. **Phase 1: Enterprise Task Engine (`erp_tasks`, `TaskService`)**
2. **Phase 2: Core Workflow Framework (`WorkflowActionService`, `Strategy` Interface)**
3. **Phase 3: Case Workspace UI (Read-Only Drawer & Data APIs)**
4. **Phase 4: AdmissionApplicationService (Business orchestration layer)**
5. **Phase 5: Business Rule Validators (Rule Engine)**
6. **Phase 6: Spring Events (Pub/Sub for Notifications)**
7. **Phase 7: Business Actions (Implementing specific triggers via `/actions` endpoint)**

---

## 19. Final Enterprise Verdict

- **Architecture:** 8/10 (Solid MVC, but needs the Workflow/Case abstraction).
- **Maintainability:** 7/10 (State transitions currently hardcoded; moving to Strategies will fix this).
- **Scalability:** 9/10 (Database structure handles multi-school well).
- **Security:** 9/10 (JWT & RBAC well-defined).
- **Performance:** 8/10 (Can be improved with Lazy-Loaded Case Tabs).
- **Admission Module Readiness:** Pending Workflow extraction.
- **Overall ERP Readiness:** High potential. Once the generic Task & Workflow engines are implemented, the remaining 10+ modules can be developed exponentially faster using the exact same Case Workspace pattern.
