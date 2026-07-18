# Data Dictionary
*Status: Verified*
*Last Updated: 2026-06-30*

## Table: `erp_users`
* **Entity Class:** `com.erp.montfortuganda.auth.entity.User`
* **Purpose:** Authentication credentials.
* **Used By Services:** `UserDetailsServiceImpl`, `BranchServiceImpl`
* **Used By Controllers:** `AuthController`, `SuperAdminApiController`

| Column | Data Type | Nullable | Unique | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | Integer (PK) | No | Yes | Auto-increment primary key. |
| `username` | String (100) | No | Yes | Login identifier. |
| `password` | String | No | No | BCrypt hashed password. |
| `role` | String (50) | Yes | No | e.g. 'SUPER_ADMIN', 'BRANCH_ADMIN'. |
| `assigned_branch` | Integer (FK)| Yes | No | Maps to `erp_branches(branch_id)`. FetchType.LAZY. |
| `is_active` | Integer | Yes | No | Default 1. Soft delete flag. |
| `created_at` | LocalDateTime | No | No | Inherited from AuditableEntity. |
| `updated_at` | LocalDateTime | No | No | Inherited from AuditableEntity. |

## Table: `erp_branches`
* **Entity Class:** `com.erp.montfortuganda.school.entity.Branch`
* **Purpose:** Multi-tenant master data.
* **Used By Services:** `BranchService`
* **Used By Controllers:** `BranchController`, `SuperAdminApiController`

| Column | Data Type | Nullable | Unique | Description |
| :--- | :--- | :--- | :--- | :--- |
| `branch_id` | Integer (PK) | No | Yes | Auto-increment primary key. |
| `branch_name` | String | Yes | No | Human-readable name. |
| `school_code` | String (10) | Yes | Yes | Critical system identifier (e.g. UG001). |
| `branch_location`| String | Yes | No | Physical location. |
| `contact_details`| Text | Yes | No | Phone/Email blob. |
| `incharge_details`| Text | Yes | No | Admin contact blob. |
| `school_photo_url`| String | Yes | No | Path to uploaded logo. |
| `gov_document_url`| String | Yes | No | Path to license pdf. |
| `foundation_date`| String | Yes | No | Date established. |
| `is_active` | Integer | Yes | No | Default 1. Soft delete flag. |
| `created_at` | LocalDateTime | No | No | Inherited from AuditableEntity. |

*Relationships:*
* `@OneToMany` to `BranchLevel` (`cascade = CascadeType.ALL`, `orphanRemoval = true`).