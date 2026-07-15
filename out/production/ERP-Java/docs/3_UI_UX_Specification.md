# 3. UI/UX Specification Document
## Project: Montfort Uganda Multi-School ERP
## Module: Admission Management System

---

## 1. Executive Summary
This document specifies the exact UI/UX requirements for the 9 distinct dashboards required to run the enterprise admission pipeline. Every dashboard is mapped to a specific user role and phase of the workflow. The design language must match the existing ERP (Slate-gray and gold density, server-side rendered via Thymeleaf, interactive Vanilla JS).

---

## 2. Dashboard Specifications

### 1. Admission Officer Dashboard
- **Target User:** Admission Officer
- **Purpose:** Intake, validation, and verification of applications.
- **Top KPIs:** Total New, Missing Docs, Verification Pending.
- **Main View (List Screen):**
  - **Filters:** Academic Year, Class, Status (Pending/Verified), Date Range.
  - **Table Columns:** Ref No, Applicant Name, Applied Class, Submission Date, Docs Status, Actions.
  - **Row Actions:** View Profile, Request Docs (Email Trigger), Verify.
- **Detail Popup (Applicant Profile):**
  - Side-by-side view: Applicant Details (Left) | Document Viewer (Right).
  - **Buttons:** `[Assign to Teacher]` -> Opens Teacher Selection Modal.

### 2. Teacher Dashboard
- **Target User:** Class Teachers
- **Purpose:** Conduct academic assessments and log marks.
- **Top KPIs:** Assigned Tests, Pending Results, Processed Today.
- **Main View (List Screen):**
  - **Filters:** Exam Date, Status (Pending Test, Assessed).
  - **Table Columns:** Ref No, Name, Target Class, Test Date, Actions.
- **Assessment Form Screen:**
  - Read-only applicant details and downloaded previous report cards.
  - **Form Fields:** `Written Marks (0-100)`, `Oral Marks (0-50)`.
  - **Auto-calculated Field:** `Total Score`.
  - **Dropdown:** `Recommendation` (Recommended, Waitlisted, Not Recommended).
  - **Textarea:** `Internal Remarks` (Hidden from parents).
  - **Button:** `[Submit Assessment]` (Triggers `APPLICATION_ASSESS`).

### 3. Parent Meeting Dashboard
- **Target User:** Admission Officer / Principal
- **Purpose:** Final alignment with parents on school rules and fees.
- **Main View:** Kanban Board of Shortlisted applicants.
- **Meeting Form Screen:**
  - **Checkboxes:** `School Rules Accepted`, `Uniform Policy Accepted`, `Hostel Rules Accepted`.
  - **Select:** `Transport Required?` (Yes/No).
  - **Outcome Buttons:** `[Parent Accepts]`, `[Parent Declines]`, `[Needs Time]`, `[Apply for Scholarship]`.

### 4. Scholarship Dashboard
- **Target User:** Scholarship Officer
- **Purpose:** Manage financial aid requests.
- **Top KPIs:** Total Requests, Approved Funds, Remaining Branch Budget.
- **Main View (List Screen):**
  - **Table Columns:** Applicant Name, Requested Amount, Reason, Status, Action.
- **Action Modal:**
  - **Inputs:** `Approved Amount`, `Remarks`.
  - **Buttons:** `[Approve from Branch Fund]`, `[Forward to Super Admin]`, `[Reject]`.

### 5. Principal Dashboard
- **Target User:** Principal
- **Purpose:** Executive oversight and final approvals.
- **Top KPIs:** Total Admitted, Total Rejected, Revenue Projection.
- **Approval Queue (List Screen):**
  - Only shows applicants with `Teacher Recommendation = RECOMMENDED` and `Meeting = ACCEPTED`.
  - **Row Actions:** Quick Approve, View Full File.
  - **Bulk Operations:** Multi-select checkbox -> `[Bulk Approve]`.

### 6. Fee Officer Dashboard
- **Target User:** Bursar / Fee Officer
- **Purpose:** Collect registration and admission fees.
- **Top KPIs:** Expected Revenue, Collected Today, Pending Balances.
- **Main View:**
  - **Table Columns:** Ref No, Name, Invoice Total, Paid, Balance, Status.
- **Payment Form Screen:**
  - **Inputs:** `Amount Received`, `Payment Mode` (Cash, Bank, Mobile Money), `Reference Number`.
  - **Buttons:** `[Record Payment & Email Receipt]`.
  - **Warning Logic:** If payment < Minimum Deposit, flag as `Partial Payment`.

### 7. Enrollment Dashboard
- **Target User:** Registrar
- **Purpose:** Finalize data mapping and trigger Student Creation.
- **Main View:**
  - Only shows applications where `Status = APPROVED` and `Fee Status = PAID` or `PARTIAL`.
- **Enrollment Wizard Screen:**
  - **Step 1:** Confirm spelling of Names and DOB.
  - **Step 2:** Finalize Class, Section, and Roll Number allocation.
  - **Step 3:** Assign Transport Route and Hostel bed (if applicable).
  - **Button:** `[Execute Enrollment]` -> Fires the massive transactional conversion.

### 8. Student Creation Dashboard
- **Target User:** Registrar / IT Admin
- **Purpose:** Audit log of successful vs failed enrollments.
- **Main View:**
  - **Table:** App Ref, Generated Student Code, Generated Parent Login, Generation Status.
  - **Row Action:** `[Resend Welcome Credentials]`.

### 9. Reports Dashboard
- **Target User:** All Admins (Based on permissions)
- **Purpose:** Graphical and tabular analytics.
- **Widgets Required:**
  - Funnel Chart: Total Applications -> Verified -> Tested -> Approved -> Enrolled.
  - Pie Chart: Admission by Class.
  - Bar Chart: Fee Collection vs Expected.
- **Export Capabilities:** Export to CSV/Excel/PDF for all tabular data.

---

## 3. Global UI Standards
1. **Document Viewer:** All PDF/Image uploads must open in an embedded modal frame. Users should never have to download files to verify them.
2. **Timeline Panel:** Every Detail Screen must feature a right-side "Activity Timeline" pulling from `erp_application_status_history`. (e.g., "Assessed by Tr. John at 2:00 PM").
3. **Email Logs:** Every screen must have a "Communications" tab showing all automated emails sent to the applicant.
4. **Data Density:** Use condensed table padding to maximize rows on screen. Avoid unnecessary white space.
