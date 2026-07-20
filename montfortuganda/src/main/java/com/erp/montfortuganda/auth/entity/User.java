package com.erp.montfortuganda.auth.entity;

import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.entity.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(
        callSuper = true,
        exclude = {
                "userRoles",
                "sessions",
                "loginHistory"
        }
)
@ToString(
        callSuper = true,
        exclude = {
                "password",
                "userRoles",
                "sessions",
                "loginHistory"
        }
)
@DynamicUpdate
@Entity
@Table(name = "erp_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "id")
    private Integer id;

    @NotBlank
    @Size(max = 100)
    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @JsonIgnore
    @NotBlank
    @Size(max = 255)
    @Column(
            name = "password",
            nullable = false
    )
    private String password;

    /**
     * Legacy role column retained for backward compatibility.
     * The RBAC implementation also uses erp_user_roles.
     */
    @Column(
            name = "role",
            length = 50
    )
    private String role;

    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_branch",
            referencedColumnName = "branch_id",
            foreignKey = @ForeignKey(
                    name = "erp_users_ibfk_1"
            )
    )
    private Branch assignedBranch;

    @Column(
            name = "is_active",
            nullable = false
    )
    private Integer isActive = 1;

    // ==========================================
    // TEMPORARY CREDENTIAL SECURITY
    // ==========================================

    @JsonIgnore
    @Column(
            name = "must_change_password",
            nullable = false
    )
    private Boolean mustChangePassword = false;

    @JsonIgnore
    @Column(name = "temporary_password_created_at")
    private LocalDateTime temporaryPasswordCreatedAt;

    @JsonIgnore
    @Column(name = "temporary_password_expires_at")
    private LocalDateTime temporaryPasswordExpiresAt;

    @JsonIgnore
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(
            name = "credential_delivery_status",
            nullable = false,
            length = 30
    )
    private CredentialDeliveryStatus credentialDeliveryStatus =
            CredentialDeliveryStatus.NOT_REQUIRED;

    @JsonIgnore
    @Column(name = "credentials_sent_at")
    private LocalDateTime credentialsSentAt;

    @JsonIgnore
    @Column(
            name = "credential_delivery_attempts",
            nullable = false
    )
    private Integer credentialDeliveryAttempts = 0;

    @JsonIgnore
    @Column(
            name = "credential_version",
            nullable = false
    )
    private Integer credentialVersion = 0;

    // ==========================================
    // SECURITY MAPPINGS
    // ==========================================

    @Setter(AccessLevel.NONE)
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ErpUserRole> userRoles =
            new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ErpUserSession> sessions =
            new ArrayList<>();

    @Setter(AccessLevel.NONE)
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ErpLoginHistory> loginHistory =
            new ArrayList<>();

    // ==========================================
    // BIDIRECTIONAL RELATIONSHIP HELPER
    // ==========================================

    public void addRole(
            ErpUserRole userRole
    ) {
        userRoles.add(userRole);
        userRole.setUser(this);
    }
}