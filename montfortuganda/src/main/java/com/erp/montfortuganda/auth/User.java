package com.erp.montfortuganda.auth;

import com.erp.montfortuganda.model.AuditableEntity;
import com.erp.montfortuganda.school.Branch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "erp_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", length = 50)
    private String role;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_branch", referencedColumnName = "branch_id")
    private Branch assignedBranch;

    @Column(name = "is_active", columnDefinition = "integer default 1")
    private Integer isActive = 1;
    // ==========================================
    // MAPPINGS
    // ==========================================
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ErpUserRole> userRoles = new java.util.ArrayList<>();
}