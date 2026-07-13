// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeDocumentDTO.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeDocumentDTO {
    private Long employeeDocumentId;
    private Long employeeId;
    private EmployeeDocumentType employeeDocumentType;
    private String employeeDocumentName;
    private String employeeDocumentFileName;
    private String employeeDocumentFilePath;
    private LocalDate employeeDocumentExpiryDate;
    private Boolean employeeDocumentVerified;
    private Boolean employeeDocumentActive;
}