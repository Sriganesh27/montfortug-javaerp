# ERP Architecture
*Status: Verified*

## Overall Architecture
Monolithic Spring Boot backend with a REST API and server-rendered Vanilla JS frontend.
* Layer Responsibilities: Controllers (API routing), Services (Business logic/Transactions), Repositories (Data access).

## Package Structure
Feature-driven (`com.erp.montfortuganda.[module]`).
*Observation:* Highly cohesive, but some cross-module dependencies exist.

## Module Boundaries
* `auth`: Security & JWT
* `school`: Master data (Branches, Levels)
* `admission`: Public application intake
* `scholarship`: NGO allocations

## Dependency Flow
Frontend -> Controller -> Service -> Repository -> MySQL

## Request Lifecycle
HTTP Request -> JwtAuthenticationFilter -> Controller -> Service -> JPA -> DB.

## Authentication & Authorization Flow
* Authentication: JWT Token provided in `Authorization` header.
* Authorization: Evaluated via `@PreAuthorize("hasRole(...)")`.

## Transaction Flow
Managed by `@Transactional` at the Service layer.

## Error Handling Flow
*Requires Manual Verification:* Needs a unified `@ControllerAdvice` for consistent JSON errors.

```text
[Frontend] <--> [Spring Security] <--> [Controllers] <--> [Services] <--> [Repositories] <--> [MySQL]
```
