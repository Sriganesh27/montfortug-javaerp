// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeUpdateRequest.java
package com.erp.montfortuganda.employee.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeUpdateRequest extends EmployeeCreateRequest {
    // Inherits all fields from CreateRequest.
    // We omit employee_id here because it should be passed as a @PathVariable in the Controller
}