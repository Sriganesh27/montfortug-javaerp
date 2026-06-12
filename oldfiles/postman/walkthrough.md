# ERP Migration Walkthrough: Application & Admission Module

## Overview
We have successfully completed the migration of the Application and Admission module from the legacy PHP system to the Spring Boot Java Backend. The system now has full parity with the legacy PHP frontend, including file handling, dynamic dropdowns, authentication bypass for public images, and seamless database insertion across 5 interconnected tables.

## 1. Frontend Enhancements (JavaScript & UI)
- **Dynamic School Name**: Fixed the hardcoded "St. Kizito" name by dynamically fetching the `tenant_name` from the backend API on page load.
- **Subjects Table Rendering**: Added parsing for the JSON `subjects` array so that the student's subjects are displayed cleanly in the Admission Modal.
- **Streams & Scholarships**: Restored the missing Stream dropdown `[A, B, C, D, E]` and the dynamic Scholarship Details input field.
- **Data Parity**: Matched every single Javascript payload parameter exactly to what the Spring Boot `StudentController` expects.

## 2. File Upload & Authentication Bypass
- **The 403 Forbidden Bug**: Discovered that the legacy PHP system served images directly from the filesystem, but Spring Security was blocking unauthenticated access to the `assets/` folder.
- **The Solution**: Configured `SecurityConfig.java` to whitelist `/assets/**` and `WebMvcConfig.java` to register a `ResourceHandler` that points directly to the external `uploads/` folder on the local machine.
- **Result**: Publicly uploaded application photos and documents now load instantly in the browser without requiring a JWT token.

## 3. The "Admit Student" Pipeline & Migration
When an application is approved and the student is admitted, a complex pipeline triggers:

### Database Insertion
We successfully mapped the legacy SQL inserts to the Java `JdbcTemplate` for 5 interconnected tables:
1. `erp_students`
2. `erp_parents`
3. `erp_academichistory`
4. `erp_enrollment`
5. `erp_student_accounts`

### Schema Resolution (The Ghost Columns Bug)
- **The Problem**: A previous ORM run had accidentally generated duplicate snake_case ghost columns (e.g. `admission_no`, `photo_path`) and marked them as `NOT NULL`. Additionally, the `erp_academichistory` table had a mismatched schema.
- **The Fix**: We ran a custom Java SQL utility to `DROP` all 12 duplicate columns across the database, added the missing `SubjectMarks` and `FormerSchoolCode` columns, and updated the Java `StudentService` to perfectly match the actual database schema.

### File Migration
- **Fallback Logic**: Implemented an automated file migration system in `StudentService.java`.
- When an application is admitted, the backend takes the `Photo` and `PreviousMarks` PDF from the temporary applications folder, renames them using the permanent Student Admission Number, and copies them to the permanent `/students/` directory.

## Validation Complete ✅
All **8 Test Cases** executed via Postman have passed successfully, confirming full end-to-end functionality of the Application & Admissions Module!
