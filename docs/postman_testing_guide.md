# Postman Testing Guide: Department Module

You're right—we built the robust Services and Repositories, but we never exposed them to the web! You need a **REST Controller** to hit these endpoints from Postman.

Here is the complete `DepartmentController.java` to handle the HTTP requests, followed by the exact JSON payloads you can use in Postman to test it.

---

### 1. `DepartmentController.java`
**Location:** `src/main/java/com/erp/montfortuganda/school/controller/DepartmentController.java`

Create this file in your `.school.controller` package:

```java
package com.erp.montfortuganda.school.controller;

import com.erp.montfortuganda.common.dto.ApiResponse;
import com.erp.montfortuganda.common.dto.PagedResponse;
import com.erp.montfortuganda.model.enums.RecordStatus;
import com.erp.montfortuganda.school.dto.DepartmentDTO;
import com.erp.montfortuganda.school.service.DepartmentDeletionService;
import com.erp.montfortuganda.school.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentDeletionService departmentDeletionService;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(@Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO created = departmentService.createDepartment(dto);
        return ResponseEntity.ok(ApiResponse.success("Department created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentDTO dto) {
        DepartmentDTO updated = departmentService.updateDepartment(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("Department fetched successfully", department));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DepartmentDTO>>> searchDepartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) RecordStatus status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean isAcademic,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<DepartmentDTO> result = departmentService.searchDepartments(
                keyword, branchId, status, active, isAcademic, pageable);
                
        return ResponseEntity.ok(ApiResponse.success("Departments fetched successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentDeletionService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }
}
```

---

### 2. Postman Testing Guide

Once your app is running, open Postman and run these requests to verify the architecture.

#### A. Create a Department (POST)
**URL:** `POST http://localhost:8081/api/v1/departments`
**Headers:** `Content-Type: application/json`
*(Note: If you have JWT enabled, you'll need your `Authorization: Bearer <token>` header)*
**Body:**
```json
{
  "branchId": 1,
  "departmentCode": "SCI",
  "departmentName": "Science Department",
  "isAcademic": true,
  "description": "Handles all science subjects."
}
```

#### B. Update a Department (PUT)
**URL:** `PUT http://localhost:8081/api/v1/departments/1`
**Headers:** `Content-Type: application/json`
**Body:**
*(Note: Try changing the `version` to a lower number than what is in the DB to test Optimistic Locking!)*
```json
{
  "branchId": 1,
  "departmentCode": "SCI",
  "departmentName": "Advanced Science Department",
  "isAcademic": true,
  "status": "ACTIVE",
  "active": true,
  "version": 0
}
```

#### C. Search Departments (GET)
**URL:** `GET http://localhost:8081/api/v1/departments?keyword=Science&status=ACTIVE`
**Expected Response:** You should see the standard `ApiResponse` wrapper with a `PagedResponse` nested inside the `data` field.

#### D. Soft Delete Department (DELETE)
**URL:** `DELETE http://localhost:8081/api/v1/departments/1`
**Expected Result:** The `active` flag in the database changes to `false`, and the `status` changes to `INACTIVE`.

---

**Next Steps:** Create this controller, restart your server, and hit those endpoints! Let me know if you run into any validation errors or security blocks.
