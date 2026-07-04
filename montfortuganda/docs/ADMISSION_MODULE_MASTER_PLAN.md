# ENTERPRISE ADMISSION MODULE: MASTER IMPLEMENTATION PLAN

**IMPORTANT**
*MODE: READ-ONLY / ARCHITECTURAL ANALYSIS*
*STATUS: DRAFT - PENDING APPROVAL*
*AUTHOR: Lead Enterprise Software Architect*

## 1. Executive Summary
This document outlines the end-to-end Enterprise Architecture and Implementation Roadmap for the Montfort Uganda Admission Module. It covers the complete lifecycle of a student from initial application submission to final enrollment, incorporating multi-tiered verification, teacher interviews, financial/scholarship processing, and final fee payment. The design strictly adheres to a layered architecture (Controller → Service → Repository → DB), enforces strict RBAC (Role-Based Access Control), and guarantees a 100% audit trail.

## 2. Architecture Review
**Current Standard:**
- **Presentation:** Vanilla HTML/CSS/JS (Scoped, no inline, no Bootstrap/React).
- **Security:** JWT + HttpSession Security, OWASP Top 10 compliance.
- **Backend Layering:** Controller (HTTP routing) → Service (Business Logic/Transactions) → Repository (JPA Data Access) → Database (MySQL).
- **Rule:** Controllers must never contain business logic or call Repositories directly. DTOs are the only data carrier across the network boundary.

## 3. Current Application Flow
**Completed (Step 1):** The parent successfully submits the ErpApplication via the public portal. The application includes dynamic branch loading, secure file upload (magic bytes validation), email notifications, and JWT-secured tracking.

## 4. Admission Module Design
The remaining module (Steps 2-14) introduces three distinct back-office actor roles:
- **Admission Officer:** First-line reviewer, document verifier, and fee coordinator.
- **Assigned Teacher/HOD:** Academic evaluator (Test Marks, Interview Remarks). Cannot approve/reject.
- **School Admin / Super Admin:** Financial verifiers and final decision makers for Scholarships.

## 5. Database Changes
To support the workflow without polluting the main `erp_applications` table (avoiding a "God Table"), we require the following normalized tables:
- `erp_application_interviews`: Stores teacher assignments, test marks, and recommendations.
- `erp_application_scholarships`: Stores scholarship requests, verified income, and final percentages.
- `erp_application_scholarship_docs`: Links income/medical proof to the scholarship.
- `erp_application_fees`: Tracks the fee quotation, scholarship deduction, and final payment status prior to enrollment.

## 6. Entity Design
New JPA Entities to be created in `com.erp.montfortuganda.admission.entity`:
- `ErpApplicationInterview`
- `ErpApplicationScholarship`
- `ErpApplicationScholarshipDoc`
- `ErpApplicationFee`

*Note: All entities will extend audit capabilities (createdBy, createdAt, updatedAt, status) and utilize strict `@NotNull` and `@Size` validation.*

## 7. DTO Design
Data Transfer Objects required for the UI-to-Controller boundary:
- `ApplicationReviewDTO`: For the Officer to verify or return applications.
- `TeacherAssignmentDTO`: For assigning an application to a specific teacher ID.
- `TeacherEvaluationDTO`: For the Teacher to submit marks and recommendations.
- `ScholarshipRequestDTO`: To initiate Option 1 (Officer) or Option 2 (Parent).
- `ScholarshipDecisionDTO`: For Admin/Super Admin to approve percentages.

## 8. Repository Design
Standard Spring Data JPA Interfaces:
- `ErpApplicationInterviewRepository`
- `ErpApplicationScholarshipRepository`
- `ErpApplicationFeeRepository`

## 9. Service Design
Business logic must be encapsulated in domain-driven services:
- `AdmissionReviewService`: Handles Steps 2, 5, 7.
- `TeacherEvaluationService`: Handles Steps 3, 4.
- `ScholarshipService`: Handles Steps 8, 9, 10, 11, 12.
- `EnrollmentOrchestrationService`: Handles Step 14 (Migrating Application → Student, creating parent accounts, sending final emails).

## 10. Controller Design
REST API endpoints scoped by Role:
- `/api/v1/officer/applications/*` (Requires ROLE_OFFICER)
- `/api/v1/teacher/applications/*` (Requires ROLE_TEACHER)
- `/api/v1/admin/scholarships/*` (Requires ROLE_ADMIN / ROLE_SUPER_ADMIN)

## 11. Security & Advanced Authentication Design
- **Multi-Factor Authentication (MFA):** Require SMS or Email OTP (One-Time Password) for Officers and Admins before accessing the Admission Dashboard.
- **IP Allowlisting & Geofencing:** Restrict back-office access (Officer/Admin logins) to specific school IP ranges or block non-Ugandan IP addresses.
- **Time-Based Access Control:** Restrict Teachers from accessing the application dashboard outside of school working hours (e.g., restricted between 8 PM - 6 AM).
- **Session Concurrency Management:** Ensure a user can only have one active session. If they log in on a new device, the old session is automatically invalidated.
- **Brute Force Protection:** Automatic account lockout after 5 consecutive failed login attempts, requiring Super Admin unlock.
- **RBAC:** Endpoints secured with `@PreAuthorize("hasRole('...')")`.
- **IDOR Prevention:** Teachers can only fetch applications where `teacher_id = {jwt.userId}`. Officers can only fetch applications for their assigned `branch_id`.
- **Validation:** Strict `@Valid` on all DTOs.
- **File Upload:** Scholarship documents must pass Magic Byte validation, canonical path checks, and be stored in a non-executable directory.

