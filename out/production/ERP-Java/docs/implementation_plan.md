# Employee Module Modification Plan

Based on the latest database scan, here is the detailed plan to apply the modifications to the Employee Module without breaking the existing architecture.

## Proposed Changes

### 1. Database Schema & Data Updates

#### [MODIFY] `database/montfortug_erp_employees.sql`
- Add columns: `address_county`, `address_sub_county`, `address_parish`. (Will rename/alias `address_village` to represent LC-I).
- Add columns: `skills` (TEXT), `languages_spoken` (TEXT).

#### [MODIFY] `database/montfortug_erp_employee_qualifications.sql`
- Add column: `division_or_grade` (VARCHAR).
- Add column: `custom_level` (VARCHAR) to store raw text when `OTHER` is selected.

#### [MODIFY] `database/montfortug_erp_employee_experience.sql`
- Add column: `employee_experience_type` (VARCHAR) for Employee / Self-Employed distinction.
- Note: We will use the existing `employee_experience_company_name` field for "Organisation".

#### [NEW] `database/seed_updates.sql`
> [!IMPORTANT]
> The database scan shows existing IDs 1-4 are `PRINCIPAL`, `VICE_PRINCIPAL`, `HEAD_TEACHER`, `TEACHER`. We will provide SQL to update and insert the new roles logically in the sequence you requested without breaking foreign keys.
- **Designations**: Insert `DIRECTOR`, `DEAN_OF_STUDIES (DOS)`, `HOD`, and `BURSAR`.
- **Departments**: Update the existing codes (e.g., change `ADMIN` to `ADMN`, `ACADEMICS` to `ACA`, `ADMISSIONS` to `ADMS`, `FINANCE` to `FIN`) to exactly match your requested shorthand codes.

---

### 2. Backend (Entities and Enums)

#### [MODIFY] `employee/entity/ErpEmployee.java`
- Add Fields: `String addressCounty`, `String addressSubCounty`, `String addressParish`.
- Add Fields: `String skills`, `String languagesSpoken`.

#### [MODIFY] `employee/entity/ErpEmployeeQualification.java`
- Add: `String divisionOrGrade`.
- Add: `String customLevel`.
- **Note**: The existing `employeeQualificationSpecialization` field will be mapped to "Subject" on the frontend.

#### [MODIFY] `employee/entity/ErpEmployeeExperience.java`
- Add: `ExperienceEmploymentType employeeExperienceType`.

#### [MODIFY] `employee/enums/QualificationLevel.java`
- Rewrite enum values to: `PRIMARY, SECONDARY, SENIOR_SECONDARY, DIPLOMA, CERTIFICATE, GRADUATION, PG, DR_PHD, OTHER`.

#### [MODIFY] `employee/enums/ExperienceEmploymentType.java`
- Change to: `EMPLOYEE, SELF_EMPLOYED`.

#### [MODIFY] DTOs & Mappers
- Add all the new fields to `EmployeeCreateRequest`, `EmployeeUpdateRequest`, and `EmployeeResponse`.

---

### 3. Frontend (UI and JavaScript)

#### [MODIFY] `static/views/admin/add-employee.html`
- **Address Section**: Insert new inputs for `County`, `Sub-County (LC-III)`, and `Parish (LC-II)` right after `District`. Update `Village` label to include `(LC-I)`.
- **New Fieldset**: Add a `<fieldset>` containing `Skills` and `Languages Spoken` before the dynamic Qualification section.

#### [MODIFY] `static/js/modules/employees.js`
- **Qualifications Dynamic Array**: 
  - Change `Level` from a text box to a `<select>` dropdown populated with the new Enum values.
  - **Dynamic Toggle Logic 1**: If `OTHER` is selected, render an input box for `customLevel`.
  - **Dynamic Toggle Logic 2**: If `PRIMARY` or `SECONDARY` is selected, hide/disable the `Subject` input.
  - Add `Division/Grade` input field.
- **Experience Dynamic Array**: 
  - Change "Company" label to "Organisation".
  - Add "Employee Type" dropdown (`EMPLOYEE`, `SELF_EMPLOYED`).
  - Add "Post Held" input (mapped to existing `jobRole`).
  - Add "Duration" input (if not automatically calculating from Start/End dates).
- Update the JS DTO builder (`gatherAsync`) to harvest these new fields and send them to the API.

---

## Verification Plan

### Automated Checks
- The Spring Boot application must boot without `HibernateException` mapped against the modified SQL schema.
- DTO Validations must pass.

### Manual Verification
- Render the `Add Employee` form.
- Verify Address fields flow properly: District -> County -> Sub-County -> Parish -> Village.
- Select `PRIMARY` in Qualification and verify that `Subject` disappears.
- Select `OTHER` in Qualification and verify that the `Custom Level` input box appears.
- Ensure the Designations dropdown lists `Director` first, and the Departments dropdown reflects the new codes (`ACA`, `ADMS`, etc.).

---
**Please approve this plan. As per your strict Read-Only mode instructions, once approved, I will output the SQL scripts and Java code blocks in the chat for you to manually copy and paste.**
