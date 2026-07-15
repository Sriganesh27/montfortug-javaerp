# PAGE STRUCTURE

*Status: Active*

This document serves as the enterprise reference for PAGE STRUCTURE.

## 1. List View vs Detail View
*   Every module typically consists of two primary UI states:
    1.  **List View (e.g., `#ba-employees-list-view`)**: Contains the data table (`<table class="erp-table">`) and filters.
    2.  **Detail View (e.g., `#ba-add-employee-view`)**: Contains the form (`.detail-input`) for creating or editing records.
*   Views are toggled by adding/removing the `.hidden` class based on user actions.

## 2. Standard Containers
*   `.view-header`: The top bar containing the `<h2>` Title and `.action-btn-group` (Add, Export, Import buttons).
*   `.table-responsive`: Wrapper around tables to prevent horizontal overflow on small screens.
*   `.form-group`: Wrapper for `<label>` and `<input>` pairs in detail views.