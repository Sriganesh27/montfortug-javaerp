# STRICT ENTERPRISE DATABASE + SOURCE CODE ARCHITECTURE AUDIT
**Date:** 2026-07-14
**Mode:** READ-ONLY (No Code Generation)

---

## 1. COMPLETE DATABASE INVENTORY & 2. ENTITY MAPPING
The following is an exhaustive mapping of every SQL file found in the `databse` folder to its corresponding JPA Entity, DTO, Repository, and Controller.

| Database Table | Purpose | JPA Entity | Repository | API Controller | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`erp_academic_terms`** | School Terms | `ErpAcademicTerm` | - | - | 🟡 Entities Only |
| **`erp_academic_years`** | Academic Years | `ErpAcademicYear` | - | - | 🟡 Entities Only |
| **`erp_applications`** | Student Admissions | `ErpApplication` | `ErpApplicationRepository` | `PublicApplicationController`| 🟢 Partial (Public API) |
| **`erp_application_documents`**| Admission Docs | `ErpApplicationDocument` | - | `PublicApplicationController`| 🟢 Partial (Public API) |
| **`erp_application_fees`** | Admission Fees | `ErpApplicationFee` | - | - | 🟡 Entities Only |
| **`erp_application_interviews`**| Admission Interviews| `ErpApplicationInterview` | - | - | 🟡 Entities Only |
| **`erp_application_status_history`**| Admission Workflow| `ErpApplicationStatusHistory`| `ErpApplicationStatusHistoryRepository`| `PublicApplicationController`| 🟢 Partial (Public API) |
| **`erp_branches`** | School Branches | `Branch` | `BranchRepository` | `SuperAdminApiController` | 🟢 Implemented |
| **`erp_branch_fund_allocations`**| Scholarships to Branches| `ErpBranchFundAllocation`| `ErpBranchFundAllocationRepository`| `SuperAdminScholarshipController`| 🟢 Implemented |
| **`erp_branch_levels`** | Branch Academics | `BranchLevel` | - | - | 🟡 Entities Only |
| **`erp_classes`** | School Classes | `SchoolClass` | `SchoolClassRepository` | - | 🟡 Repos Only |
| **`erp_departments`** | Staff Departments | `Department` | `DepartmentRepository` | `DepartmentController` | 🟢 Implemented |
| **`erp_designations`** | Staff Designations | `Designation` | `DesignationRepository` | `DesignationController` | 🟢 Implemented |
| **`erp_document_sequences`** | Auto-ID Generation | `ErpDocumentSequence` | - | - | 🟡 Internal Service |
| **`erp_employees`** | Staff Master | `ErpEmployee` | `EmployeeRepository` | `EmployeeController` | 🟢 Implemented |
| **`erp_employee_contacts`** | Staff Contact | `ErpEmployeeContact` | `EmployeeContactRepository`| `EmployeeController` | 🟢 Implemented |
| **`erp_employee_documents`** | Staff Files | `ErpEmployeeDocument`| `EmployeeDocumentRepository`| `EmployeeController` | 🟢 Implemented |
| **`erp_employee_experience`** | Staff History | `ErpEmployeeExperience`| `EmployeeExperienceRepository`| `EmployeeController` | 🟢 Implemented |
| **`erp_employee_qualifications`**| Staff Education | `ErpEmployeeQualification`|`EmployeeQualificationRepository`| `EmployeeController` | 🟢 Implemented |
| **`erp_levels`** | Academic Levels | `Level` | `LevelRepository` | `PublicApplicationController`| 🟢 Partial (Public API) |
| **`erp_login_history`** | IAM Audit | `ErpLoginHistory` | - | `AuthController` | 🟢 Implemented |
| **`erp_parents`** | Student Guardians | `ErpParent` | - | - | 🟡 Entities Only |
| **`erp_permissions`** | IAM Permissions | `ErpPermission` | - | - | 🟡 Core IAM |
| **`erp_roles`** | IAM Roles | `ErpRole` | `ErpRoleRepository` | - | 🟢 Implemented |
| **`erp_role_permissions`** | IAM Role Access | `ErpRolePermission` | - | - | 🟡 Core IAM |
| **`erp_scholarship_allocations`**| Student Scholarship| `ErpScholarshipAllocation`| `ErpScholarshipAllocationRepository`| `SuperAdminScholarshipController`| 🟢 Implemented |
| **`erp_scholarship_applications`**| Scholarship Req. | `ErpScholarshipApplication`| `ErpScholarshipApplicationRepository`| `SuperAdminScholarshipController`| 🟢 Implemented |
| **`erp_sections`** | Class Sections | `ErpSection` | - | - | 🟡 Entities Only |
| **`erp_site_settings`** | Global Config | `SiteSetting` | `SiteSettingRepository` | `SuperAdminApiController` | 🟢 Implemented |
| **`erp_students`** | Student Master | `ErpStudent` | - | - | 🟡 Entities Only |
| **`erp_student_academic_history`**| Past Schooling | `ErpStudentAcademicHistory`| - | - | 🟡 Entities Only |
| **`erp_student_accounts`** | Student Login | `ErpStudentAccount` | - | - | 🟡 Entities Only |
| **`erp_student_alumni`** | Alumni Tracker | `ErpStudentAlumni` | - | - | 🟡 Entities Only |
| **`erp_student_archives`** | Deleted Students | `ErpStudentArchive` | - | - | 🟡 Entities Only |
| **`erp_student_documents`** | Student Files | `ErpStudentDocument` | - | - | 🟡 Entities Only |
| **`erp_student_enrollment`** | Current Academics | `ErpStudentEnrollment` | - | - | 🟡 Entities Only |
| **`erp_student_fee_assignments`**| Fee Demands | `ErpStudentFeeAssignment`| - | - | 🟡 Entities Only |
| **`erp_student_fee_ledger`** | Fee Tracking | `ErpStudentFeeLedger`| - | - | 🟡 Entities Only |
| **`erp_student_fee_payments`** | Fee Receipts | `ErpStudentFeePayment`| - | - | 🟡 Entities Only |
| **`erp_student_hostel`** | Boarding Module | `ErpStudentHostel` | - | - | 🟡 Entities Only |
| **`erp_student_medical`** | Medical Module | `ErpStudentMedical` | - | - | 🟡 Entities Only |
| **`erp_student_transport`** | Transport Module | `ErpStudentTransport`| - | - | 🟡 Entities Only |
| **`erp_users`** | Core Accounts | `User` | `UserRepository` | `UserController` | 🟢 Implemented |
| **`erp_user_roles`** | IAM User-Roles | `ErpUserRole` | - | - | 🟢 Implemented |
| **`web_donations`** | Public Donations | `WebDonation` | `WebDonationRepository` | - | 🟡 Entities Only |

