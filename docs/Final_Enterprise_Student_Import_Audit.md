# STRICT ENTERPRISE STUDENT IMPORT: FINAL AUDIT SPECIFICATION

This document outlines the **final enterprise specification** for migrating an existing school (500–10,000+ active students, alumni, boarders, and special cases) into the Montfort Uganda Multi-School ERP. 

By analyzing the strict constraints of the `student/entity` package, including the newly audited `ErpStudentHostel` and `ErpStudentFeeAssignment` entities, we can guarantee that this architecture supports every real-world migration scenario without data loss.

---

## Part 1: Migration Scenario Coverage

The Excel template must inherently support the following edge cases via the mapped entities:

| Scenario | Handled By | Required Fields / Enums in Template |
| :--- | :--- | :--- |
| **Current Students** | `ErpStudentEnrollment` | `Enrollment Status = ACTIVE` |
| **Alumni** | `ErpStudentEnrollment` | `Enrollment Status = GRADUATED` |
| **Transfers (In/Out)** | `ErpStudentEnrollment` | `Admission Type = TRANSFER` or `Status = TRANSFERRED` |
| **Suspended Students** | `ErpStudentEnrollment` | `Enrollment Status = SUSPENDED` |
| **Boarding Students** | `ErpStudentHostel` | `Hostel Required = YES`, `Hostel Name`, `Room Name` |
| **Day Scholars** | `ErpStudentHostel` | `Hostel Required = NO` (Ignores Hostel columns) |
| **Sponsored Students** | `ErpParent` | `Fee Responsibility = SPONSOR` |
| **Scholarship Students** | `ErpStudentFeeAssignment`| `Scholarship Amount > 0`, `Scholarship ID` |
| **Transport Users** | `ErpStudentTransport` | `Transport Required = YES`, `Route Name`, `Monthly Fee` |
| **Medical Conditions** | `ErpStudentMedical` | `Allergies`, `Chronic Conditions`, `Ongoing Medication` |
| **Special Needs** | `ErpStudentMedical` | `Special Needs` definition |

---

## Part 2: Enterprise Excel Workbook Structure

To prevent a massive, unreadable 100-column spreadsheet, the Excel workbook will be structured with **Master Data Lookup Sheets** and **Data Validation**.

### 1. Workbook Structure
- **Sheet 1: `Instructions`** (Read-only guidelines and color-coding rules)
- **Sheet 2: `Student Migration Data`** (The main import template, max 60 columns grouped by color)
- **Sheet 3: `Lookup_Master`** (Hidden/Locked sheet containing dynamic IDs from the database)

### 2. Lookup Sheets & Data Validation Lists
When the user downloads the template, the ERP must query the database and populate the `Lookup_Master` sheet with real IDs.
Data validation dropdowns in the `Student Migration Data` sheet will link to these columns:

* **Academic Year:** `[2025-2026, 2026-2027]`
* **Class & Section:** `[Primary 1 - A, Primary 1 - B, Primary 2 - A]`
* **Stream / House:** `[Science, Arts] / [Blue House, Red House]`
* **Hostel Details:** `[Hostel Alpha - Room 101, Hostel Beta - Room 201]`
* **Transport Routes:** `[City Center, Northern Bypass]`

### 3. Enumerated Dropdowns (Hardcoded Validation)
- **Gender:** `Male`, `Female`
- **Blood Group:** `A_PLUS`, `A_MINUS`, `B_PLUS`, `B_MINUS`, `AB_PLUS`, `AB_MINUS`, `O_PLUS`, `O_MINUS`, `UNKNOWN`
- **Admission Type:** `NEW`, `TRANSFER`, `READMISSION`
- **Enrollment Status:** `ACTIVE`, `PROMOTED`, `TRANSFERRED`, `WITHDRAWN`, `GRADUATED`, `SUSPENDED`, `EXPELLED`
- **Fee Responsibility:** `FATHER`, `MOTHER`, `GUARDIAN`, `SPONSOR`

---

## Part 3: Import Processing & Batching Strategy

### 4. Duplicate Detection Rules (Pre-Flight Checks)
Before inserting into the database, the `StudentImportService` must run a memory check against existing records:
1. **Primary Unique Key:** `admissionNo` + `branchId`. If a match is found in `erp_students`, flag as DUPLICATE.
2. **Secondary Unique Key:** `studentCode` + `branchId`.
3. **Soft Warning:** Matching `firstName` + `lastName` + `dateOfBirth` (Flags as POTENTIAL DUPLICATE in the migration report).

### 5. Import Batching Strategy
A school migrating 10,000 students cannot hold a single HTTP request open.
* The Excel file is uploaded and saved to `uploads/school_code/imports/`.
* The ERP queues a **Background Async Task** (e.g., Spring `@Async` or JMS).
* Processing is batched in chunks of **250 students**.
* `EntityManager.flush()` and `clear()` are invoked after each chunk to prevent `OutOfMemoryError`.

### 6. Transactional Rollback Strategy
- The system will NOT use a global rollback for the entire Excel file (one failure should not block 9,999 valid students).
- Instead, transaction boundaries (`@Transactional(propagation = Propagation.REQUIRES_NEW)`) are wrapped around **each individual student row**.
- If `ErpStudent` saves, but `ErpStudentHostel` fails a constraint, that *single student row* is rolled back, and the error is logged. The batch continues to the next student.

---

## Part 4: Enterprise Audit & Reporting Strategy

When migrating thousands of students, administrators require absolute visibility into what succeeded, what failed, and why.

### 7. Error Excel Generation
If any rows fail validation or transaction boundaries, the system generates an `Errors_Only_Student_Import.xlsx`.
- This file contains the **exact same columns** as the uploaded template.
- It contains **only the rows that failed**.
- A new Column A is appended: `FAILURE REASON` (e.g., "Row 45: Parent Email exceeds 150 characters; Hostel Room '101' does not exist in branch").
- The school administrator can fix this specific file and re-upload it.

### 8. Success Report
A concise JSON/PDF summary provided immediately upon batch completion:
* **Total Rows Parsed:** 5,000
* **Successfully Migrated:** 4,850
* **Failed Rows:** 150
* **Duplicates Skipped:** 0

### 9. Migration Report
A detailed cross-module consistency report generated post-import to verify financial and logistical impact:
* **Hostel Capacity Impact:** "400 students allocated to hostels. Warning: Hostel Alpha is now 105% over capacity."
* **Transport Route Impact:** "150 students allocated to City Center route."
* **Fee Generation:** "4,850 fee ledgers successfully initialized based on Scholarship and Sponsored statuses."

### 10. Import Audit Log
Every bulk import action is logged in a centralized `erp_audit_logs` table (or similar enterprise audit log) containing:
* `timestamp`
* `uploaded_by_user_id`
* `branch_id`
* `filename`
* `status` (COMPLETED, PARTIAL_SUCCESS, FAILED)
* `records_processed`
