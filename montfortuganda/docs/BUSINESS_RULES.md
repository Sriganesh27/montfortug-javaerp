# Business Rules
*Status: Verified*

* **Branch Rules:** Every branch receives a unique `school_code`. Auto-generates a Branch Admin user.
* **Admission Rules:** Public applications are unauthenticated and track progress via Status History.
* **Security Rules:** Passwords must be hashed via BCrypt. All APIs except `/api/auth` and `/api/public` require a valid JWT.
* **File Upload Rules:** Files saved locally. (Recommendation: Enforce Magic Bytes).