---

## 3. CODE USAGE & 4. API MAPPING

### **Identity & Access Management (IAM)**
- **API:** `AuthController.java` (`/api/auth/login`, `/api/auth/logout`) -> Uses `UserServiceImpl.java` -> Uses `UserRepository`, `ErpRoleRepository`
- **UI:** Rendered via Thymeleaf (`login.html`) -> JS (`auth.js`)

### **Employee & HR Module**
- **API:** `EmployeeController.java` (`/api/branchadmin/employees`) -> Uses `EmployeeServiceImpl.java` -> Uses `EmployeeRepository`, `EmployeeDocumentRepository`, `EmployeeQualificationRepository`, etc.
- **API:** `DepartmentController.java` & `DesignationController.java`
- **UI:** `employees.html`, `departments.html`, `designations.html` -> JS (`employees.js`, etc.)

### **SuperAdmin & Branch Management**
- **API:** `SuperAdminApiController.java` (`/api/superadmin/branches`, `/api/superadmin/users`, `/api/superadmin/settings`)
- **UI:** `superadmin-dashboard.html`, `superadmin-branches.html`, `superadmin-users.html`

### **Admission (Public Portal)**
- **API:** `PublicApplicationController.java` (`/api/public/applications/submit`, `/api/public/applications/{id}/status`)
- **API:** `BranchAdmissionController.java` (`/api/admission/branch/applications`) -> (Internal API, partial implementation)
- **UI:** `public/admission-form.html`

### **Scholarship Module**
- **API:** `SuperAdminScholarshipController.java` (`/api/superadmin/scholarships/funds-summary`, `/allocate-branch`, `/allocate-student`)
- **UI:** `superadmin-scholarships.html`

---

## 5. FRONTEND MAPPING

Current ERP follows a Multi-Page Application (MPA) Server-Side Rendered architecture:
1. **Thymeleaf Template (`*.html`)**: Renders the skeleton and security contexts (e.g., `sec:authorize`).
2. **Vanilla JS (`static/js/*.js`)**: Handles DOM manipulation and REST API calls.
3. **REST APIs (`@RestController`)**: Returns JSON.

| HTML Template | Core JS File | Related API | Description |
| :--- | :--- | :--- | :--- |
| `employees.html` | `employees.js` | `/api/branchadmin/employees` | Staff Management |
| `departments.html` | `departments.js` | `/api/departments` | HR Settings |
| `designations.html` | `designations.js` | `/api/designations` | HR Settings |
| `superadmin-branches.html`| `superadmin-branches.js` | `/api/superadmin/branches` | Branch creation |
| `admission-form.html` | `admission.js` | `/api/public/applications` | Public Registration |

---

## 6. WORKFLOW DEPENDENCY DIAGRAM & 7. PARENT-CHILD TABLE RELATIONSHIPS

### A. The Admission Workflow
```text
[erp_applications] (Master)
  ├── 1:M ── [erp_application_documents] (Uploads)
  ├── 1:M ── [erp_application_fees] (Registration Fee)
  ├── 1:1 ── [erp_application_interviews] (Scheduling)
  └── 1:M ── [erp_application_status_history] (Audit Trail)
```

