# Montfort Uganda ERP - Pending Tasks
**Last Updated:** July 14, 2026

## High Priority

### Employee Bulk Import Enhancements
- `[ ]` **Import Progress Tracking**: Implement WebSockets or Polling mechanism to give frontend progress (e.g., 20%, 40%, 100%) for large 5000+ row files so the UI doesn't timeout.
- `[ ]` **Downloadable Error Excel**: Implement a service that converts the `EmployeeImportResponse` JSON into an `Employee_Import_Errors.xlsx` file highlighting failed rows for easy user correction.
- `[ ]` **Import Audit Logs**: Create `erp_import_logs` entity to log metadata of every import (User, Branch, Timestamp, Total Rows, Success Count, Failed Count, Duration).

### Admissions Module
- `[ ]` Build frontend UI for Admissions based on the generated BRD.
- `[ ]` Implement backend APIs and entities for Admissions flow.

## Medium Priority
- `[ ]` **Import Rollback Strategy**: Add a UI toggle to allow "Partial Import" (current implementation) vs "Strict Rollback Entire File" (transaction aborts completely if any row fails).
- `[ ]` **Advanced Duplicate Strategy**: Implement UI options allowing the user to select how duplicates are handled (Skip, Update, Replace, Merge).
- `[ ]` **Preview Mode for Imports**: Create a staging table/cache to parse the Excel file and show the user exactly what will be imported/failed *before* they click "Confirm Import".

## Low Priority
- `[ ]` **File Virus Scanning Hook**: Integrate a basic virus scan hook or deeper Apache Tika signature verification beyond standard `Content-Type` headers.
