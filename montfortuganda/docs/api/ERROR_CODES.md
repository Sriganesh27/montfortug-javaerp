# Error Codes & Handling
*Status: Verified*
*Last Updated: 2026-06-30*

## Global Exception Handling
The application uses a `@ControllerAdvice` (`GlobalExceptionHandler`) to catch and format exceptions into a consistent structure using the generic `ApiResponse<T>` wrapper.

### 1. Validation Errors
* **Trigger:** `MethodArgumentNotValidException` (caused by failed `@Valid` constraints).
* **HTTP Status:** 400 Bad Request
* **Response Structure:** Returns `ApiResponse<Map<String, String>>`.
* **Behavior:** Maps the specific invalid DTO field names to their default constraint violation messages.
* **JSON Example:**
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "username": "Username cannot be blank",
    "password": "Password must be at least 8 characters"
  }
}
```

### 2. Runtime Errors
* **Trigger:** `RuntimeException` (thrown manually in Services/Controllers).
* **HTTP Status:** 400 Bad Request
* **Response Structure:** Returns `ApiResponse<String>`.
* **Behavior:** Returns the exact exception string message.
* **JSON Example:**
```json
{
  "success": false,
  "message": "User not found in database",
  "data": null
}
```

## Security Authentication Errors
* **Trigger:** Failed login in `AuthController`.
* **HTTP Status:** 401 Unauthorized
* **Response Structure:** Hardcoded Map (bypasses `ApiResponse`).
* **JSON Example:**
```json
{
  "message": "Invalid username or password"
}
```
*(Technical Debt: Security errors should ideally map through the global `ApiResponse` format for frontend consistency).*