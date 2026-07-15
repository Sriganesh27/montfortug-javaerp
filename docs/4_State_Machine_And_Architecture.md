# 4. Admission Workflow State Machine & Architecture Blueprint
## Project: Montfort Uganda Multi-School ERP
## Module: Admission Management System

---

## 1. The Admission State Machine
The core of the admission engine is driven by 25 immutable states. An application can only move linearly (or fallback strictly based on business rules).

1. `DRAFT`
2. `SUBMITTED`
3. `UNDER_VERIFICATION`
4. `DOCUMENTS_PENDING`
5. `VERIFIED`
6. `ASSIGNED_TO_TEACHER`
7. `TEST_SCHEDULED`
8. `TEST_COMPLETED`
9. `RECOMMENDED`
10. `NOT_RECOMMENDED`
11. `SHORTLISTED`
12. `PARENT_MEETING_PENDING`
13. `PARENT_ACCEPTED`
14. `PARENT_DECLINED`
15. `SCHOLARSHIP_REQUESTED`
16. `SCHOLARSHIP_BRANCH_REVIEW`
17. `SCHOLARSHIP_SUPERADMIN_REVIEW`
18. `SCHOLARSHIP_APPROVED`
19. `SCHOLARSHIP_REJECTED`
20. `FEE_PENDING`
21. `PARTIAL_PAYMENT`
22. `FULL_PAYMENT`
23. `READY_FOR_ENROLLMENT`
24. `STUDENT_CREATED`
25. `COMPLETED`

---

## 2. Dashboard to State Mapping
Every dashboard acts as a strict filter over the state machine.

| Dashboard | Visible States (Filters) | Target User |
| :--- | :--- | :--- |
| **Admission Officer** | `SUBMITTED`, `UNDER_VERIFICATION`, `VERIFIED`, `DOCUMENTS_PENDING` | Admission Officer |
| **Teacher** | `ASSIGNED_TO_TEACHER`, `TEST_SCHEDULED` | Assigned Teacher |
| **Parent Meeting** | `SHORTLISTED`, `PARENT_MEETING_PENDING` | Admission Officer / Principal |
| **Scholarship Officer** | `SCHOLARSHIP_REQUESTED`, `SCHOLARSHIP_BRANCH_REVIEW`, `SCHOLARSHIP_SUPERADMIN_REVIEW` | Scholarship Officer / Super Admin |
| **Principal Approval** | `RECOMMENDED`, `NOT_RECOMMENDED`, `PARENT_ACCEPTED`, `SCHOLARSHIP_APPROVED` | Principal |
| **Fee Officer** | `FEE_PENDING`, `PARTIAL_PAYMENT` | Bursar / Fee Officer |
| **Enrollment** | `FULL_PAYMENT`, `READY_FOR_ENROLLMENT` | Registrar |
| **Student Creation** | `STUDENT_CREATED`, `COMPLETED` | Registrar / IT Admin |

---

## 3. Granular Action-Based Permission Matrix
Instead of blanket permissions, roles are validated per action at each stage.

| Role | View | Edit | Verify | Assign | Reject | Upload | Download | Email | Print | Reopen | Override | Delete |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Admission Officer**| Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | No | No | No |
| **Teacher** | Yes | No | No | No | No | No | Yes | No | Yes | No | No | No |
| **Scholarship Off.** | Yes | Yes | No | No | Yes | Yes | Yes | Yes | Yes | No | No | No |
| **Principal** | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | No |
| **Fee Officer** | Yes | No | No | No | No | Yes | Yes | Yes | Yes | No | No | No |
| **Registrar** | Yes | Yes | No | No | No | Yes | Yes | Yes | Yes | Yes | Yes | No |
| **Super Admin** | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes | Yes |

---

## 4. Multi-Channel Notification Matrix
Every transition triggers targeted notifications.

| Event / Transition | Target | Email | SMS | In-App Alert |
| :--- | :--- | :--- | :--- | :--- |
| `SUBMITTED` | Parent | Yes | No | Yes |
| `DOCUMENTS_PENDING` | Parent | Yes | Yes | Yes |
| `ASSIGNED_TO_TEACHER`| Teacher | Yes | No | Yes |
| `TEST_SCHEDULED` | Parent | Yes | Yes | Yes |
| `RECOMMENDED` | Principal | No | No | Yes |
| `PARENT_MEETING` | Parent | Yes | Yes | Yes |
| `SCHOLARSHIP_APPROVED`| Parent | Yes | Yes | Yes |
| `PARTIAL/FULL_PAYMENT`| Parent | Receipt | Yes | Yes |
| `STUDENT_CREATED` | Parent & Student | Yes | Yes | Yes |