### B. The Student Core Workflow (Future)
```text
[erp_students] (Master)
  ├── 1:1 ── [erp_student_accounts] (IAM Portal)
  ├── 1:1 ── [erp_parents] (Guardian)
  ├── 1:1 ── [erp_student_medical] (Health)
  ├── 1:1 ── [erp_student_academic_history] (Past Schools)
  ├── 1:M ── [erp_student_documents] (Birth Cert, etc.)
  ├── 1:M ── [erp_student_enrollment] (Current Academics)
  │            └── 1:M ── [erp_student_fee_assignments] (Invoices)
  │                         └── 1:M ── [erp_student_fee_payments] (Receipts)
  ├── 1:M ── [erp_student_transport] (Buses)
  └── 1:M ── [erp_student_hostel] (Boarding)
```

### C. The Employee Workflow (Fully Implemented)
```text
[erp_employees] (Master)
  ├── 1:M ── [erp_employee_contacts]
  ├── 1:M ── [erp_employee_documents]
  ├── 1:M ── [erp_employee_experience]
  └── 1:M ── [erp_employee_qualifications]
```

---

## 8. MODULE-WISE TABLE CLASSIFICATION & 9. CURRENT IMPLEMENTATION STATUS

| Module | Core Tables | Implementation Status |
| :--- | :--- | :--- |
| **IAM & Security** | `erp_users`, `erp_roles`, `erp_permissions` | 🟢 **Implemented** |
| **HR & Employee** | `erp_employees`, `erp_departments`, `erp_designations`| 🟢 **Implemented** |
| **SuperAdmin / Config**| `erp_branches`, `erp_site_settings`, `erp_document_sequences`| 🟢 **Implemented** |
| **Scholarships** | `erp_scholarship_allocations`, `erp_branch_fund_allocations`| 🟢 **Implemented** |
| **Admissions** | `erp_applications`, `erp_application_status_history`| 🟡 **Partial** (Public APIs exist, Internal dashboards missing) |
| **Student Info (SIS)** | `erp_students`, `erp_parents`, `erp_student_enrollment` | 🔴 **Missing** (Entities exist, zero API/UI) |
| **Finance / Fees** | `erp_student_fee_ledger`, `erp_student_fee_assignments` | 🔴 **Missing** (Entities exist, zero API/UI) |
| **Auxiliary Services** | `erp_student_hostel`, `erp_student_transport`, `erp_student_medical` | 🔴 **Missing** (Entities exist, zero API/UI) |

---

## 11. GAP ANALYSIS

### Architecture Violations / Incomplete Logic
1. **Missing Student Import Service**: While a Student Import Excel template has been defined, the actual `@Service` and `@RestController` to parse the CSV/Excel into the `erp_students` entities **does not exist yet**.
2. **Missing Internal Admission Processing UI**: The public can submit an application via `/api/public/applications/submit`, but the Branch Admins have no HTML UI or Dashboard to view, approve, or reject these applications.
3. **Dead Tables (Currently Unused)**: `erp_student_archives`, `erp_student_alumni`, `erp_academic_terms`. Entities exist, but there are no Repositories or Controllers referencing them.

---

## 12. ADMISSION PROCESS ANALYSIS

**Current Reality:** 
The public-facing side of Admission is built (`PublicApplicationController`). A parent can submit data, which writes to `erp_applications`. 

**The Missing Gap:** 
Once `erp_applications` receives data, there is no UI for the Principal or Registrar to look at it.

**Existing Tables Ready for Use:**
- `erp_applications`
- `erp_application_documents`
- `erp_application_interviews`
- `erp_application_status_history`

**Existing APIs Ready for Use:**
- `BranchAdmissionController.java` has a stub: `@GetMapping("/applications")` for internal use.

---

## 13. FUTURE DASHBOARD PLANNING FOUNDATION

To design the upcoming Admission Dashboards without touching code, here is the blueprint of what must be built:

### Stage 1: The Admissions Intake Dashboard
- **Responsible Role:** Branch Admin / Registrar
- **Current Tables Used:** `erp_applications`, `erp_application_documents`
- **Related APIs Required:** `GET /api/admission/branch/applications?status=PENDING`
- **Missing UI:** `branchadmin-admissions.html`
- **KPIs:** Total New Applicants, Applications by Class, Pending Reviews.

### Stage 2: Interview & Verification Processing
- **Responsible Role:** Principal / Interviewer
- **Current Tables Used:** `erp_application_interviews`, `erp_application_status_history`
- **Related APIs Required:** `POST /api/admission/branch/applications/{id}/schedule-interview`
- **Missing UI:** A modal or page to assign interview dates and log notes.
- **Required Actions:** Update status to `INTERVIEW_SCHEDULED`, send Email Notification to Parents via `EmailService.java`.

### Stage 3: Final Approval & Conversion
- **Responsible Role:** Branch Admin
- **Current Tables Used:** `erp_applications`, `erp_students`, `erp_student_enrollment`
- **Related APIs Required:** `POST /api/admission/branch/applications/{id}/approve`
- **Workflow:** When APPROVED, the backend must map the `ErpApplication` object directly into a new `ErpStudent`, `ErpParent`, and `ErpStudentEnrollment` record, and generate the `admissionNo` using `ErpDocumentSequence`.

---
**END OF REPORT.**
*This artifact serves as the single source of truth for the ERP database schema and system readiness before commencing UI/UX design for internal dashboards.*
