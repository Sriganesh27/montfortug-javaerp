# Database Architecture
*Status: Verified*

## Tables

### `erp_branches`
* Purpose: Multi-tenant core structure.
* Columns: `branch_id` (PK), `branch_name`, `school_code`, `is_active`.
* Used By Modules: `school`, `admission`, `scholarship`.

### `erp_users`
* Purpose: Auth credentials.
* Columns: `user_id` (PK), `username`, `password`, `role`.

### `erp_applications`
* Purpose: Core transactional intake.
* Columns: `application_id` (PK), `branch_id` (FK).
* Constraints: Missing compound index on branch/status.

### Student Domain
* Tables: `erp_students`, `erp_student_enrollment`, `erp_student_fee_ledger`, `erp_student_hostel`, `erp_student_medical`, `erp_student_transport`.
* Purpose: Highly normalized transactional and master records for enrolled students. 
* Architecture: Enforces `CHECK` constraints (no negative fees) and `@Version` optimistic locking.

### Admission Domain
* Tables: `erp_application_interviews`, `erp_internal_scholarships`, `erp_application_fees`.
* Purpose: Normalizes the admission workflow to prevent the `erp_applications` table from becoming a "God Table".

## ER Diagram
```text
(erp_users) }|--|| (erp_branches) ||--|{ (erp_applications)
```
