# Changelog
*Status: Verified*

## 01-07-2026 (Security & Architecture Overhaul)
* Architecture Changes: Designed secure `VerifiedApplicationSession` to eliminate IDOR on public tracking routes. Designed `GlobalExceptionHandler` to mask server errors.
* Security: Implemented Session-Based Rate Limiting for DOB verification. Added `Cache-Control` header mandates for PII. Resolved Session Fixation vulnerabilities.
* UI/UX: Completed Email System Theme architecture with `#000000` headers for transparent logo rendering in email clients. Prepared frontend for cosmetic URLs (`/apply/print?student=Name`).

## Version 1.0.0 (Baseline)
* Architecture Changes: Baseline 3-tier Spring Boot.
* Security: JWT Authentication established.
* Known Debt: Controller/Repo coupling identified.
