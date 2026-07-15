# Implement Frontend UI for Nested Employee Payloads

The backend is fully equipped to handle `contacts`, `qualifications`, `experiences`, and `documents` payloads for an Employee. However, the frontend currently lacks the UI components to collect and send this nested data.

Furthermore, my audit of the frontend source code revealed that `add-employee.html` currently contains an exact duplicate of the read-only Detail View from `employees.html`, and completely lacks the required `<form id="add-emp-form">` and `<div id="ba-add-employee-view">` wrappers expected by `employees.js`.

## User Review Required

> [!IMPORTANT]  
> The Add Employee UI needs to become a multi-section form to support adding multiple Contacts, Qualifications, Experiences, and Documents.

## Proposed Changes

### 1. Rebuild `add-employee.html`

I will completely rewrite `add-employee.html` to serve as the actual Add Employee form.

**Structure:**
- Wrap in `<div id="ba-add-employee-view">` and `<form id="add-emp-form">`.
- **Section 1: Personal Details** (Name, DOB, Gender, etc.)
- **Section 2: Employment Details** (Dept, Designation, Category, Dates)
- **Section 3: Nested Collections (Dynamic Tables/Cards):**
  - **Contacts:** Add multiple emergency/primary contacts.
  - **Qualifications:** Add multiple educational degrees.
  - **Experiences:** Add prior work experiences.
  - **Documents:** Add document metadata (uploads can be handled later or mocked as string paths for now).

### 2. Upgrade `employees.js` Payload Serialization

The `initAddEmployeeView()` and `initEmployeesView()` functions in `employees.js` will be upgraded.

- Provide JS functions to dynamically add/remove rows for the nested collections in the DOM.
- On Save/Submit, iterate through the DOM rows to construct the arrays: `contacts[]`, `qualifications[]`, `experiences[]`, and `documents[]`.
- Attach these arrays to the main `payload` object before sending via `apiPost` (for creation) and `apiPut` (for updates).

### 3. Upgrade Detail/Edit View in `employees.html`

- Add tabs or accordions to the `emp-detailView` to display existing Contacts, Qualifications, Experiences, and Documents.
- When the user clicks "Edit Details", allow them to modify the nested collections using the same dynamic table/card approach used in the Add form.

## Verification Plan

### Manual Verification
1. I will ask you to navigate to the "Add Employee" screen and verify that the form renders correctly with all dynamic nested sections.
2. We will submit a test employee with 2 contacts and 1 qualification, and verify that the JSON payload successfully persists via the backend.
3. We will open the Detail View for that employee to verify the nested collections render correctly.
