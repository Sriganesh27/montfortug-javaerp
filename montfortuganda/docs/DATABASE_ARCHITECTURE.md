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

## ER Diagram
```text
(erp_users) }|--|| (erp_branches) ||--|{ (erp_applications)
```
