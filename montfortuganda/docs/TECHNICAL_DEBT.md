# Technical Debt
*Status: Verified*

## Architecture Debt
* **Description:** Controllers directly injecting Repositories (specifically `PublicApplicationController` calling `BranchRepository`, etc.). Must move logic to Service Layer.
* **Priority:** CRITICAL

## Security Debt
* **Description:** Lack of file magic byte validation.
* **Priority:** CRITICAL

## Database Debt
* **Description:** `@Data` on JPA entities causing recursion.
* **Priority:** HIGH
