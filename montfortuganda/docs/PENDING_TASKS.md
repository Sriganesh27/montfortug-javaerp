# PENDING TASKS
*Generated at close of day: 08-07-2026*

This document serves as the immediate backlog for the next development session. All tasks listed here are pending and must be executed in priority order.

## 🔴 CRITICAL PRIORITY (Staff Management & Admissions)
- [ ] **Staff Management Module:** The user must copy the generated Staff Management code (Backend and Frontend artifacts) into their IDE and verify that adding an employee successfully generates a system login with RBAC.
- [ ] **Admission Verification Module:** Implement the UI and backend logic for the Admission Officer to view applications, verify documents, and update statuses (e.g. APPROVED, REJECTED).
- [ ] **Interview Module:** Build the UI and backend logic to schedule interviews and record test scores and teacher recommendations.

## 🟡 HIGH PRIORITY (Academic & SaaS Pipeline)
- [ ] **Academic Mapping:** Design and implement `erp_class_subjects` to map the new global `ErpSubject` master table to specific `SchoolClass` entities.
- [ ] **SaaS Payroll Engine:** Build the payroll and attendance framework linking to the new `ErpEmployee` master records.

## 🔵 MEDIUM PRIORITY (Security & Session Management)
- [ ] **Spring Security:** With the RBAC database (Roles/Permissions) now 100% complete, implement the `UserDetailsService` using the `JOIN FETCH` graph strategy to load `User` -> `UserRole` -> `Role` -> `RolePermission` -> `Permission`.
- [ ] **Global Error Handling:** Implement a unified `GlobalExceptionHandler.java` using `@ControllerAdvice` to catch `IllegalArgumentException` thrown by our Smart Entities and return clean JSON responses.

## 🟢 NORMAL PRIORITY (Frontend)
- [ ] **Frontend Security:** Add the Date of Birth (DOB) verification input to `status.html` to act as the 2FA challenge for application tracking.
- [ ] **Frontend JS Refactoring:** Refactor `status.js`, `apply.js`, and `print_application.js` to rely securely on backend sessions instead of plaintext URL parameters.
