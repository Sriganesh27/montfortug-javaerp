# Security Guidelines
*Status: Verified*

* **Authentication:** Stateless JWT only.
* **Authorization:** Role-based (`hasRole`) via `@PreAuthorize`.
* **Password Policy:** Bcrypt hashing.
* **File Upload Security:** (Recommendation) Validate Magic Bytes, do not trust extensions.
* **SQL Injection:** Prevented by Hibernate/JPA prepared statements.
* **XSS Prevention:** Handled by frontend DOM escaping (e.g. `textContent` over `innerHTML`).
