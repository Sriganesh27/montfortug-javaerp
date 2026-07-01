# Development Memory (MASTER REFERENCE)
*Status: Verified*
*Last Updated: 2026-06-30*

## Current Architecture
Monolithic Spring Boot 3.x, JWT Auth, MySQL JPA, Vanilla JS Frontend.

## Strict Architectural Rules
1. **Flow:** Controller -> Service -> Repository -> Database.
2. **Logic:** No business logic in Controllers.
3. **Data:** DTO pattern strictly enforced. Never duplicate DTOs or Services.
4. **Injection:** Constructor Injection only.
5. **Transactions:** Managed by `@Transactional` at the Service layer.

## HTML / CSS / JS Standards
* **HTML:** Semantic tags (`article`, `fieldset`, etc). No div-soup. Forms must look like compact enterprise systems (SAP, Oracle). Every input needs a label.
* **CSS:** 100% Scoped. No inline styles. No `*` global selectors. Avoid oversized AI-looking spacing/corners. Professional compact grids.
* **JS:** Modular. Separate files only. Attach events via `addEventListener` (NO inline `onclick`). Use async/await and robust fetch wrappers.

## Things that MUST NEVER change without migration
1. Database Schema mappings (`erp_` prefix tables).
2. Stateless JWT authentication flow.
3. `school_code` uniqueness constraint (used for auth generation and directories).

## Known Technical Debt
1. Pagination is missing on transactional tables.
2. Controllers directly call repositories in some modules.
3. Entity equals/hashCode generation via Lombok `@Data` is unsafe.

**Future AI assistants must read this document to understand architectural boundaries.**
