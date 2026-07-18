package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Audit trail of student lifecycle events (Archived / Restored).
 * Mapped as Many-to-One since a student could theoretically be archived,
 * restored, and archived again over their lifetime.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_archives",
        indexes = {
                @Index(name = "idx_archive_student", columnList = "student_id"),
                @Index(name = "idx_archive_branch", columnList = "branch_id"),
                @Index(name = "idx_archive_admission", columnList = "admission_no"),
                @Index(name = "idx_archive_status", columnList = "archive_status"),
                @Index(name = "idx_archive_reason", columnList = "archive_reason"),
                @Index(name = "idx_archive_leaving_date", columnList = "date_of_leaving")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentArchive implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // NESTED ENUMS
    // ==========================================
    public enum ArchiveStatus {
        ARCHIVED, RESTORED
    }

    public enum ArchiveReason {
        GRADUATED, TRANSFERRED, WITHDRAWN, EXPELLED, DECEASED, DROPPED_OUT, OTHER
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "archive_id")
    private Long archiveId;

    // ==========================================
    // OPTIMISTIC LOCKING
    // ==========================================
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // ==========================================
    // MASTER REFERENCES
    // ==========================================
    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    // ==========================================
    // ARCHIVE INFORMATION
    // ==========================================
    @NotNull(message = "Archive status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "archive_status", nullable = false, length = 20)
    private ArchiveStatus archiveStatus = ArchiveStatus.ARCHIVED;

    @NotNull(message = "Archive reason is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "archive_reason", nullable = false, length = 30)
    private ArchiveReason archiveReason;

    /**
     * Date on which the student permanently left the institution.
     */
    @NotNull(message = "Date of leaving is required")
    @Column(name = "date_of_leaving", nullable = false)
    private LocalDate dateOfLeaving;

    @Column(name = "restored_by")
    private Long restoredBy;

    @Column(name = "restored_at")
    private LocalDateTime restoredAt;

    @Size(max = 255, message = "Restore reason cannot exceed 255 characters")
    @Column(name = "restore_reason", length = 255)
    private String restoreReason;

    @Lob
    @Column(name = "remarks")
    private String remarks;

    // ==========================================
    // AUDIT
    // ==========================================
    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================
    // JPA LIFECYCLE CALLBACKS
    // ==========================================
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}