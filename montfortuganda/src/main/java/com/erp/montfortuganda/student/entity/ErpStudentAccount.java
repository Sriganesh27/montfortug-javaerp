package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "erp_student_accounts",
        indexes = {
                @Index(name = "idx_student_account_student", columnList = "student_id"),
                @Index(name = "idx_student_account_branch", columnList = "branch_id"),
                @Index(name = "idx_student_account_admission", columnList = "admission_no")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_student_account", columnNames = "student_id")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentAccount {

    public enum AccountStatus {
        ACTIVE, LOCKED, DISABLED, SUSPENDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    // Enterprise Optimistic Locking
    @Version
    @Column(name = "version")
    private Long version;

    @NotNull(message = "Student is required")
    @OneToOne(fetch = FetchType.LAZY)
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

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @NotBlank(message = "Password hash is required")
    @Size(max = 255, message = "Password hash cannot exceed 255 characters")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @NotNull(message = "Password changed flag is required")
    @Column(name = "password_changed", nullable = false)
    private Boolean passwordChanged;

    // Explicit security flag to force reset on next login
    @NotNull(message = "Password reset required flag is mandatory")
    @Column(name = "password_reset_required", nullable = false)
    private Boolean passwordResetRequired;

    @NotNull(message = "Failed attempts count is required")
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts;

    @NotNull(message = "Account locked flag is required")
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Size(max = 100, message = "Last login IP cannot exceed 100 characters")
    @Column(name = "last_login_ip", length = 100)
    private String lastLoginIp;

    @Size(max = 255, message = "Last login device cannot exceed 255 characters")
    @Column(name = "last_login_device", length = 255)
    private String lastLoginDevice;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @Lob
    @Column(name = "remarks")
    private String remarks;

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
        if (this.accountStatus == null) {
            this.accountStatus = AccountStatus.ACTIVE;
        }
        if (this.passwordChanged == null) {
            this.passwordChanged = false;
        }
        if (this.passwordResetRequired == null) {
            this.passwordResetRequired = false;
        }
        if (this.failedAttempts == null) {
            this.failedAttempts = 0;
        }
        if (this.accountLocked == null) {
            this.accountLocked = false;
        }

        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}