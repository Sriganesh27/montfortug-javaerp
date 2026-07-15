# API INTEGRATION

*Status: Active*

This document serves as the enterprise reference for API INTEGRATION.

## 1. The Global Fetch Wrapper
*   Never use raw `fetch()` directly in module code.
*   Always use the wrappers defined in `api.js`:
    *   `apiGet(endpoint)`
    *   `apiPost(endpoint, data)`
    *   `apiPut(endpoint, data)`
    *   `apiDelete(endpoint)`

## 2. Authentication Handling
*   The `api.js` wrappers automatically inject the JWT Bearer token into the `Authorization` header.
*   They globally intercept HTTP 401 (Unauthorized) and HTTP 403 (Forbidden) to redirect the user to `/login`.

## 3. Error Handling
*   All `api.js` methods return a rejected promise if the `response.ok` is false.
*   Modules must catch these errors and display them using `AppToast.error(err.message)`.