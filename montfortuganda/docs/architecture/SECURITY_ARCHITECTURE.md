# Security Architecture
*Status: Verified*
*Last Updated: 2026-06-30*

## Authentication
* **Type:** Stateless JWT (JSON Web Token).
* **Delivery:** HTTP-Only Secure Cookie (`jwt_token`). It is NOT passed in the JSON payload body or `Authorization` headers manually by the frontend.
* **Filter:** `JwtAuthenticationFilter` intercepts requests and validates the token.
* **Token Lifecycle:** 
  - Token expiration (`jwtExpirationInMs`) and signing key (`secret`) are provided via `application.properties`.
  - Signature Algorithm: HMAC SHA-256 (`HS256`).
* **Session Management:** `SessionCreationPolicy.STATELESS` configured in `SecurityConfig`.

## Authorization & Roles
* **Enforcement:** `@PreAuthorize` used at the Controller method level.
* **Roles Extracted:** User details inject roles from the database (`erp_users.role`).
* **Role Matrix (Observed from Code):**
  - `SUPER_ADMIN`: Can create branches, access global configs.
  - `BRANCH_ADMIN`: Isolated to branch operations.
  - *Note:* Roles are currently hardcoded Strings (e.g., `"SUPER_ADMIN"`).
* **Public Gateway Protection:** Admins attempting to login from public entry points without the `SECURE_ADMIN_GATEWAY` role parameter are explicitly blocked at the `AuthController` layer.

## File Upload Security
* **Status:** Requires Manual Verification / Remediation.
* **Vulnerability:** `FileStorageService` currently accepts files based on extensions. Magic Byte (file signature) validation is missing and must be implemented to prevent RCE.

## Password Encoding
* **Algorithm:** BCrypt (`BCryptPasswordEncoder` configured in `SecurityConfig`).

## Protection Configurations
* **CSRF:** Disabled for REST APIs (`csrf.disable()`).
* **CORS:** Inherits global Spring Boot defaults (requires explicit WebMvcConfig if separating frontend domains).