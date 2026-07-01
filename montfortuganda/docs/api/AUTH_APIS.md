# Auth APIs
*Status: Verified*
*Last Updated: 2026-06-30*

## POST `/api/auth/login`
* **Controller:** `AuthController`
* **Service:** `UserDetailsServiceImpl` (via `AuthenticationManager`)
* **Authentication Required:** False
* **Required Role:** None
* **Request DTO:** `AuthRequest`
* **Response DTO:** `AuthResponse`
* **Validation Rules:**
  - If `role` from DB is `SUPER_ADMIN` or `BRANCH_ADMIN`, the Request `role` MUST equal `SECURE_ADMIN_GATEWAY`.
* **Possible Error Responses:** 
  - `401 Unauthorized`: "Invalid username or password"
  - `401 Unauthorized`: "Authentication failed: User session is invalid."
* **Related Database Tables:** `erp_users`

**Request JSON Example:**
```json
{
  "username": "user123",
  "password": "password123",
  "branchId": 1,
  "role": "SECURE_ADMIN_GATEWAY"
}
```

**Response JSON Example:**
```json
{
  "token": null,
  "role": "SUPER_ADMIN",
  "branchId": 1
}
```
*(Note: JWT token is delivered via HTTP-Only Secure Cookie, not in the JSON body).*

## POST `/api/auth/logout`
* **Controller:** `AuthController`
* **Service:** None
* **Authentication Required:** False
* **Required Role:** None
* **Request DTO:** None
* **Response DTO:** String
* **Validation Rules:** Clears the `jwt_token` cookie.
* **Possible Error Responses:** None.
* **Related Database Tables:** None