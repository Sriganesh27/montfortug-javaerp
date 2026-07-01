# Development Memory

*Status: Verified*

## Current Architecture
Monolithic Spring Boot 3.x, JWT Auth, MySQL JPA, Vanilla JS Frontend.

## Things that MUST NEVER change without migration
1. Database Schema mappings (`erp_` prefix tables).
2. Stateless JWT authentication flow.
3. `school_code` uniqueness constraint.

**Future AI assistants must read this document to understand architectural boundaries.**
