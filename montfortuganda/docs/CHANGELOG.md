# Changelog
*Status: Verified*

## 11-07-2026 (Staff Management Completion & UI Compaction)
* Architecture: Fully completed the Staff Management module (Department, Designation, and Employee) across both frontend and backend.
* Architecture: Deleted all old/legacy department, designation, and employee code and replaced it completely with the new architecture.
* Security: Created HashGen utility for secure BCrypt password provisioning.
* UI/UX: Adopted Strict Non-Breaking strategy. Compacted and dense-optimized `admin.css` and `global.css` for a high-density enterprise dashboard layout.
* UI/UX: Enforced strict ID-scoping on global foundation CSS to prevent style leakage outside of Branch Admin boundaries.

## 09-07-2026 (Core Infrastructure & Branch UI Stabilization)
* Backend: Implemented core platform infrastructure including BaseEntity, AuditableEntity (JPA Auditing), and global services (StorageService, DocumentSequenceService, UsernameService, PasswordService).
* Backend: Refactored BranchRepository packaging and integrated CurrentUserService with the new core infrastructure.
* Frontend: Fixed SPA lifecycle bugs in initAddBranchView() that caused duplicated event listeners and phantom form submissions.
* Frontend: Resolved JSON parsing exceptions for inchargeDetails extraction.
* Frontend: Fixed checkbox state mapping for levelIds during Branch Creation.
## 01-07-2026 (Security & Architecture Overhaul)
* Architecture Changes: Designed secure `VerifiedApplicationSession` to eliminate IDOR on public tracking routes. Designed `GlobalExceptionHandler` to mask server errors.
* Security: Implemented Session-Based Rate Limiting for DOB verification. Added `Cache-Control` header mandates for PII. Resolved Session Fixation vulnerabilities.
* UI/UX: Completed Email System Theme architecture with `#000000` headers for transparent logo rendering in email clients. Prepared frontend for cosmetic URLs (`/apply/print?student=Name`).

## Version 1.0.0 (Baseline)
* Architecture Changes: Baseline 3-tier Spring Boot.
* Security: JWT Authentication established.
* Known Debt: Controller/Repo coupling identified.


