// File: src/main/java/com/erp/montfortuganda/employee/entity/ErpEmployeeDocument.java
package com.erp.montfortuganda.employee.entity;

import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.employee.enums.EmployeeDocumentType;
import com.erp.montfortuganda.model.AuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_employee_documents")
public class ErpEmployeeDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_document_id")
    private Long employeeDocumentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private ErpEmployee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_document_type", nullable = false, length = 50)
    private EmployeeDocumentType employeeDocumentType;

    @Column(name = "employee_document_name", nullable = false, length = 255)
    private String employeeDocumentName;

    @Column(name = "employee_document_description", columnDefinition = "TEXT")
    private String employeeDocumentDescription;

    @Column(name = "employee_document_file_name", nullable = false, length = 255)
    private String employeeDocumentFileName;

    @Column(name = "employee_document_original_file_name", length = 255)
    private String employeeDocumentOriginalFileName;

    @Column(name = "employee_document_file_path", nullable = false, length = 500)
    private String employeeDocumentFilePath;

    @Column(name = "employee_document_file_extension", length = 20)
    private String employeeDocumentFileExtension;

    @Column(name = "employee_document_mime_type", length = 100)
    private String employeeDocumentMimeType;

    @Column(name = "employee_document_file_size")
    private Long employeeDocumentFileSize;

    @Column(name = "employee_document_issue_date")
    private LocalDate employeeDocumentIssueDate;

    @Column(name = "employee_document_expiry_date")
    private LocalDate employeeDocumentExpiryDate;

    @Column(name = "employee_document_verified", nullable = false)
    private Boolean employeeDocumentVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_document_verified_by")
    private User employeeDocumentVerifiedBy;

    @Column(name = "employee_document_verified_at")
    private LocalDateTime employeeDocumentVerifiedAt;

    @Column(name = "employee_document_is_mandatory", nullable = false)
    private Boolean employeeDocumentIsMandatory = false;

    @Column(name = "employee_document_active", nullable = false)
    private Boolean employeeDocumentActive = true;

    @Column(name = "employee_document_remarks", columnDefinition = "TEXT")
    private String employeeDocumentRemarks;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}