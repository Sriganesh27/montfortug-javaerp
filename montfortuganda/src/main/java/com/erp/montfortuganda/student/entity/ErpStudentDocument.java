package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_student_documents", indexes = {
        @Index(name = "idx_student_documents_student", columnList = "student_id"),
        @Index(name = "idx_student_documents_branch", columnList = "branch_id"),
        @Index(name = "idx_student_documents_admission_no", columnList = "admission_no"),
        @Index(name = "idx_student_documents_status", columnList = "document_status")
})
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentDocument {

    // Document Status Enum
    public enum DocumentStatus {
        PENDING, VERIFIED, REJECTED, EXPIRED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull(message = "Document type is required")
    @Size(max = 100, message = "Document type cannot exceed 100 characters")
    @Column(name = "document_type", nullable = false, length = 100)
    private String documentType;

    @NotNull(message = "Document name is required")
    @Size(max = 150, message = "Document name cannot exceed 150 characters")
    @Column(name = "document_name", nullable = false, length = 150)
    private String documentName;

    @Size(max = 100, message = "Document number cannot exceed 100 characters")
    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @NotNull(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Size(max = 255, message = "Original file name cannot exceed 255 characters")
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @NotNull(message = "File path is required")
    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Size(max = 20, message = "File extension cannot exceed 20 characters")
    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    @Size(max = 100, message = "MIME type cannot exceed 100 characters")
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status")
    private DocumentStatus documentStatus;

    @Lob
    @Column(name = "remarks")
    private String remarks;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================

    @PrePersist
    protected void onCreate() {
        if (this.active == null) {
            this.active = true;
        }
        if (this.documentStatus == null) {
            this.documentStatus = DocumentStatus.PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        this.uploadedAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}