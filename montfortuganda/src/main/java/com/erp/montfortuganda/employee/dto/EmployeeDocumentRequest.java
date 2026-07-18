package com.erp.montfortuganda.employee.dto;

import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeDocumentRequest {

    private Long employeeDocumentId;

    @NotNull(message = "Document type is required")
    private EmployeeDocumentType documentType;

    /**
     * Human-readable document name.
     * Example:
     * Uganda National ID
     * Teaching Certificate
     * Passport
     */
    @NotBlank(message = "Document name is required")
    @Size(max = 255)
    private String documentName;

    /**
     * Optional identification/reference number.
     * There is currently no separate document-number column
     * in the database.
     */
    @Size(max = 150)
    private String documentNumber;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Size(max = 1000)
    private String remarks;

    /**
     * Base64 file data sent from the current frontend.
     */
    private String fileData;

    /**
     * Original uploaded filename.
     */
    @Size(max = 255)
    private String fileName;
}