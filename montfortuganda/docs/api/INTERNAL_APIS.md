# Internal APIs
*Status: Verified*
*Last Updated: 2026-06-30*

## GET `/api/branches`
* **Controller:** `BranchController`
* **Service:** None (Directly calls `BranchRepository` - Technical Debt)
* **Authentication Required:** True
* **Required Role:** `isAuthenticated()`
* **Request DTO:** None
* **Response DTO:** `List<Branch>` (Direct Entity Return)
* **Validation Rules:** None
* **Possible Error Responses:** Inherits global Spring Boot errors.
* **Related Database Tables:** `erp_branches`, `erp_branch_levels`

**Response JSON Example:**
```json
[
  {
    "branchId": 1,
    "branchName": "Test Branch",
    "schoolCode": "TB001",
    "branchLocation": "Uganda",
    "isActive": 1
  }
]
```

## POST `/api/branches`
* **Controller:** `BranchController`
* **Service:** None (Directly calls `BranchRepository`)
* **Authentication Required:** True
* **Required Role:** `hasRole('SUPER_ADMIN')`
* **Request DTO:** `Branch` (Direct Entity Request)
* **Response DTO:** `Branch` (Direct Entity Return)
* **Validation Rules:** `school_code` must be unique.
* **Possible Error Responses:** DataIntegrityViolation (500) if schoolCode duplicates.
* **Related Database Tables:** `erp_branches`

**Request JSON Example:**
```json
{
  "branchName": "New Branch",
  "schoolCode": "NB002",
  "branchLocation": "Kampala"
}
```