## 12. Email Workflow
Automated triggers executed via EmailService async threads:
- `CORRECTION_REQUESTED`: Sent by Officer.
- `INTERVIEW_SCHEDULED`: Sent when Teacher is assigned.
- `SCHOLARSHIP_LINK`: Sent to Parent (Option 2).
- `SCHOLARSHIP_DECISION`: Sent by Super Admin.
- `ADMISSION_CONFIRMED`: Sent after Fee Payment.
- `ENROLLMENT_COMPLETED`: Contains Student ID and Parent Portal Login.

## 13. Status Workflow
The `ApplicationStatus` enum must be expanded to: `DRAFT`, `SUBMITTED`, `UNDER_REVIEW`, `RETURNED_FOR_CORRECTION`, `TEACHER_EVALUATION`, `PENDING_FINAL_REVIEW`, `SELECTED`, `WAITLISTED`, `REJECTED`, `SCHOLARSHIP_PENDING`, `FEE_PENDING`, `ENROLLED`.

## 14. Scholarship Workflow (Recommendation for Step 8)
> **TIP: Enterprise Recommendation for Option 1 vs Option 2**
> Implement Option 2 (System sends secure link to Parent). This prevents the Admission Officer from manually typing sensitive financial data (liability risk) and ensures the Parent electronically signs/submits their own income proof. The Officer simply clicks "Request Scholarship Verification," which generates a JWT-secured one-time link valid for 72 hours.

## 15. Enrollment Workflow (Step 14)
The `EnrollmentOrchestrationService` acts as a distributed transaction:
1. Generate `admission_no` based on Branch/Year sequence.
2. Insert into `erp_students`.
3. Insert into `erp_student_enrollment`.
4. Generate `erp_student_accounts` (Parent & Student login).
5. Mark `erp_applications.application_status = ENROLLED`.
6. Mark `erp_applications.student_created = true`.

## 16. Role & Permission Matrix
| Role | Action | Scope |
| :--- | :--- | :--- |
| **OFFICER** | Verify Docs, Assign Teacher, Final Review, Trigger Scholarship | Branch Level |
| **TEACHER** | Input Marks, Add Remarks, Recommend | Assigned Apps Only |
| **ADMIN** | Verify Scholarship Income/Docs | Branch Level |
| **SUPER ADMIN** | Approve/Reject Scholarship %, Override | Global |

## 17. Audit Logging Strategy
Every state change will generate an entry in `erp_application_status_history`.
- Includes: `from_status`, `to_status`, `changed_by_user_id`, `remarks`, `timestamp`.
- **Rule:** No Hard Deletes. If an application is discarded, it is marked as `REJECTED` or `WITHDRAWN`.

## 18. Notification Strategy
All notifications (Email/SMS) are decoupled from the main HTTP request using `@Async` or Spring Application Events (`ApplicationEventPublisher`) to prevent UI blocking.

## 19. API Design
Example Endpoints:
- `PUT /api/v1/officer/applications/{id}/assign-teacher`
- `PUT /api/v1/teacher/applications/{id}/evaluate`
- `POST /api/v1/officer/applications/{id}/request-scholarship`
- `POST /api/v1/public/scholarship/{jwt_token}/submit`

## 20. Frontend Pages Required
- `officer-dashboard.html` (DataTables, Filters for Status/Branch)
- `teacher-evaluation.html` (Form for Marks/Remarks)
- `admin-scholarship-review.html` (Financial document viewer, Approval buttons)
- `public-scholarship-form.html` (Secure page for Parents to upload income proof)

## 21. Risk Analysis
- **Risk:** Parent scholarship link intercepted.
  - **Mitigation:** The link requires entering the Application Reference Number and Primary Mobile Number as a 2FA challenge before rendering the form.
- **Risk:** Teacher alters marks after submission.
  - **Mitigation:** Once the teacher submits, the `erp_application_interviews` record is locked (status = SUBMITTED). Only an Admin can unlock it.

## 22. Performance Analysis
- The dashboard will query applications using indexed columns (`branch_id`, `application_status`).
- Pagination will be enforced at the Repository level (`Pageable`). No `findAll()` permitted.

## 23. Testing Strategy
- **Unit Tests:** Service layer transition logic.
- **Integration Tests:** Repository queries and JPA mapping.
- **Security Tests:** Verify Teacher cannot access another Teacher's assigned application.

## 24. Documentation Changes
- `API_Documentation.md`: Update with the new endpoints.
- `Database_Schema.md`: Add the 4 new tables.
- `Admission_Workflow.md`: Map this exact 14-step process for developer reference.

## 25. Step-by-Step Implementation Roadmap
*Awaiting your approval to begin executing this roadmap.*
- **Phase 1: Database & Entities:** Create the 4 new SQL tables and corresponding JPA Entities. Update the ApplicationStatus enum.
- **Phase 2: Repository & DTOs:** Create the Repositories and strictly validated DTOs.
- **Phase 3: Core Services:** Implement AdmissionReviewService and TeacherEvaluationService.
- **Phase 4: Controllers:** Expose the REST APIs.
- **Phase 5: Scholarship Sub-System:** Implement the JWT secure link generation and Public Scholarship Form.
- **Phase 6: Enrollment Orchestration:** Build the complex data migration from Application to Student.
- **Phase 7: Frontend UI:** Build the Vanilla JS/HTML Dashboards.
