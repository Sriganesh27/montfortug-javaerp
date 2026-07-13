// File: src/main/java/com/erp/montfortuganda/employee/dto/EmployeeDocumentRequest.java
package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeDocumentStatus;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDocumentRequest {

    // --- IDENTITY ---
    private Long employeeDocumentId;

    @Size(max = 50)
    private String employeeDocumentCode;

    // --- DOCUMENT DETAILS ---
    @NotNull(message = "Document type is required")
    private EmployeeDocumentType documentType;

    @NotBlank(message = "Document number is required")
    @Size(max = 100)
    private String documentNumber;

    @Size(max = 255)
    private String issuingAuthority;

    // --- TIMELINE ---
    private LocalDate issueDate;
    private LocalDate expiryDate;

    // --- VERIFICATION LIFECYCLE ---
    @NotNull(message = "Document status is required")
    private EmployeeDocumentStatus status = EmployeeDocumentStatus.PENDING;

    private Boolean verified = false;
    private LocalDate verificationDate;
    private Long verifiedByUserId;

    @Size(max = 1000)
    private String verificationRemarks;

    // --- ATTACHMENT & METADATA (Oracle Cloud Style Tracking) ---
    @Size(max = 255)
    private String documentFileName;

    @Size(max = 255)
    private String documentOriginalName;

    @Size(max = 100)
    private String documentContentType;

    private Long documentFileSize;

    @Size(max = 500)
    private String documentStoragePath;

    @Size(max = 1000)
    private String remarks;

    // --- SYSTEM ---
    @Min(value = 1)
    private Integer displayOrder = 1;

    private Boolean active = true;
    private Boolean deleted = false;
    private String fileData;
    private String fileName;
}