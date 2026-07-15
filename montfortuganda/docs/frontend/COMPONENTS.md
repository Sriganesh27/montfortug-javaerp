# COMPONENTS

*Status: Active*

This document serves as the enterprise reference for COMPONENTS.

## 1. Modals (`.ba-modal-backdrop`, `.ba-modal-box`)
*   Used for confirmations, data imports, or file previews.
*   Must cover the entire screen (`position: fixed`, `z-index: 1000`).
*   Include standard `.ba-modal-actions` for Save/Cancel buttons.

## 2. Toasts (`AppToast`)
*   Global notification system injected via `toast.js`.
*   Supports `AppToast.success(msg)` and `AppToast.error(msg)`.
*   Auto-dismisses after 3 seconds.

## 3. Form Inputs (`.detail-input`)
*   Standardized input class for all text, number, and file inputs.
*   Enforces 1px solid border, `#f8fafc` background on disabled, and crisp focus rings.