# PENDING TASKS
*Generated at close of day: 06-07-2026*

This document serves as the immediate backlog for the next development session. All tasks listed here are pending and must be executed in priority order.

## 🔴 CRITICAL PRIORITY (Admission Module & Student Phase 2)
- [ ] **Admission Repositories:** Implement Spring Data JPA Repositories for the core Admission and Student Domain Entities (e.g., `ErpApplicationRepository`, `ErpStudentRepository`, `ErpApplicationInterviewRepository`).
- [ ] **Admission DTOs & Projections:** Build highly optimized DTO projections and implement `@EntityGraph` annotations on the repositories to prevent N+1 query issues during heavy admission cycles.
- [ ] **Admission Service Layer:** Finalize the business logic for transitioning an Application to an Enrolled Student.

## 🟡 HIGH PRIORITY (Academic & SaaS Pipeline)
- [ ] **Academic Mapping:** Design and implement `erp_class_subjects` to map the new global `ErpSubject` master table to specific `SchoolClass` entities.
- [ ] **SaaS HR/Payroll Foundation:** Build the `erp_staff` master profile table to kick off the Advanced SaaS Payroll Engine.

## 🔵 MEDIUM PRIORITY (Security & Session Management)
- [ ] **Spring Security:** With the RBAC database (Roles/Permissions) now 100% complete, implement the `UserDetailsService` using the `JOIN FETCH` graph strategy to load `User` -> `UserRole` -> `Role` -> `RolePermission` -> `Permission`.
- [ ] **Global Error Handling:** Implement a unified `GlobalExceptionHandler.java` using `@ControllerAdvice` to catch `IllegalArgumentException` thrown by our Smart Entities and return clean JSON responses.

## 🟢 NORMAL PRIORITY (Frontend)
- [ ] **Frontend Security:** Add the Date of Birth (DOB) verification input to `status.html` to act as the 2FA challenge for application tracking.
- [ ] **Frontend JS Refactoring:** Refactor `status.js`, `apply.js`, and `print_application.js` to rely securely on backend sessions instead of plaintext URL parameters.
