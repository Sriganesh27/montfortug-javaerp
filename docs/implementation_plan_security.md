# Enterprise Security Implementation Plan

To elevate the application's security posture to enterprise standards, we need to address several critical vulnerabilities present in the current `SecurityConfig` and API layer.

## Proposed Security Upgrades

### 1. Robust CORS Configuration
Currently, `SecurityConfig.java` lacks an explicit Cross-Origin Resource Sharing (CORS) policy.
**Action:** We will implement a strict `CorsConfigurationSource` restricting allowed origins to your specific live domains (e.g., `https://erp.montfortbrothersuganda.com`), while explicitly mapping allowed HTTP methods and headers.

### 2. JSON-Aware Security Exception Handling
Presently, if a user accesses a protected route without a valid token, Spring Security defaults to returning an HTML `401 Unauthorized` page. This causes Single Page Applications (SPAs) to crash when they attempt to parse the response as JSON.
**Action:** We will implement custom `AuthenticationEntryPoint` and `AccessDeniedHandler` components. These will intercept security rejections and guarantee that the frontend always receives a structured `ApiResponse` in JSON format.

### 3. Fortified Security Headers
We must prevent browsers from falling victim to common frontend attacks.
**Action:** We will configure Spring Security to enforce:
- **X-Frame-Options (DENY):** Prevents the app from being embedded in IFrames (Clickjacking protection).
- **X-Content-Type-Options (nosniff):** Prevents browsers from incorrectly sniffing MIME types.
- **Strict-Transport-Security (HSTS):** Enforces HTTPS-only connections.
- **X-XSS-Protection:** Enables the browser's built-in Cross-Site Scripting filter.

### 4. Brute Force Login Protection (Rate Limiting)
Your `/api/auth/login` endpoint is currently entirely open to brute-force password guessing algorithms.
**Action:** We will implement a lightweight `LoginAttemptService` to track failed login attempts by username/IP. If an IP fails to authenticate >5 times in a row, they will be temporarily locked out.

### 5. Verified Strengths (No Action Needed)
- **Secrets Management:** Your `application.properties` correctly injects `${JWT_SECRET}` and database credentials via environment variables. This is excellent.
- **SQL Injection:** Utilizing Spring Data JPA fully mitigates first-order SQL injection natively.

---

## Open Questions

> [!WARNING]  
> **JWT Storage Strategy:** Currently, your API expects the JWT token in the `Authorization: Bearer <token>` header. This implies the frontend stores it in `localStorage` or `sessionStorage`, making it vulnerable to XSS (Cross-Site Scripting) attacks extracting the token.
> 
> **Question:** Would you like to keep the current `Authorization` header approach (easier to implement, requires `csrf.disable()`), OR would you prefer we upgrade the authentication system to issue the JWT inside an **HttpOnly Cookie** (much more secure, but requires enabling CSRF protection and updating the frontend API calls)?

---

## Verification Plan
1. We will attempt a cross-origin request from an unauthorized domain and verify it is blocked.
2. We will attempt to trigger 6 failed logins and verify the IP is locked out.
3. We will hit a secure endpoint with no token and verify a clean JSON `401` response is returned.

> [!IMPORTANT]  
> Please review this Security Implementation Plan. Let me know your decision regarding the JWT Storage Strategy (Headers vs HttpOnly Cookies), and I will generate the complete security code for you to copy and paste!
