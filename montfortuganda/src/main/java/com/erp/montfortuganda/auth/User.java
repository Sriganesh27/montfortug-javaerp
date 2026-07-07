package com.erp.montfortuganda.auth;

import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

// CRITICAL: Prevent recursion while preserving AuditableEntity fields
@EqualsAndHashCode(callSuper = true, exclude = {"userRoles", "sessions", "loginHistory"})
@ToString(callSuper = true, exclude = {"userRoles", "sessions", "loginHistory"})
@DynamicUpdate
@Entity
@Table(name = "erp_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @JsonIgnore
    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Legacy role column.
     * Maintained for backward compatibility.
     * The new RBAC implementation uses erp_user_roles.
     */
    @Column(name = "role", length = 50)
    private String role;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_branch",
            referencedColumnName = "branch_id",
            foreignKey = @ForeignKey(name = "erp_users_ibfk_1")
    )
    private Branch assignedBranch;

    @Column(name = "is_active", nullable = false)
    private Integer isActive = 1;

    // ==========================================
    // SECURITY MAPPINGS
    // ==========================================

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpUserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpUserSession> sessions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErpLoginHistory> loginHistory = new ArrayList<>();

    // ==========================================
    // HELPER METHODS (Bidirectional Sync)
    // ==========================================

    public void addRole(ErpUserRole role) {
        userRoles.add(role);
        role.setUser(this);
    }

    public void removeRole(ErpUserRole role) {
        userRoles.remove(role);
        role.setUser(null);
    }

    public void addSession(ErpUserSession session) {
        sessions.add(session);
        session.setUser(this);
    }

    public void removeSession(ErpUserSession session) {
        sessions.remove(session);
        session.setUser(null);
    }

    public void addLoginHistory(ErpLoginHistory history) {
        loginHistory.add(history);
        history.setUser(this);
    }

    public void removeLoginHistory(ErpLoginHistory history) {
        loginHistory.remove(history);
        history.setUser(null);
    }
}