---

## 5. Strict Audit Matrix Definition
Every action triggers an insert into `erp_audit_logs`. The payload must capture:
- **Who:** User ID, Username
- **Role:** Active Role at execution
- **Location:** Branch ID, School ID
- **State Change:** `Old Status` -> `New Status`
- **Context:** Action taken (e.g., "Enter Marks"), Custom Remarks
- **Technical:** IP Address, Browser / User Agent, Timestamp
- **Data Diff:** `Previous Values` (JSON), `New Values` (JSON)
- **Attachment:** Linked file IDs (if uploaded during action)

---

## 6. Database Impact Matrix (Transaction Profiles)
Defining exactly what happens under the hood during a stage transition.

### Example A: Teacher Submits Marks
*Triggered when transitioning to `TEST_COMPLETED` or `RECOMMENDED`.*
- **Updates `erp_application_marks`:** Inserts Written, Oral, Total, Remarks.
- **Updates `erp_applications`:** Sets `status` = `RECOMMENDED`.
- **Inserts `erp_application_status_history`:** "Assessed by Tr. Davis (Score: 85)".
- **Inserts `erp_notifications`:** In-App alert to Principal.
- **Inserts `erp_audit_logs`:** Logs score JSON diff and IP.

### Example B: Fee Received
*Triggered when transitioning to `FULL_PAYMENT`.*
- **Updates `erp_application_fees`:** Inserts payment row, sets `status` = `PAID`.
- **Updates `erp_applications`:** Sets `status` = `READY_FOR_ENROLLMENT`.
- **Inserts `erp_application_status_history`:** "Payment of 500k received (Ref: Bank123)".
- **Inserts `erp_email_queue`:** Triggers `FEE_RECEIPT` PDF generation and dispatch.
- **Inserts `erp_audit_logs`:** Logs payment ID and executor.

---

## 7. Universal Dashboard Specifications
All 9 dashboards must share the following architectural UI components:

1. **KPI Headers:** 4 summary cards at the top (e.g., Pending, Processed, Rejected, Revenue).
2. **Filters & Advanced Search:** By Date, Name, Ref No, Status, and Custom Fields (e.g., Marks > 80).
3. **Bulk Actions:** Multi-select checkboxes to trigger bulk verify, bulk assign, or bulk email.
4. **Row Actions (Three Dots):** View, Edit, Audit Trail.
5. **Detail Tabs (Standard Layout):**
   - *Profile:* Core applicant data.
   - *Timeline:* Visual render of `erp_application_status_history`.
   - *Email History:* Log of all dispatched comms.
   - *Attachments:* Integrated Document Viewer component.
   - *Activity Log:* Read-only render of `erp_audit_logs`.
6. **Export Options:** Universal CSV, Excel, and PDF export for the current datatable view.
7. **Security Gate:** Entire page wrapped in Thymeleaf `sec:authorize="hasAuthority('...')"` mapped to Section 3 of this document.

---

## 8. Frozen Development Order (Execution Roadmap)

With the architecture and requirements completely frozen, the implementation will strictly follow this sequence:

1. **Workflow Engine & State Machine:** Enums, Validators, Status History Service.
2. **Admission Officer Dashboard:** Verification logic and Document review UI.
3. **Teacher Assessment Dashboard:** Marks entry table, PDF report card viewer.
4. **Parent Meeting Dashboard:** Shortlist management and meeting logging.
5. **Scholarship Workflow:** Multi-tier branch/admin approval routing.
6. **Principal Approval Dashboard:** Final gatekeeping and overrides.
7. **Fee Collection Dashboard:** Invoicing, partial tracking, and PDF receipts.
8. **Enrollment Wizard:** Manual mapping overrides (Section, Transport route).
9. **Application → Student Conversion Engine:** The core `@Transactional` migrator.
10. **Reports & Analytics:** KPI generation and chart plotting.
11. **Email, Notifications & Audit Logs:** System-wide triggers and dispatching.
