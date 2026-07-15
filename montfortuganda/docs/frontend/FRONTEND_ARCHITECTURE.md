# FRONTEND ARCHITECTURE

*Status: Active*

This document serves as the enterprise reference for FRONTEND ARCHITECTURE.

## 1. Core Architecture Pattern
*   **Paradigm:** Multi-Page Application (MPA).
*   **Rendering:** Server-Side Rendering (SSR) via Thymeleaf for base layout, secured context, and initial DOM.
*   **Dynamic Data:** Client-side DOM manipulation via Vanilla JavaScript (No React, Angular, or Vue).
*   **State Management:** Stateless frontend. All data is fetched via REST APIs; state resides in the backend database or user session token.

## 2. Directory Structure
*   `src/main/resources/static/css/`: Global stylesheets (`global.css`, `admin.css`).
*   `src/main/resources/static/js/framework/`: Core infrastructure (`api.js`, `modal.js`, `toast.js`).
*   `src/main/resources/static/js/modules/`: Business logic scripts (`employees.js`, `admission.js`).
*   `src/main/resources/static/views/`: Reusable HTML fragments.

## 3. Session & Security Handling
*   All API requests must carry the JWT token. This is handled globally by `js/framework/api.js`.
*   If a `401 Unauthorized` or `403 Forbidden` is returned, the frontend must immediately redirect to the login page (`window.location.href = '/login'`).
*   Never store sensitive data (PII, salaries, grades) in `localStorage` or `sessionStorage`.

## 4. Routing Strategy
*   Thymeleaf handles top-level routing (e.g., `/admin/employees`).
*   JavaScript handles sub-views using the HTML5 History API (`window.history.pushState`) to toggle between list views and detail views without full page reloads.