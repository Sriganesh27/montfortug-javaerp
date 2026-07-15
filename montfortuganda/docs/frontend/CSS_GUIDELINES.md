# CSS GUIDELINES

*Status: Active*

This document serves as the enterprise reference for CSS GUIDELINES.

## 1. Enterprise Aesthetics (No AI Design)
*   **Color Palette:** Use the standardized slate-gray (`#475569`, `#334155`), pure white backgrounds, and deep gold/blue accents.
*   **Density:** Interfaces must be highly condensed (minimal padding/margins) to maximize data density. Similar to SAP or Oracle ERP screens.
*   **Avoid:** Massive rounded corners, massive drop shadows, thick borders, or overly colorful gradients. Use crisp `2px` or `4px` border-radius.

## 2. Global vs. Scoped Styles
*   `global.css`: Core resets, typography, and utility classes (`.hidden`, `.text-strong`).
*   `admin.css`: Layout-specific grids (e.g., `#main-content-area`).
*   Always prefix module-specific styling with the module's parent ID (e.g., `#ba-add-employee-view .detail-input`) to prevent style leakage across the SPA.