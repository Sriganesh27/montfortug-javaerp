package com.erp.montfortuganda.student.entity;

import com.erp.montfortuganda.school.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Enterprise Student Security Entity.
 * Strictly decoupled from ErpStudent to ensure high-performance authentication
 * without loading heavy academic data.
 */
@Data
@Entity
@DynamicUpdate
@Table(name = "erp_student_accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_student_account", columnNames = "student_id")
        },
        indexes = {
                @Index(name = "idx_student_account_student", columnList = "student_id"),
                @Index(name = "idx_student_account_branch", columnList = "branch_id"),
                @Index(name = "idx_student_account_admission", columnList = "admission_no")
        }
)
@EqualsAndHashCode(exclude = {"student", "branch"})
@ToString(exclude = {"student", "branch"})
public class ErpStudentAccount implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ==========================================
    // ENUMS
    // ==========================================
    public enum AccountStatus {
        ACTIVE, LOCKED, DISABLED, SUSPENDED
    }

    // ==========================================
    // PRIMARY KEY
    // ==========================================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    // ==========================================
    // MASTER REFERENCES
    // ==========================================
    @NotNull(message = "Student is required")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private ErpStudent student;

    @NotNull(message = "Branch is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotBlank(message = "Admission number is required")
    @Size(max = 50, message = "Admission number cannot exceed 50 characters")
    @Column(name = "admission_no", nullable = false, length = 50)
    private String admissionNo;

    // ==========================================
    // LOGIN CREDENTIALS
    // ==========================================
    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username cannot exceed 100 characters")
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    /**
     * BCrypt encoded password.
     * Never store plain text passwords.
     */
    @NotBlank(message = "Password hash is required")
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @NotNull(message = "Account status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @NotNull(message = "Password changed flag is required")
    @Column(name = "password_changed", nullable = false)
    private Boolean passwordChanged = false;

    @NotNull(message = "Password reset required flag is required")
    @Column(name = "password_reset_required", nullable = false)
    private Boolean passwordResetRequired = false;

    /**
     * Number of consecutive failed login attempts.
     * Used for automatic account lockout.
     */
    @NotNull(message = "Failed attempts is required")
    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    /**
     * Indicates whether the account is currently locked.
     */
    @NotNull(message = "Account locked flag is required")
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;

    /**
     * Last successful authentication timestamp.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Size(max = 100)
    @Column(name = "last_login_ip", length = 100)
    private String lastLoginIp;

    @Size(max = 255)
    @Column(name = "last_login_device", length = 255)
    private String lastLoginDevice;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    // ==========================================
    // STATUS
    // ==========================================
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // ==========================================
    // AUDIT & LOCKING
    // ==========================================
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_by", updatable = false)
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

        if (active == null)
            active = true;

        if (accountStatus == null)
            accountStatus = AccountStatus.ACTIVE;

        if (passwordChanged == null)
            passwordChanged = false;

        if (passwordResetRequired == null)
            passwordResetRequired = false;

        if (failedAttempts == null)
            failedAttempts = 0;

        if (accountLocked == null)
            accountLocked = false;

        if (version == null)
            version = 0L;

        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}