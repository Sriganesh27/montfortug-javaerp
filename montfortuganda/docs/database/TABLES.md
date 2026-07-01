# Database Tables Overview
*Status: Verified*
*Last Updated: 2026-06-30*

## 1. `erp_users`
* **Purpose:** Stores authentication credentials and role assignments.
* **Entity Mapping:** `com.erp.montfortuganda.auth.User`
* **Primary Key:** `id`
* **Foreign Keys:** `assigned_branch` -> `erp_branches(branch_id)`

## 2. `erp_branches`
* **Purpose:** Core master data for multi-tenancy.
* **Entity Mapping:** `com.erp.montfortuganda.school.Branch`
* **Primary Key:** `branch_id`
* **Foreign Keys:** None.

## 3. `erp_branch_levels`
* **Purpose:** Mapping table joining Branches to Levels (e.g., Primary, Secondary).
* **Entity Mapping:** `com.erp.montfortuganda.school.BranchLevel`
* **Primary Key:** `id`
* **Foreign Keys:** `branch_id` -> `erp_branches`, `level_id` -> `erp_levels`