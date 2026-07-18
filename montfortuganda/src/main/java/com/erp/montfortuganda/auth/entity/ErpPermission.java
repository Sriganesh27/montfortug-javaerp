package com.erp.montfortuganda.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(
        name = "erp_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permission_code",
                        columnNames = "permission_code"
                )
        }
)
@EqualsAndHashCode
@ToString
public class ErpPermission implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    @NotBlank
    @Size(max = 150)
    @Column(name = "permission_name", nullable = false, length = 150)
    private String permissionName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {

        if (active == null) {
            active = true;
        }

        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // MAPPINGS
    // ==========================================
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ErpRolePermission> rolePermissions = new java.util.ArrayList<>();
}